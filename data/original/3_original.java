protected void masterOperation(
    Task task,
    PutTrainedModelAction.Request request,
    ClusterState state,
    ActionListener<Response> listener
) {
    TrainedModelConfig config = request.getTrainedModelConfig();
    try {
        if (request.isDeferDefinitionDecompression() == false) {
            config.ensureParsedDefinition(xContentRegistry);
        }
    } catch (IOException ex) {
        listener.onFailure(ExceptionsHelper.badRequestException("Failed to parse definition for [{}]", ex, config.getModelId()));
        return;
    }

    // NOTE: hasModelDefinition is false if we don't parse it. But, if the fully parsed model was already provided, continue
    boolean hasModelDefinition = config.getModelDefinition() != null;
    if (hasModelDefinition) {
        try {
            config.getModelDefinition().getTrainedModel().validate();
        } catch (ElasticsearchException ex) {
            listener.onFailure(
                ExceptionsHelper.badRequestException("Definition for [{}] has validation failures.", ex, config.getModelId())
            );
            return;
        }

        TrainedModelType trainedModelType = TrainedModelType.typeFromTrainedModel(config.getModelDefinition().getTrainedModel());
        if (trainedModelType == null) {
            listener.onFailure(
                ExceptionsHelper.badRequestException(
                    "Unknown trained model definition class [{}]",
                    config.getModelDefinition().getTrainedModel().getName()
                )
            );
            return;
        }

        if (config.getModelType() == null) {
            // Set the model type from the definition
            config = new TrainedModelConfig.Builder(config).setModelType(trainedModelType).build();
        } else if (trainedModelType != config.getModelType()) {
            listener.onFailure(
                ExceptionsHelper.badRequestException(
                    "{} [{}] does not match the model definition type [{}]",
                    TrainedModelConfig.MODEL_TYPE.getPreferredName(),
                    config.getModelType(),
                    trainedModelType
                )
            );
            return;
        }

        if (config.getInferenceConfig().isTargetTypeSupported(config.getModelDefinition().getTrainedModel().targetType()) == false) {
            listener.onFailure(
                ExceptionsHelper.badRequestException(
                    "Model [{}] inference config type [{}] does not support definition target type [{}]",
                    config.getModelId(),
                    config.getInferenceConfig().getName(),
                    config.getModelDefinition().getTrainedModel().targetType()
                )
            );
            return;
        }

        TransportVersion minCompatibilityVersion = config.getModelDefinition().getTrainedModel().getMinimalCompatibilityVersion();
        if (state.getMinTransportVersion().before(minCompatibilityVersion)) {
            listener.onFailure(
                ExceptionsHelper.badRequestException(
                    "Cannot create model [{}] while cluster upgrade is in progress.",
                    config.getModelId()
                )
            );
            return;
        }
    }

    TrainedModelConfig.Builder trainedModelConfig = new TrainedModelConfig.Builder(config).setVersion(MlConfigVersion.CURRENT)
        .setCreateTime(Instant.now())
        .setCreatedBy("api_user")
        .setLicenseLevel(License.OperationMode.PLATINUM.description());
    AtomicReference<ModelPackageConfig> modelPackageConfigHolder = new AtomicReference<>();

    if (hasModelDefinition) {
        trainedModelConfig.setModelSize(config.getModelDefinition().ramBytesUsed())
            .setEstimatedOperations(config.getModelDefinition().getTrainedModel().estimatedNumOperations());
    } else {
        // Set default location for the given model type.
        trainedModelConfig.setLocation(
            Optional.ofNullable(config.getModelType()).orElse(TrainedModelType.TREE_ENSEMBLE).getDefaultLocation(config.getModelId())
        );
    }

    if (ModelAliasMetadata.fromState(state).getModelId(trainedModelConfig.getModelId()) != null) {
        listener.onFailure(
            ExceptionsHelper.badRequestException(
                "requested model_id [{}] is the same as an existing model_alias. Model model_aliases and ids must be unique",
                config.getModelId()
            )
        );
        return;
    }

    if (TrainedModelAssignmentMetadata.fromState(state).hasDeployment(trainedModelConfig.getModelId())) {
        listener.onFailure(
            ExceptionsHelper.badRequestException(
                "Cannot create model [{}] the id is the same as an current model deployment",
                config.getModelId()
            )
        );
        return;
    }

    ActionListener<Boolean> finishedStoringListener = ActionListener.wrap(bool -> {
        TrainedModelConfig configToReturn = trainedModelConfig.clearDefinition().build();
        if (modelPackageConfigHolder.get() != null) {
            triggerModelFetchIfNecessary(
                configToReturn.getModelId(),
                modelPackageConfigHolder.get(),
                request.isWaitForCompletion(),
                ActionListener.wrap(
                    downloadTriggered -> listener.onResponse(new PutTrainedModelAction.Response(configToReturn)),
                    listener::onFailure
                )
            );
        } else {
            listener.onResponse(new PutTrainedModelAction.Response(configToReturn));
        }
    }, listener::onFailure);

    var isPackageModel = config.isPackagedModel();
    ActionListener<Void> checkStorageIndexSizeListener = ActionListener.wrap(
        r -> trainedModelProvider.storeTrainedModel(trainedModelConfig.build(), finishedStoringListener, isPackageModel),
        listener::onFailure
    );

    ActionListener<Void> tagsModelIdCheckListener = ActionListener.wrap(r -> {
        if (TrainedModelType.PYTORCH.equals(trainedModelConfig.getModelType())) {
            client.admin()
                .indices()
                .prepareStats(InferenceIndexConstants.nativeDefinitionStore())
                .clear()
                .setStore(true)
                .execute(ActionListener.wrap(stats -> {
                    IndexStats indexStats = stats.getIndices().get(InferenceIndexConstants.nativeDefinitionStore());
                    if (indexStats != null
                        && indexStats.getTotal().getStore().getSizeInBytes() > MAX_NATIVE_DEFINITION_INDEX_SIZE.getBytes()) {
                        listener.onFailure(
                            new ElasticsearchStatusException(
                                "Native model store has exceeded the maximum acceptable size of {}, "
                                    + "please delete older unused pytorch models",
                                RestStatus.CONFLICT,
                                MAX_NATIVE_DEFINITION_INDEX_SIZE.toString()
                            )
                        );
                        return;
                    }

                    checkStorageIndexSizeListener.onResponse(null);
                }, e -> {
                    if (ExceptionsHelper.unwrapCause(e) instanceof ResourceNotFoundException) {
                        checkStorageIndexSizeListener.onResponse(null);
                        return;
                    }
                    listener.onFailure(
                        new ElasticsearchStatusException(
                            "Unable to calculate stats for definition storage index [{}], please try again later",
                            RestStatus.SERVICE_UNAVAILABLE,
                            e,
                            InferenceIndexConstants.nativeDefinitionStore()
                        )
                    );
                }));
            return;
        }
        checkStorageIndexSizeListener.onResponse(null);
    }, listener::onFailure);

    ActionListener<Void> modelIdTagCheckListener = ActionListener.wrap(
        r -> checkTagsAgainstModelIds(request.getTrainedModelConfig().getTags(), tagsModelIdCheckListener),
        listener::onFailure
    );

    ActionListener<Void> handlePackageAndTagsListener = ActionListener.wrap(r -> {
        if (isPackageModel) {
            resolvePackageConfig(trainedModelConfig.getModelId(), ActionListener.wrap(resolvedModelPackageConfig -> {
                try {
                    TrainedModelValidator.validatePackage(trainedModelConfig, resolvedModelPackageConfig, state);
                } catch (ValidationException e) {
                    listener.onFailure(e);
                    return;
                }
                modelPackageConfigHolder.set(resolvedModelPackageConfig);
                setTrainedModelConfigFieldsFromPackagedModel(trainedModelConfig, resolvedModelPackageConfig, xContentRegistry);

                checkModelIdAgainstTags(trainedModelConfig.getModelId(), modelIdTagCheckListener);
            }, listener::onFailure));
        } else {
            checkModelIdAgainstTags(trainedModelConfig.getModelId(), modelIdTagCheckListener);
        }
    }, listener::onFailure);

    checkForExistingTask(
        client,
        trainedModelConfig.getModelId(),
        request.isWaitForCompletion(),
        listener,
        handlePackageAndTagsListener,
        request.timeout()
    );
}