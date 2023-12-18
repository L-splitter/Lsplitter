/**
 * This method is responsible for handling the master operation.
 * It decomposes the original long method into smaller ones for better readability and maintainability.
 *
 * @param task The task to be performed.
 * @param request The request to put the trained model.
 * @param state The current state of the cluster.
 * @param listener The listener to handle the response.
 */
protected void masterOperation(
    Task task,
    PutTrainedModelAction.Request request,
    ClusterState state,
    ActionListener<Response> listener
) {
    TrainedModelConfig config = request.getTrainedModelConfig();
    if (!parseDefinition(config, request, listener)) return;
    boolean hasModelDefinition = config.getModelDefinition() != null;
    if (hasModelDefinition) {
        if (!validateModelDefinition(config, listener)) return;
        if (!checkModelType(config, listener)) return;
        if (!checkTargetType(config, listener)) return;
        if (!checkMinCompatibilityVersion(config, state, listener)) return;
    }
    TrainedModelConfig.Builder trainedModelConfig = prepareTrainedModelConfig(config, hasModelDefinition);
    AtomicReference<ModelPackageConfig> modelPackageConfigHolder = new AtomicReference<>();
    if (!checkModelId(state, trainedModelConfig, listener)) return;
    handleStoringModel(config, trainedModelConfig, modelPackageConfigHolder, listener, request, state);
}
/**
 * This method is responsible for parsing the definition of the trained model.
 *
 * @param config The configuration of the trained model.
 * @param request The request to put the trained model.
 * @param listener The listener to handle the response.
 * @return A boolean indicating whether the parsing was successful or not.
 */
private boolean parseDefinition(TrainedModelConfig config, PutTrainedModelAction.Request request, ActionListener<Response> listener) {
    try {
        if (!request.isDeferDefinitionDecompression()) {
            config.ensureParsedDefinition(xContentRegistry);
        }
    } catch (IOException ex) {
        listener.onFailure(ExceptionsHelper.badRequestException("Failed to parse definition for [{}]", ex, config.getModelId()));
        return false;
    }
    return true;
}
// Add the rest of the methods here...
