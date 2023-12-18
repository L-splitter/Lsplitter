/**
 * This method is used to update the function.
 * @param callingConvention The calling convention of the function.
 * @param returnVar The return variable of the function.
 * @param newParams The new parameters of the function.
 * @param updateType The update type of the function.
 * @param force The force flag of the function.
 * @param source The source of the function.
 * @throws DuplicateNameException If there is a duplicate name.
 * @throws InvalidInputException If there is an invalid input.
 */
public void updateFunction(String callingConvention, Variable returnVar,
		List<? extends Variable> newParams, FunctionUpdateType updateType, boolean force,
		SourceType source) throws DuplicateNameException, InvalidInputException {
	manager.lock.acquire();
	try {
		startUpdate();
		checkDeleted();
		
  if (thunkedFunction != null) {
    thunkedFunction.updateFunction(callingConvention,returnVar,newParams,updateType,force,source);
    return;
  }

		loadVariables();
		purgeBadVariables();
		boolean useCustomStorage = (updateType == FunctionUpdateType.CUSTOM_STORAGE);
		setCustomVariableStorage(useCustomStorage);
		
  if (callingConvention != null) {
    setCallingConvention(callingConvention);
  }

		callingConvention = getCallingConventionName();
		
  if (returnVar == null) {
    returnVar=returnParam;
  }
 else   if (returnVar.isUniqueVariable()) {
    throw new IllegalArgumentException("Invalid return specified: UniqueVariable not allowed");
  }

		DataType returnType = returnVar.getDataType();
		VariableStorage returnStorage = returnVar.getVariableStorage();
		if (!useCustomStorage) {
			handleNonCustomStorage(callingConvention, newParams, returnVar, returnType, returnStorage, updateType);
		}
		// Update return data type
		getReturn().setDataType(returnType, returnStorage, true, source);
		Set<String> nonParamNames = getNonParamNames();
		handleNewParams(newParams, useCustomStorage, nonParamNames);
		if (useCustomStorage) {
			checkStorageConflicts(newParams, force);
		}
		repopulateParamsList(newParams, useCustomStorage, oldParams);
		if (source.isHigherPriorityThan(getStoredSignatureSource())) {
			setSignatureSource(source);
		}
		// assign dynamic storage
		updateParametersAndReturn();
		manager.functionChanged(this, ChangeManager.FUNCTION_CHANGED_PARAMETERS);
	}
	finally {
		frame.setInvalid();
		endUpdate();
		manager.lock.release();
	}
}



/**
 * This method is used to get the non parameter names.
 * @return The set of non parameter names.
 */
private Set<String> getNonParamNames() {
	Set<String> nonParamNames = new HashSet<>();
	for (Symbol s : program.getSymbolTable().getSymbols(this)) {
		if (s.getSource() != SourceType.DEFAULT &&
			s.getSymbolType() != SymbolType.PARAMETER) {
			nonParamNames.add(s.getName());
		}
	}
	return nonParamNames;
}
/**
 * This method is used to handle the new parameters.
 * @param newParams The new parameters of the function.
 * @param useCustomStorage The flag to use custom storage.
 * @param nonParamNames The set of non parameter names.
 */
private void handleNewParams(List<? extends Variable> newParams, boolean useCustomStorage, Set<String> nonParamNames) {
	// Must ensure that all names do not conflict and that variable types are
	// resolved to this program so that they have the proper sizes
	List<Variable> clonedParams = new ArrayList<>();
	for (int i = 0; i < newParams.size(); i++) {
		Variable p = newParams.get(i);
		if (!useCustomStorage && (p instanceof AutoParameterImpl)) {
			continue;
		}
		if (p.isUniqueVariable()) {
			throw new IllegalArgumentException(
				"Invalid parameter specified: UniqueVariable not allowed");
		}
		checkForParameterNameConflict(p, newParams, nonParamNames);
		clonedParams.add(getResolvedVariable(p, false, !useCustomStorage));
	}
	newParams = clonedParams;
}
/**
 * This method is used to repopulate the parameters list.
 * @param newParams The new parameters of the function.
 * @param useCustomStorage The flag to use custom storage.
 * @param oldParams The old parameters of the function.
 */
