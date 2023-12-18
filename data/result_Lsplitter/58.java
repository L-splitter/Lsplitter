/**
 * This method handles the operations related to the constant pool.
 * @param ref The reference to the constant pool.
 * @return The record related to the constant pool.
 */
public Record getRecord(long[] ref) {
    Record res = new Record();
    String op = Long.toString(ref[1]);
    if (op.equals(CPOOL_NEWARRAY)) {
        handleNewArrayOp(ref, res);
    } else if (op.equals(CPOOL_ARRAYLENGTH)) {
        handleArrayLengthOp(res);
    } else {
        handleOtherOps(ref, res, op);
    }
    return res;
}
/**
 * This method handles the new array operation.
 * @param ref The reference to the constant pool.
 * @param res The record to be updated.
 */
private void handleNewArrayOp(long[] ref, Record res) {
    res.tag = ConstantPool.POINTER_METHOD;
    res.token = ArrayMethods.getPrimitiveArrayToken((int) ref[0]);
    DataType elementType = ArrayMethods.getArrayBaseType((int) ref[0], dtManager);
    res.type = dtManager.getPointer(elementType);
}
/**
 * This method handles the array length operation.
 * @param res The record to be updated.
 */
private void handleArrayLengthOp(Record res) {
    res.tag = ConstantPool.ARRAY_LENGTH;
    res.token = "length";
    res.type = IntegerDataType.dataType;
}
/**
 * This method handles other operations related to the constant pool.
 * @param ref The reference to the constant pool.
 * @param res The record to be updated.
 * @param op The operation to be performed.
 */
private void handleOtherOps(long[] ref, Record res, String op) {
    AbstractConstantPoolInfoJava poolRef = constantPool[(int) ref[0]];
    int name_and_type_index;
    switch (op) {
        case CPOOL_ANEWARRAY:
        case CPOOL_NEW:
            handleNewOp(poolRef, res);
            break;
        case CPOOL_CHECKCAST:
            handleCheckCastOp(poolRef, res);
            break;
        case CPOOL_INSTANCEOF:
            handleInstanceOfOp(poolRef, res);
            break;
        case CPOOL_GETFIELD:
        case CPOOL_PUTFIELD:
        case CPOOL_GETSTATIC:
        case CPOOL_PUTSTATIC:
            handlePutAndGetOps(poolRef, res, op);
            break;
        case CPOOL_INVOKEDYNAMIC:
        case CPOOL_INVOKEINTERFACE:
        case CPOOL_INVOKESPECIAL:
        case CPOOL_INVOKESTATIC:
        case CPOOL_INVOKEVIRTUAL:
            handleInvokeOps(poolRef, res, op, (int) ref[0]);
            break;
        case CPOOL_LDC:
            handleLdcOp(poolRef, res);
            break;
        case CPOOL_LDC2_W:
            handleLdc2wOp(poolRef, res);
            break;
        case CPOOL_MULTIANEWARRAY:
            handleMultiNewArrayOp(poolRef, res);
            break;
        default:
            break;
    }
}
