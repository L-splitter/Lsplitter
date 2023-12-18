/**
 * Reconciles the parameters.
 *
 * @param parameters the parameters
 * @return the list of reconciled parameters
 */
protected List<SqlParameter> reconcileParameters(List<SqlParameter> parameters) {
    CallMetaDataProvider provider = obtainMetaDataProvider();
    final List<SqlParameter> declaredReturnParams = new ArrayList<>();
    final Map<String, SqlParameter> declaredParams = new LinkedHashMap<>();
    boolean returnDeclared = false;
    List<String> outParamNames = new ArrayList<>();
    List<String> metaDataParamNames = getMetaDataParamNames(provider);
    returnDeclared = separateReturnAndExplicitParams(parameters, provider, declaredReturnParams, declaredParams, outParamNames, returnDeclared, metaDataParamNames);
    setOutParameterNames(outParamNames);
    List<SqlParameter> workParams = new ArrayList<>(declaredReturnParams);
    if (!provider.isProcedureColumnMetaDataUsed()) {
        workParams.addAll(declaredParams.values());
        return workParams;
    }
    Map<String, String> limitedInParamNamesMap = getLimitedInParamNamesMap(provider);
    processMetaDataParams(provider, declaredParams, returnDeclared, workParams, limitedInParamNamesMap, outParamNames);
    return workParams;
}
/**
 * Gets the meta-data parameter names.
 *
 * @param provider the meta-data provider
 * @return the list of meta-data parameter names
 */
private List<String> getMetaDataParamNames(CallMetaDataProvider provider) {
    List<String> metaDataParamNames = new ArrayList<>();
    for (CallParameterMetaData meta : provider.getCallParameterMetaData()) {
        if (!meta.isReturnParameter()) {
            metaDataParamNames.add(lowerCase(meta.getParameterName()));
        }
    }
    return metaDataParamNames;
}
/**
 * Separates return parameters from explicit parameters.
 *
 * @param parameters the parameters
 * @param provider the meta-data provider
 * @param declaredReturnParams the declared return parameters
 * @param declaredParams the declared parameters
 * @param outParamNames the out parameter names
 * @param returnDeclared the return declared flag
 * @param metaDataParamNames the meta-data parameter names
 * @return the updated return declared flag
 */
private boolean separateReturnAndExplicitParams(List<SqlParameter> parameters, CallMetaDataProvider provider, List<SqlParameter> declaredReturnParams, Map<String, SqlParameter> declaredParams, List<String> outParamNames, boolean returnDeclared, List<String> metaDataParamNames) {
    for (SqlParameter param : parameters) {
        if (param.isResultsParameter()) {
            declaredReturnParams.add(param);
        } else {
            String paramName = param.getName();
            if (paramName == null) {
                throw new IllegalArgumentException("Anonymous parameters not supported for calls - " +
                        "please specify a name for the parameter of SQL type " + param.getSqlType());
            }
            String paramNameToMatch = lowerCase(provider.parameterNameToUse(paramName));
            declaredParams.put(paramNameToMatch, param);
            if (param instanceof SqlOutParameter) {
                outParamNames.add(paramName);
                if (isFunction() && !metaDataParamNames.contains(paramNameToMatch) && !returnDeclared) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Using declared out parameter '" + paramName +
                                "' for function return value");
                    }
                    this.actualFunctionReturnName = paramName;
                    returnDeclared = true;
                }
            }
        }
    }
    return returnDeclared;
}
/**
 * Gets the limited in parameter names map.
 *
 * @param provider the meta-data provider
 * @return the map of limited in parameter names
 */
private Map<String, String> getLimitedInParamNamesMap(CallMetaDataProvider provider) {
    Map<String, String> limitedInParamNamesMap = CollectionUtils.newHashMap(this.limitedInParameterNames.size());
    for (String limitedParamName : this.limitedInParameterNames) {
        limitedInParamNamesMap.put(lowerCase(provider.parameterNameToUse(limitedParamName)), limitedParamName);
    }
    return limitedInParamNamesMap;
}
/**
 * Processes the meta-data parameters.
 *
 * @param provider the meta-data provider
 * @param declaredParams the declared parameters
 * @param returnDeclared the return declared flag
 * @param workParams the work parameters
 * @param limitedInParamNamesMap the map of limited in parameter names
 * @param outParamNames the out parameter names
 */