private void repopulateParamsList(List<? extends Variable> newParams, boolean useCustomStorage, List<ParameterDB> oldParams) {
	// Repopulate params list
	oldParams = params;
	params = new ArrayList<>();
	// Clear current param names
	for (ParameterDB param : oldParams) {
		param.setName(null, SourceType.DEFAULT);
	}
	int newParamIndex = 0;
	// Reassign old parameters if possible
	while (newParamIndex < oldParams.size() && newParamIndex < newParams.size()) {
		ParameterDB oldParam = oldParams.get(newParamIndex);
		Variable newParam = newParams.get(newParamIndex++);
		DataType dt = (newParam instanceof Parameter && !useCustomStorage)
				? ((Parameter) newParam).getFormalDataType()
				: newParam.getDataType();
		oldParam.setName(newParam.getName(), newParam.getSource());
		oldParam.setStorageAndDataType(newParam.getVariableStorage(), dt);
		oldParam.setComment(newParam.getComment());
		params.add(oldParam); // re-add to list
	}
	// Remove unused old parameters
	for (int i = newParamIndex; i < oldParams.size(); i++) {
		ParameterDB oldParam = oldParams.get(i);
		Symbol s = oldParam.getSymbol();
		symbolMap.remove(s);
		s.delete();
	}
	// Append new parameters if needed
	SymbolManager symbolMgr = program.getSymbolTable();
	for (int i = newParamIndex; i < newParams.size(); i++) {
		Variable newParam = newParams.get(i);
		DataType dt = (newParam instanceof Parameter && !useCustomStorage)
				? ((Parameter) newParam).getFormalDataType()
				: newParam.getDataType();
		VariableStorage storage = useCustomStorage ? newParam.getVariableStorage()
				: VariableStorage.UNASSIGNED_STORAGE;
		String name = newParam.getName();
		if (name == null || name.length() == 0) {
			name = SymbolUtilities.getDefaultParamName(i);
		}
		VariableSymbolDB s = symbolMgr.createVariableSymbol(name, this,
			SymbolType.PARAMETER, i, storage, newParam.getSource());
		s.setStorageAndDataType(storage, dt);
		ParameterDB paramDb = new ParameterDB(this, s);
		paramDb.setComment(newParam.getComment());
		params.add(i, paramDb);
		symbolMap.put(s, paramDb);
	}
}
/**
 * This method is used to handle non custom storage.
 * @param callingConvention The calling convention of the function.
 * @param newParams The new parameters of the function.
 * @param returnVar The return variable of the function.
 * @param returnType The return type of the function.
 * @param returnStorage The return storage of the function.
 * @param updateType The update type of the function.
 */
private void handleNonCustomStorage(String callingConvention, List<? extends Variable> newParams, Variable returnVar, DataType returnType, VariableStorage returnStorage, FunctionUpdateType updateType) {
	// remove auto params and forced-indirect return
	newParams = new ArrayList<Variable>(newParams); // copy for edit
	boolean thisParamRemoved =
		removeExplicitThisParameter(newParams, callingConvention);
	if (removeExplicitReturnStorageParameter(newParams)) {
		returnVar = revertIndirectParameter(returnVar, true);
	}
	if (returnVar instanceof Parameter) {
		returnType = ((Parameter) returnVar).getFormalDataType();
	}
	returnStorage = VariableStorage.UNASSIGNED_STORAGE;
	if (updateType == FunctionUpdateType.DYNAMIC_STORAGE_ALL_PARAMS &&
		!thisParamRemoved &&
		CompilerSpec.CALLING_CONVENTION_thiscall.equals(callingConvention) &&
		newParams.size() != 0) {
		// Attempt to remove inferred unnamed 'this' parameter
		// WARNING! This is a bit of a hack - not sure how to account for what may be auto-params
		// within a list of parameters computed via analysis
		Variable firstParam = newParams.get(0);
		if (firstParam.getSource() == SourceType.DEFAULT &&
			firstParam.getLength() == program.getDefaultPointerSize()) {
			newParams.remove(0);
		}
	}
}
