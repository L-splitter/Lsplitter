private int readElementValue(
    final AnnotationVisitor annotationVisitor,
    final int elementValueOffset,
    final String elementName,
    final char[] charBuffer) {
  int currentOffset = elementValueOffset;
  if (annotationVisitor == null) {
    switch (classFileBuffer[currentOffset] & 0xFF) {
      case 'e': // enum_const_value
        return currentOffset + 5;
      case '@': // annotation_value
        return readElementValues(null, currentOffset + 3, /* named = */ true, charBuffer);
      case '[': // array_value
        return readElementValues(null, currentOffset + 1, /* named = */ false, charBuffer);
      default:
        return currentOffset + 3;
    }
  }
  switch (classFileBuffer[currentOffset++] & 0xFF) {
    case 'B': // const_value_index, CONSTANT_Integer
      annotationVisitor.visit(
          elementName, (byte) readInt(cpInfoOffsets[readUnsignedShort(currentOffset)]));
      currentOffset += 2;
      break;
    case 'C': // const_value_index, CONSTANT_Integer
      annotationVisitor.visit(
          elementName, (char) readInt(cpInfoOffsets[readUnsignedShort(currentOffset)]));
      currentOffset += 2;
      break;
    case 'D': // const_value_index, CONSTANT_Double
    case 'F': // const_value_index, CONSTANT_Float
    case 'I': // const_value_index, CONSTANT_Integer
    case 'J': // const_value_index, CONSTANT_Long
      annotationVisitor.visit(
          elementName, readConst(readUnsignedShort(currentOffset), charBuffer));
      currentOffset += 2;
      break;
    case 'S': // const_value_index, CONSTANT_Integer
      annotationVisitor.visit(
          elementName, (short) readInt(cpInfoOffsets[readUnsignedShort(currentOffset)]));
      currentOffset += 2;
      break;

    case 'Z': // const_value_index, CONSTANT_Integer
      annotationVisitor.visit(
          elementName,
          readInt(cpInfoOffsets[readUnsignedShort(currentOffset)]) == 0
              ? Boolean.FALSE
              : Boolean.TRUE);
      currentOffset += 2;
      break;
    case 's': // const_value_index, CONSTANT_Utf8
      annotationVisitor.visit(elementName, readUTF8(currentOffset, charBuffer));
      currentOffset += 2;
      break;
    case 'e': // enum_const_value
      annotationVisitor.visitEnum(
          elementName,
          readUTF8(currentOffset, charBuffer),
          readUTF8(currentOffset + 2, charBuffer));
      currentOffset += 4;
      break;
    case 'c': // class_info
      annotationVisitor.visit(elementName, Type.getType(readUTF8(currentOffset, charBuffer)));
      currentOffset += 2;
      break;
    case '@': // annotation_value
      currentOffset =
          readElementValues(
              annotationVisitor.visitAnnotation(elementName, readUTF8(currentOffset, charBuffer)),
              currentOffset + 2,
              true,
              charBuffer);
      break;
    case '[': // array_value
      int numValues = readUnsignedShort(currentOffset);
      currentOffset += 2;
      if (numValues == 0) {
        return readElementValues(
            annotationVisitor.visitArray(elementName),
            currentOffset - 2,
            /* named = */ false,
            charBuffer);
      }
      switch (classFileBuffer[currentOffset] & 0xFF) {
        case 'B':
          byte[] byteValues = new byte[numValues];
          for (int i = 0; i < numValues; i++) {
            byteValues[i] = (byte) readInt(cpInfoOffsets[readUnsignedShort(currentOffset + 1)]);
            currentOffset += 3;
          }
          annotationVisitor.visit(elementName, byteValues);
          break;
        case 'Z':
          boolean[] booleanValues = new boolean[numValues];
          for (int i = 0; i < numValues; i++) {
            booleanValues[i] = readInt(cpInfoOffsets[readUnsignedShort(currentOffset + 1)]) != 0;
            currentOffset += 3;
          }
          annotationVisitor.visit(elementName, booleanValues);
          break;
        case 'S':
          short[] shortValues = new short[numValues];
          for (int i = 0; i < numValues; i++) {
            shortValues[i] = (short) readInt(cpInfoOffsets[readUnsignedShort(currentOffset + 1)]);
            currentOffset += 3;
          }
          annotationVisitor.visit(elementName, shortValues);
          break;
        case 'C':
          char[] charValues = new char[numValues];
          for (int i = 0; i < numValues; i++) {
            charValues[i] = (char) readInt(cpInfoOffsets[readUnsignedShort(currentOffset + 1)]);
            currentOffset += 3;
          }
          annotationVisitor.visit(elementName, charValues);
          break;
        case 'I':
          int[] intValues = new int[numValues];
          for (int i = 0; i < numValues; i++) {
            intValues[i] = readInt(cpInfoOffsets[readUnsignedShort(currentOffset + 1)]);
            currentOffset += 3;
          }
          annotationVisitor.visit(elementName, intValues);
          break;
        case 'J':
          long[] longValues = new long[numValues];
          for (int i = 0; i < numValues; i++) {
            longValues[i] = readLong(cpInfoOffsets[readUnsignedShort(currentOffset + 1)]);
            currentOffset += 3;
          }
          annotationVisitor.visit(elementName, longValues);
          break;
        case 'F':
          float[] floatValues = new float[numValues];
          for (int i = 0; i < numValues; i++) {
            floatValues[i] =
                Float.intBitsToFloat(
                    readInt(cpInfoOffsets[readUnsignedShort(currentOffset + 1)]));
            currentOffset += 3;
          }
          annotationVisitor.visit(elementName, floatValues);
          break;
        case 'D':
          double[] doubleValues = new double[numValues];
          for (int i = 0; i < numValues; i++) {
            doubleValues[i] =
                Double.longBitsToDouble(
                    readLong(cpInfoOffsets[readUnsignedShort(currentOffset + 1)]));
            currentOffset += 3;
          }
          annotationVisitor.visit(elementName, doubleValues);
          break;
        default:
          currentOffset =
              readElementValues(
                  annotationVisitor.visitArray(elementName),
                  currentOffset - 2,
                  /* named = */ false,
                  charBuffer);
          break;
      }
      break;
    default:
      throw new IllegalArgumentException();
  }
  return currentOffset;
}