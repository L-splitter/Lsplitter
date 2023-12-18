protected List<SqlParameter> reconcileParameters(List<SqlParameter> parameters) {
	CallMetaDataProvider provider = obtainMetaDataProvider();

	final List<SqlParameter> declaredReturnParams = new ArrayList<>();
	final Map<String, SqlParameter> declaredParams = new LinkedHashMap<>();
	boolean returnDeclared = false;
	List<String> outParamNames = new ArrayList<>();
	List<String> metaDataParamNames = new ArrayList<>();

	// Get the names of the meta-data parameters
	for (CallParameterMetaData meta : provider.getCallParameterMetaData()) {
		if (!meta.isReturnParameter()) {
			metaDataParamNames.add(lowerCase(meta.getParameterName()));
		}
	}

	// Separate implicit return parameters from explicit parameters...
	for (SqlParameter param : parameters) {
		if (param.isResultsParameter()) {
			declaredReturnParams.add(param);
		}
		else {
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
	setOutParameterNames(outParamNames);

	List<SqlParameter> workParams = new ArrayList<>(declaredReturnParams);
	if (!provider.isProcedureColumnMetaDataUsed()) {
		workParams.addAll(declaredParams.values());
		return workParams;
	}

	Map<String, String> limitedInParamNamesMap = CollectionUtils.newHashMap(this.limitedInParameterNames.size());
	for (String limitedParamName : this.limitedInParameterNames) {
		limitedInParamNamesMap.put(lowerCase(provider.parameterNameToUse(limitedParamName)), limitedParamName);
	}

	for (CallParameterMetaData meta : provider.getCallParameterMetaData()) {
		String paramName = meta.getParameterName();
		String paramNameToCheck = null;
		if (paramName != null) {
			paramNameToCheck = lowerCase(provider.parameterNameToUse(paramName));
		}
		String paramNameToUse = provider.parameterNameToUse(paramName);
		if (declaredParams.containsKey(paramNameToCheck) || (meta.isReturnParameter() && returnDeclared)) {
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
				}
				else {
					this.actualFunctionReturnName = param.getName();
				}
			}
			else {
				param = declaredParams.get(paramNameToCheck);
			}
			if (param != null) {
				workParams.add(param);
				if (logger.isDebugEnabled()) {
					logger.debug("Using declared parameter for '" +
							(paramNameToUse != null ? paramNameToUse : getFunctionReturnName()) + "'");
				}
			}
		}
		else {
			if (meta.isReturnParameter()) {
				// DatabaseMetaData.procedureColumnReturn or possibly procedureColumnResult
				if (!isFunction() && !isReturnValueRequired() && paramName != null &&
						provider.byPassReturnParameter(paramName)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Bypassing meta-data return parameter for '" + paramName + "'");
					}
				}
				else {
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
			else {
				if (paramNameToUse == null) {
					paramNameToUse = "";
				}
				if (meta.getParameterType() == DatabaseMetaData.procedureColumnOut) {
					workParams.add(provider.createDefaultOutParameter(paramNameToUse, meta));
					outParamNames.add(paramNameToUse);
					if (logger.isDebugEnabled()) {
						logger.debug("Added meta-data out parameter for '" + paramNameToUse + "'");
					}
				}
				else if (meta.getParameterType() == DatabaseMetaData.procedureColumnInOut) {
					workParams.add(provider.createDefaultInOutParameter(paramNameToUse, meta));
					outParamNames.add(paramNameToUse);
					if (logger.isDebugEnabled()) {
						logger.debug("Added meta-data in-out parameter for '" + paramNameToUse + "'");
					}
				}
				else {
					// DatabaseMetaData.procedureColumnIn or possibly procedureColumnUnknown
					if (this.limitedInParameterNames.isEmpty() ||
							limitedInParamNamesMap.containsKey(lowerCase(paramNameToUse))) {
						workParams.add(provider.createDefaultInParameter(paramNameToUse, meta));
						if (logger.isDebugEnabled()) {
							logger.debug("Added meta-data in parameter for '" + paramNameToUse + "'");
						}
					}
					else {
						if (logger.isDebugEnabled()) {
							logger.debug("Limited set of parameters " + limitedInParamNamesMap.keySet() +
									" skipped parameter for '" + paramNameToUse + "'");
						}
					}
				}
			}
		}
	}

	return workParams;
}