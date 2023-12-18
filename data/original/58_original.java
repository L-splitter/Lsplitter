public Record getRecord(long[] ref) {
	Record res = new Record();
	String op = Long.toString(ref[1]);
	/*The newarray operation doesn't actually reference the constant pool.
	 * However, it does use "array type codes" to determine the primitive 
	 * type of the elements of the new array.  We use the cpool operator to
	 * look up the string token corresponding to the primitive type.
	 */
	if (op.equals(CPOOL_NEWARRAY)) {
		res.tag = ConstantPool.POINTER_METHOD;
		res.token = ArrayMethods.getPrimitiveArrayToken((int) ref[0]);
		DataType elementType = ArrayMethods.getArrayBaseType((int) ref[0], dtManager);
		res.type = dtManager.getPointer(elementType);
		return res;
	}
	/*arraylength instruction does not reference the constant pool */
	if (op.equals(CPOOL_ARRAYLENGTH)) {
		res.tag = ConstantPool.ARRAY_LENGTH;
		res.token = "length";
		res.type = IntegerDataType.dataType;
		return res;
	}

	AbstractConstantPoolInfoJava poolRef = constantPool[(int) ref[0]];
	int name_and_type_index;
	switch (op) {
		case CPOOL_ANEWARRAY:
		case CPOOL_NEW:
			res.tag = ConstantPool.CLASS_REFERENCE;
			int name_index = ((ConstantPoolClassInfo) poolRef).getNameIndex();
			String fullyQualifiedName =
				((ConstantPoolUtf8Info) constantPool[name_index]).getString();
			String[] parts = fullyQualifiedName.split("/");
			res.token = parts[parts.length - 1];
			StringBuilder sb = new StringBuilder();
			for (String part : parts) {
				sb.append(CategoryPath.DELIMITER_CHAR);
				sb.append(part);
			}
			DataTypePath dataPath = new DataTypePath(sb.toString(), res.token);
			res.type = new PointerDataType(dtManager.getDataType(dataPath));
			break;
		case CPOOL_CHECKCAST:
			setTypeNameInfo(poolRef, res);
			res.tag = ConstantPool.CHECK_CAST;
			PointerDataType pointerType = (PointerDataType) res.type;
			String typeName = pointerType.getDataType().getDisplayName();
			res.token = "checkcast(" + typeName + ")";
			break;
		case CPOOL_INSTANCEOF:
			res.tag = ConstantPool.INSTANCE_OF;
			res.token = "instanceof";
			setTypeNameInfo(poolRef, res);
			break;
		case CPOOL_GETFIELD:
		case CPOOL_PUTFIELD:
		case CPOOL_GETSTATIC:
		case CPOOL_PUTSTATIC:
			handlePutAndGetOps(poolRef, res, op);
			break;
		case CPOOL_INVOKEDYNAMIC:
			name_and_type_index =
				((ConstantPoolInvokeDynamicInfo) poolRef).getNameAndTypeIndex();

			fillinMethod((int) ref[0], name_and_type_index, res,
				JavaInvocationType.INVOKE_DYNAMIC);
			break;
		case CPOOL_INVOKEINTERFACE:
			name_and_type_index =
				((ConstantPoolInterfaceMethodReferenceInfo) poolRef).getNameAndTypeIndex();
			fillinMethod((int) ref[0], name_and_type_index, res,
				JavaInvocationType.INVOKE_INTERFACE);
			break;
		case CPOOL_INVOKESPECIAL:
			AbstractConstantPoolReferenceInfo refInfo =
				(AbstractConstantPoolReferenceInfo) poolRef;
			name_and_type_index = refInfo.getNameAndTypeIndex();
			fillinMethod((int) ref[0], name_and_type_index, res,
				JavaInvocationType.INVOKE_SPECIAL);
			break;
		case CPOOL_INVOKESTATIC:
			refInfo = (AbstractConstantPoolReferenceInfo) poolRef;
			name_and_type_index = refInfo.getNameAndTypeIndex();
			fillinMethod((int) ref[0], name_and_type_index, res,
				JavaInvocationType.INVOKE_STATIC);
			break;
		case CPOOL_INVOKEVIRTUAL:
			name_and_type_index =
				((ConstantPoolMethodReferenceInfo) poolRef).getNameAndTypeIndex();
			fillinMethod((int) ref[0], name_and_type_index, res,
				JavaInvocationType.INVOKE_VIRTUAL);
			break;

		//in this case, the constant pool entry can be a reference to:
		//int, float, string literal, or a symbolic reference to a class,
		//method type, or method handle
		case CPOOL_LDC:
			if (poolRef instanceof ConstantPoolIntegerInfo) {
				res.tag = ConstantPool.PRIMITIVE;
				res.token = "int";
				res.value = ((ConstantPoolIntegerInfo) poolRef).getValue();
				res.type = IntegerDataType.dataType;
			}
			else if (poolRef instanceof ConstantPoolFloatInfo) {
				res.tag = ConstantPool.PRIMITIVE;
				res.token = "float";
				res.value = ((ConstantPoolFloatInfo) poolRef).getRawBytes() & 0xffffffffL;
				res.type = FloatDataType.dataType;
			}
			else if (poolRef instanceof ConstantPoolStringInfo) {
				int string_index = ((ConstantPoolStringInfo) poolRef).getStringIndex();
				res.tag = ConstantPool.STRING_LITERAL;
				res.byteData = ((ConstantPoolUtf8Info) constantPool[string_index]).getBytes();
				res.type = DescriptorDecoder.getReferenceTypeOfDescriptor("java/lang/String",
					dtManager, false);
			}
			else if (poolRef instanceof ConstantPoolClassInfo) {
				res.tag = ConstantPool.CLASS_REFERENCE;
				name_index = ((ConstantPoolClassInfo) poolRef).getNameIndex();
				fullyQualifiedName =
					((ConstantPoolUtf8Info) constantPool[name_index]).getString();
				String className = getClassName(fullyQualifiedName);
				res.token = className + ".class";
				res.type = DescriptorDecoder.getReferenceTypeOfDescriptor(fullyQualifiedName,
					dtManager, false);
			}
			else if (poolRef instanceof ConstantPoolMethodTypeInfo) {
				res.tag = ConstantPool.POINTER_METHOD;
				name_index = ((ConstantPoolMethodTypeInfo) poolRef).getDescriptorIndex();
				res.token = ((ConstantPoolUtf8Info) constantPool[name_index]).getString();
				res.type = dtManager.getPointer(DWordDataType.dataType);
			}
			//TODO set the token?  
			else if (poolRef instanceof ConstantPoolMethodHandleInfo) {
				res.tag = ConstantPool.POINTER_METHOD;
				res.type = dtManager.getPointer(DWordDataType.dataType);
			}
			break;
		//must be a constant of type long or double
		//according to JVM spec
		case CPOOL_LDC2_W:
			if (poolRef instanceof ConstantPoolLongInfo) {
				res.tag = ConstantPool.PRIMITIVE;
				res.token = "long";
				res.value = ((ConstantPoolLongInfo) poolRef).getValue();
				res.type = LongDataType.dataType;
			}
			else {
				res.tag = ConstantPool.PRIMITIVE;
				res.token = "double";
				res.value = ((ConstantPoolDoubleInfo) poolRef).getRawBytes();
				res.type = DoubleDataType.dataType;
			}
			break;
		case CPOOL_MULTIANEWARRAY:
			res.tag = ConstantPool.CLASS_REFERENCE;
			res.type = new PointerDataType(VoidDataType.dataType);
			int nameIndex = ((ConstantPoolClassInfo) poolRef).getNameIndex();
			ConstantPoolUtf8Info utf8Info = (ConstantPoolUtf8Info) constantPool[nameIndex];
			String classNameWithSemicolon = utf8Info.getString();
			res.token = DescriptorDecoder.getTypeNameFromDescriptor(classNameWithSemicolon,
				false, false);
		default:
			break;
	}
	return res;
}