private void processMetaDataParams(CallMetaDataProvider provider, Map<String, SqlParameter> declaredParams, boolean returnDeclared, List<SqlParameter> workParams, Map<String, String> limitedInParamNamesMap, List<String> outParamNames) {
    for (CallParameterMetaData meta : provider.getCallParameterMetaData()) {
        String paramName = meta.getParameterName();
        String paramNameToCheck = null;
        if (paramName != null) {
            paramNameToCheck = lowerCase(provider.parameterNameToUse(paramName));
        }
        String paramNameToUse = provider.parameterNameToUse(paramName);
        if (declaredParams.containsKey(paramNameToCheck) || (meta.isReturnParameter() && returnDeclared)) {
            SqlParameter param = getDeclaredParam(provider, declaredParams, meta, paramNameToCheck);
            if (param != null) {
                workParams.add(param);
                if (logger.isDebugEnabled()) {
                    logger.debug("Using declared parameter for '" +
                            (paramNameToUse != null ? paramNameToUse : getFunctionReturnName()) + "'");
                }
            }
        } else {
            
  if (meta.isReturnParameter()) {
    handleReturnParam(provider,meta,workParams,outParamNames,paramName,paramNameToUse);
  }
 else {
    handleInOutParam(provider,meta,workParams,limitedInParamNamesMap,outParamNames,paramNameToUse);
  }

        }
    }
}
/**
 * Gets the declared parameter.
 *
 * @param provider the meta-data provider
 * @param declaredParams the declared parameters
 * @param meta the meta-data
 * @param paramNameToCheck the parameter name to check
 * @return the declared parameter
 */
private SqlParameter getDeclaredParam(CallMetaDataProvider provider, Map<String, SqlParameter> declaredParams, CallParameterMetaData meta, String paramNameToCheck) {
    SqlParameter param;
    if (meta.isReturnParameter()) {
        param = declaredParams.get(getFunctionReturnName());
        if (param == null && !getOutParameterNames().isEmpty()) {
            param = declaredParams.get(getOutParameterNames().get(0).toLowerCase());
        }
        if (param == null) {
            throw new InvalidDataAccessApiUsageException(
                    "Unable to locate declared parameter for function return value - " +
                            " add an SqlOutParameter with name '" + getFunctionReturnName() + "'");
        } else {
            this.actualFunctionReturnName = param.getName();
        }
    } else {
        param = declaredParams.get(paramNameToCheck);
    }
    return param;
}

/**
 * Handles return parameters.
 *
 * @param provider the meta-data provider
 * @param meta the meta-data
 * @param workParams the work parameters
 * @param outParamNames the out parameter names
 * @param paramName the parameter name
 * @param paramNameToUse the parameter name to use
 */
private void handleReturnParam(CallMetaDataProvider provider, CallParameterMetaData meta, List<SqlParameter> workParams, List<String> outParamNames, String paramName, String paramNameToUse) {
    // DatabaseMetaData.procedureColumnReturn or possibly procedureColumnResult
    if (!isFunction() && !isReturnValueRequired() && paramName != null &&
            provider.byPassReturnParameter(paramName)) {
        if (logger.isDebugEnabled()) {
            logger.debug("Bypassing meta-data return parameter for '" + paramName + "'");
        }
    } else {
        String returnNameToUse =
                (StringUtils.hasLength(paramNameToUse) ? paramNameToUse : getFunctionReturnName());
        workParams.add(provider.createDefaultOutParameter(returnNameToUse, meta));
        if (isFunction()) {
            this.actualFunctionReturnName = returnNameToUse;
            outParamNames.add(returnNameToUse);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Added meta-data return parameter for '" + returnNameToUse + "'");
        }
    }
}
/**
 * Handles in-out parameters.
 *
 * @param provider the meta-data provider
 * @param meta the meta-data
 * @param workParams the work parameters
 * @param limitedInParamNamesMap the map of limited in parameter names
 * @param outParamNames the out parameter names
 * @param paramNameToUse the parameter name to use
 */
private void handleInOutParam(CallMetaDataProvider provider, CallParameterMetaData meta, List<SqlParameter> workParams, Map<String, String> limitedInParamNamesMap, List<String> outParamNames, String paramNameToUse) {
    if (paramNameToUse == null) {
        paramNameToUse = "";
    }
    if (meta.getParameterType() == DatabaseMetaData.procedureColumnOut) {
        workParams.add(provider.createDefaultOutParameter(paramNameToUse, meta));
        outParamNames.add(paramNameToUse);
        if (logger.isDebugEnabled()) {
            logger.debug("Added meta-data out parameter for '" + paramNameToUse + "'");
        }
    } else if (meta.getParameterType() == DatabaseMetaData.procedureColumnInOut) {
        workParams.add(provider.createDefaultInOutParameter(paramNameToUse, meta));
        outParamNames.add(paramNameToUse);
        if (logger.isDebugEnabled()) {
            logger.debug("Added meta-data in-out parameter for '" + paramNameToUse + "'");
        }
    } else {
        // DatabaseMetaData.procedureColumnIn or possibly procedureColumnUnknown
        if (this.limitedInParameterNames.isEmpty() ||
                limitedInParamNamesMap.containsKey(lowerCase(paramNameToUse))) {
            workParams.add(provider.createDefaultInParameter(paramNameToUse, meta));
            if (logger.isDebugEnabled()) {
                logger.debug("Added meta-data in parameter for '" + paramNameToUse + "'");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Limited set of parameters " + limitedInParamNamesMap.keySet() +
                        " skipped parameter for '" + paramNameToUse + "'");
            }
        }
    }
}
