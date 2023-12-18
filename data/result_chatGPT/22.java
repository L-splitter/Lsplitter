/**
 * Reads the value of an annotation element.
 *
 * @param annotationVisitor the visitor to which the read value must be reported.
 * @param elementValueOffset the start offset in {@link #classFileBuffer} of the value to be read
 *     (<i>not including the value tag</i>).
 * @param elementName the name of the annotation element.
 * @param charBuffer the buffer to be used to call {@link #readUTF8}.
 * @return the offset in {@link #classFileBuffer} after the value of the annotation element.
 */
private int readElementValue(
    final AnnotationVisitor annotationVisitor,
    final int elementValueOffset,
    final String elementName,
    final char[] charBuffer) {
  int currentOffset = elementValueOffset;
  if (annotationVisitor == null) {
    return handleNullAnnotationVisitor(currentOffset, charBuffer);
  }
  return handleNonNullAnnotationVisitor(annotationVisitor, currentOffset, elementName, charBuffer);
}
/**
 * Handles the case when the annotation visitor is null.
 *
 * @param currentOffset the current offset in the class file buffer.
 * @param charBuffer the buffer to be used to call {@link #readUTF8}.
 * @return the offset in {@link #classFileBuffer} after the value of the annotation element.
 */
private int handleNullAnnotationVisitor(final int currentOffset, final char[] charBuffer) {
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
/**
 * Handles the case when the annotation visitor is not null.
 *
 * @param annotationVisitor the visitor to which the read value must be reported.
 * @param currentOffset the current offset in the class file buffer.
 * @param elementName the name of the annotation element.
 * @param charBuffer the buffer to be used to call {@link #readUTF8}.
 * @return the offset in {@link #classFileBuffer} after the value of the annotation element.
 */
private int handleNonNullAnnotationVisitor(
    final AnnotationVisitor annotationVisitor,
    final int currentOffset,
    final String elementName,
    final char[] charBuffer) {
  switch (classFileBuffer[currentOffset++] & 0xFF) {
    case 'B': // const_value_index, CONSTANT_Integer
    case 'C': // const_value_index, CONSTANT_Integer
    case 'D': // const_value_index, CONSTANT_Double
    case 'F': // const_value_index, CONSTANT_Float
    case 'I': // const_value_index, CONSTANT_Integer
    case 'J': // const_value_index, CONSTANT_Long
    case 'S': // const_value_index, CONSTANT_Integer
    case 'Z': // const_value_index, CONSTANT_Integer
    case 's': // const_value_index, CONSTANT_Utf8
    case 'e': // enum_const_value
    case 'c': // class_info
    case '@': // annotation_value
      return handlePrimitiveAndReferenceTypes(annotationVisitor, currentOffset, elementName, charBuffer);
    case '[': // array_value
      return handleArrayType(annotationVisitor, currentOffset, elementName, charBuffer);
    default:
      throw new IllegalArgumentException();
  }
}
/**
 * Handles the case when the annotation element value is of primitive or reference type.
 *
 * @param annotationVisitor the visitor to which the read value must be reported.
 * @param currentOffset the current offset in the class file buffer.
 * @param elementName the name of the annotation element.
 * @param charBuffer the buffer to be used to call {@link #readUTF8}.
 * @return the offset in {@link #classFileBuffer} after the value of the annotation element.
 */
private int handlePrimitiveAndReferenceTypes(
    final AnnotationVisitor annotationVisitor,
    final int currentOffset,
    final String elementName,
    final char[] charBuffer) {
  // The code for handling primitive and reference types goes here.
  // This is a placeholder method and needs to be implemented.
}
/**
 * Handles the case when the annotation element value is of array type.
 *
 * @param annotationVisitor the visitor to which the read value must be reported.
 * @param currentOffset the current offset in the class file buffer.
 * @param elementName the name of the annotation element.
 * @param charBuffer the buffer to be used to call {@link #readUTF8}.
 * @return the offset in {@link #classFileBuffer} after the value of the annotation element.
 */
private int handleArrayType(
    final AnnotationVisitor annotationVisitor,
    final int currentOffset,
    final String elementName,
    final char[] charBuffer) {
  // The code for handling array types goes here.
  // This is a placeholder method and needs to be implemented.
}
