public void grabFromFunction(boolean includeDefaultNames) {
	ArrayList<String> mergeNames = null;
	Function dbFunction = func.getFunction();
	Variable locals[] = dbFunction.getLocalVariables();
	for (Variable local : locals) {
		if (!local.isValid()) {
			// exclude locals which don't have valid storage
			continue;
		}
		DataType dt = local.getDataType();
		boolean istypelock = true;
		boolean isnamelock = true;
		if (Undefined.isUndefined(dt)) {
			istypelock = false;
		}
		String name = local.getName();
		if (name.length() > 2 && name.charAt(name.length() - 2) == '$') {
			// An indication of names like "name", "name@1", "name@2"
			if (name.charAt(name.length() - 1) == '1') {
				if (mergeNames == null) {
					mergeNames = new ArrayList<>();
				}
				mergeNames.add(name);
			}
		}

		VariableStorage storage = local.getVariableStorage();
		long id = 0;
		Symbol symbol = local.getSymbol();
		if (symbol != null) {
			id = symbol.getID();
		}
		HighSymbol sym;
		if (storage.isHashStorage()) {
			Address defAddr = dbFunction.getEntryPoint().addWrap(local.getFirstUseOffset());
			sym =
				newDynamicSymbol(id, name, dt, storage.getFirstVarnode().getOffset(), defAddr);
		}
		else {
			Address defAddr = null;
			int addrType = storage.getFirstVarnode().getAddress().getAddressSpace().getType();
			if (addrType != AddressSpace.TYPE_STACK && addrType != AddressSpace.TYPE_RAM) {
				defAddr = dbFunction.getEntryPoint().addWrap(local.getFirstUseOffset());
			}
			sym = newMappedSymbol(id, name, dt, storage, defAddr, -1);
		}
		sym.setTypeLock(istypelock);
		sym.setNameLock(isnamelock);
	}

	Parameter[] p = dbFunction.getParameters();
	boolean lock = (dbFunction.getSignatureSource() != SourceType.DEFAULT);

	Address pcaddr = dbFunction.getEntryPoint();
	pcaddr = pcaddr.subtractWrap(1);

	List<HighSymbol> paramList = new ArrayList<>();
	for (int i = 0; i < p.length; ++i) {
		Parameter var = p[i];
		if (!var.isValid()) {
			// TODO: exclude parameters which don't have valid storage ??
			continue;
		}
		DataType dt = var.getDataType();
		String name = var.getName();
		if (name.length() > 2 && name.charAt(name.length() - 2) == '$') {
			// An indication of names like "name", "name@1", "name@2"
			if (name.charAt(name.length() - 1) == '1') {
				if (mergeNames == null) {
					mergeNames = new ArrayList<>();
				}
				mergeNames.add(name);
			}
		}
		VariableStorage storage = var.getVariableStorage();
		Address resAddr = storage.isStackStorage() ? null : pcaddr;
		long id = 0;
		Symbol symbol = var.getSymbol();
		if (symbol != null) {
			id = symbol.getID();
		}
		HighSymbol paramSymbol = newMappedSymbol(id, name, dt, storage, resAddr, i);
		paramList.add(paramSymbol);
		boolean namelock = true;
		if (!includeDefaultNames) {
			namelock = isUserDefinedName(name);
		}
		paramSymbol.setNameLock(namelock);
		paramSymbol.setTypeLock(lock);
	}

	paramSymbols = new HighSymbol[paramList.size()];
	paramList.toArray(paramSymbols);
	Arrays.sort(paramSymbols, PARAM_SYMBOL_SLOT_COMPARATOR);

	grabEquates(dbFunction);
	grabMerges(mergeNames);
}