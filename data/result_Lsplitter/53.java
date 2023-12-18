/**
 * This method grabs the local variables from the function and processes them.
 */
private void processLocalVariables() {
    Variable locals[] = dbFunction.getLocalVariables();
    for (Variable local : locals) {
        if (!local.isValid()) {
            // exclude locals which don't have valid storage
            continue;
        }
        processVariable(local);
    }
}
/**
 * This method processes a single variable.
 */
private void processVariable(Variable local) {
    DataType dt = local.getDataType();
    boolean istypelock = !Undefined.isUndefined(dt);
    String name = local.getName();
    checkNameAndAddToMergeNames(name);
    VariableStorage storage = local.getVariableStorage();
    long id = getSymbolId(local);
    HighSymbol sym = createHighSymbol(local, dt, name, storage, id);
    sym.setTypeLock(istypelock);
    sym.setNameLock(true);
}
/**
 * This method checks the name of the variable and adds it to mergeNames if necessary.
 */
private void checkNameAndAddToMergeNames(String name) {
    if (name.length() > 2 && name.charAt(name.length() - 2) == '$') {
        // An indication of names like "name", "name@1", "name@2"
        if (name.charAt(name.length() - 1) == '1') {
            if (mergeNames == null) {
                mergeNames = new ArrayList<>();
            }
            mergeNames.add(name);
        }
    }
}
/**
 * This method gets the id of the symbol associated with the variable.
 */
private long getSymbolId(Variable local) {
    Symbol symbol = local.getSymbol();
    return symbol != null ? symbol.getID() : 0;
}
/**
 * This method creates a HighSymbol for the variable.
 */
private HighSymbol createHighSymbol(Variable local, DataType dt, String name, VariableStorage storage, long id) {
    HighSymbol sym;
    if (storage.isHashStorage()) {
        Address defAddr = dbFunction.getEntryPoint().addWrap(local.getFirstUseOffset());
        sym = newDynamicSymbol(id, name, dt, storage.getFirstVarnode().getOffset(), defAddr);
    } else {
        Address defAddr = getDefAddr(local, storage);
        sym = newMappedSymbol(id, name, dt, storage, defAddr, -1);
    }
    return sym;
}
/**
 * This method gets the defAddr for the variable.
 */
private Address getDefAddr(Variable local, VariableStorage storage) {
    Address defAddr = null;
    int addrType = storage.getFirstVarnode().getAddress().getAddressSpace().getType();
    if (addrType != AddressSpace.TYPE_STACK && addrType != AddressSpace.TYPE_RAM) {
        defAddr = dbFunction.getEntryPoint().addWrap(local.getFirstUseOffset());
    }
    return defAddr;
}
/**
 * This method processes the parameters of the function.
 */
private void processParameters() {
    Parameter[] p = dbFunction.getParameters();
    boolean lock = (dbFunction.getSignatureSource() != SourceType.DEFAULT);
    Address pcaddr = dbFunction.getEntryPoint();
    pcaddr = pcaddr.subtractWrap(1);
    List<HighSymbol> paramList = new ArrayList<>();
    for (int i = 0; i < p.length; ++i) {
        processParameter(p[i], lock, pcaddr, paramList, i);
    }
    paramSymbols = new HighSymbol[paramList.size()];
    paramList.toArray(paramSymbols);
    Arrays.sort(paramSymbols, PARAM_SYMBOL_SLOT_COMPARATOR);
}
/**
 * This method processes a single parameter.
 */
private void processParameter(Parameter var, boolean lock, Address pcaddr, List<HighSymbol> paramList, int i) {
    if (!var.isValid()) {
        // TODO: exclude parameters which don't have valid storage ??
        return;
    }
    DataType dt = var.getDataType();
    String name = var.getName();
    checkNameAndAddToMergeNames(name);
    VariableStorage storage = var.getVariableStorage();
    Address resAddr = storage.isStackStorage() ? null : pcaddr;
    long id = getSymbolId(var);
    HighSymbol paramSymbol = newMappedSymbol(id, name, dt, storage, resAddr, i);
    paramList.add(paramSymbol);
    boolean namelock = !includeDefaultNames || isUserDefinedName(name);
    paramSymbol.setNameLock(namelock);
    paramSymbol.setTypeLock(lock);
}
public void grabFromFunction(boolean includeDefaultNames) {
    mergeNames = null;
    dbFunction = func.getFunction();
    processLocalVariables();
    processParameters();
    grabEquates(dbFunction);
    grabMerges(mergeNames);
}
