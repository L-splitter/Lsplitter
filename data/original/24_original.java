private void readCode(final MethodVisitor methodVisitor, final Context context, final int codeOffset) {
  int currentOffset = codeOffset;

  // Read the max_stack, max_locals and code_length fields.
  final byte[] classBuffer = classFileBuffer;
  final char[] charBuffer = context.charBuffer;
  final int maxStack = readUnsignedShort(currentOffset);
  final int maxLocals = readUnsignedShort(currentOffset + 2);
  final int codeLength = readInt(currentOffset + 4);
  currentOffset += 8;
  if (codeLength > classFileBuffer.length - currentOffset) {
    throw new IllegalArgumentException();
  }

  // Read the bytecode 'code' array to create a label for each referenced instruction.
  final int bytecodeStartOffset = currentOffset;
  final int bytecodeEndOffset = currentOffset + codeLength;
  final Label[] labels = context.currentMethodLabels = new Label[codeLength + 1];
  while (currentOffset < bytecodeEndOffset) {
    final int bytecodeOffset = currentOffset - bytecodeStartOffset;
    final int opcode = classBuffer[currentOffset] & 0xFF;
    switch (opcode) {
      case Opcodes.NOP:
      case Opcodes.ACONST_NULL:
      case Opcodes.ICONST_M1:
      case Opcodes.ICONST_0:
      case Opcodes.ICONST_1:
      case Opcodes.ICONST_2:
      case Opcodes.ICONST_3:
      case Opcodes.ICONST_4:
      case Opcodes.ICONST_5:
      case Opcodes.LCONST_0:
      case Opcodes.LCONST_1:
      case Opcodes.FCONST_0:
      case Opcodes.FCONST_1:
      case Opcodes.FCONST_2:
      case Opcodes.DCONST_0:
      case Opcodes.DCONST_1:
      case Opcodes.IALOAD:
      case Opcodes.LALOAD:
      case Opcodes.FALOAD:
      case Opcodes.DALOAD:
      case Opcodes.AALOAD:
      case Opcodes.BALOAD:
      case Opcodes.CALOAD:
      case Opcodes.SALOAD:
      case Opcodes.IASTORE:
      case Opcodes.LASTORE:
      case Opcodes.FASTORE:
      case Opcodes.DASTORE:
      case Opcodes.AASTORE:
      case Opcodes.BASTORE:
      case Opcodes.CASTORE:
      case Opcodes.SASTORE:
      case Opcodes.POP:
      case Opcodes.POP2:
      case Opcodes.DUP:
      case Opcodes.DUP_X1:
      case Opcodes.DUP_X2:
      case Opcodes.DUP2:
      case Opcodes.DUP2_X1:
      case Opcodes.DUP2_X2:
      case Opcodes.SWAP:
      case Opcodes.IADD:
      case Opcodes.LADD:
      case Opcodes.FADD:
      case Opcodes.DADD:
      case Opcodes.ISUB:
      case Opcodes.LSUB:
      case Opcodes.FSUB:
      case Opcodes.DSUB:
      case Opcodes.IMUL:
      case Opcodes.LMUL:
      case Opcodes.FMUL:
      case Opcodes.DMUL:
      case Opcodes.IDIV:
      case Opcodes.LDIV:
      case Opcodes.FDIV:
      case Opcodes.DDIV:
      case Opcodes.IREM:
      case Opcodes.LREM:
      case Opcodes.FREM:
      case Opcodes.DREM:
      case Opcodes.INEG:
      case Opcodes.LNEG:
      case Opcodes.FNEG:
      case Opcodes.DNEG:
      case Opcodes.ISHL:
      case Opcodes.LSHL:
      case Opcodes.ISHR:
      case Opcodes.LSHR:
      case Opcodes.IUSHR:
      case Opcodes.LUSHR:
      case Opcodes.IAND:
      case Opcodes.LAND:
      case Opcodes.IOR:
      case Opcodes.LOR:
      case Opcodes.IXOR:
      case Opcodes.LXOR:
      case Opcodes.I2L:
      case Opcodes.I2F:
      case Opcodes.I2D:
      case Opcodes.L2I:
      case Opcodes.L2F:
      case Opcodes.L2D:
      case Opcodes.F2I:
      case Opcodes.F2L:
      case Opcodes.F2D:
      case Opcodes.D2I:
      case Opcodes.D2L:
      case Opcodes.D2F:
      case Opcodes.I2B:
      case Opcodes.I2C:
      case Opcodes.I2S:
      case Opcodes.LCMP:
      case Opcodes.FCMPL:
      case Opcodes.FCMPG:
      case Opcodes.DCMPL:
      case Opcodes.DCMPG:
      case Opcodes.IRETURN:
      case Opcodes.LRETURN:
      case Opcodes.FRETURN:
      case Opcodes.DRETURN:
      case Opcodes.ARETURN:
      case Opcodes.RETURN:
      case Opcodes.ARRAYLENGTH:
      case Opcodes.ATHROW:
      case Opcodes.MONITORENTER:
      case Opcodes.MONITOREXIT:
      case Constants.ILOAD_0:
      case Constants.ILOAD_1:
      case Constants.ILOAD_2:
      case Constants.ILOAD_3:
      case Constants.LLOAD_0:
      case Constants.LLOAD_1:
      case Constants.LLOAD_2:
      case Constants.LLOAD_3:
      case Constants.FLOAD_0:
      case Constants.FLOAD_1:
      case Constants.FLOAD_2:
      case Constants.FLOAD_3:
      case Constants.DLOAD_0:
      case Constants.DLOAD_1:
      case Constants.DLOAD_2:
      case Constants.DLOAD_3:
      case Constants.ALOAD_0:
      case Constants.ALOAD_1:
      case Constants.ALOAD_2:
      case Constants.ALOAD_3:
      case Constants.ISTORE_0:
      case Constants.ISTORE_1:
      case Constants.ISTORE_2:
      case Constants.ISTORE_3:
      case Constants.LSTORE_0:
      case Constants.LSTORE_1:
      case Constants.LSTORE_2:
      case Constants.LSTORE_3:
      case Constants.FSTORE_0:
      case Constants.FSTORE_1:
      case Constants.FSTORE_2:
      case Constants.FSTORE_3:
      case Constants.DSTORE_0:
      case Constants.DSTORE_1:
      case Constants.DSTORE_2:
      case Constants.DSTORE_3:
      case Constants.ASTORE_0:
      case Constants.ASTORE_1:
      case Constants.ASTORE_2:
      case Constants.ASTORE_3:
        currentOffset += 1;
        break;
      case Opcodes.IFEQ:
      case Opcodes.IFNE:
      case Opcodes.IFLT:
      case Opcodes.IFGE:
      case Opcodes.IFGT:
      case Opcodes.IFLE:
      case Opcodes.IF_ICMPEQ:
      case Opcodes.IF_ICMPNE:
      case Opcodes.IF_ICMPLT:
      case Opcodes.IF_ICMPGE:
      case Opcodes.IF_ICMPGT:
      case Opcodes.IF_ICMPLE:
      case Opcodes.IF_ACMPEQ:
      case Opcodes.IF_ACMPNE:
      case Opcodes.GOTO:
      case Opcodes.JSR:
      case Opcodes.IFNULL:
      case Opcodes.IFNONNULL:
        createLabel(bytecodeOffset + readShort(currentOffset + 1), labels);
        currentOffset += 3;
        break;
      case Constants.ASM_IFEQ:
      case Constants.ASM_IFNE:
      case Constants.ASM_IFLT:
      case Constants.ASM_IFGE:
      case Constants.ASM_IFGT:
      case Constants.ASM_IFLE:
      case Constants.ASM_IF_ICMPEQ:
      case Constants.ASM_IF_ICMPNE:
      case Constants.ASM_IF_ICMPLT:
      case Constants.ASM_IF_ICMPGE:
      case Constants.ASM_IF_ICMPGT:
      case Constants.ASM_IF_ICMPLE:
      case Constants.ASM_IF_ACMPEQ:
      case Constants.ASM_IF_ACMPNE:
      case Constants.ASM_GOTO:
      case Constants.ASM_JSR:
      case Constants.ASM_IFNULL:
      case Constants.ASM_IFNONNULL:
        createLabel(bytecodeOffset + readUnsignedShort(currentOffset + 1), labels);
        currentOffset += 3;
        break;
      case Constants.GOTO_W:
      case Constants.JSR_W:
      case Constants.ASM_GOTO_W:
        createLabel(bytecodeOffset + readInt(currentOffset + 1), labels);
        currentOffset += 5;
        break;
      case Constants.WIDE:
        switch (classBuffer[currentOffset + 1] & 0xFF) {
          case Opcodes.ILOAD:
          case Opcodes.FLOAD:
          case Opcodes.ALOAD:
          case Opcodes.LLOAD:
          case Opcodes.DLOAD:
          case Opcodes.ISTORE:
          case Opcodes.FSTORE:
          case Opcodes.ASTORE:
          case Opcodes.LSTORE:
          case Opcodes.DSTORE:
          case Opcodes.RET:
            currentOffset += 4;
            break;
          case Opcodes.IINC:
            currentOffset += 6;
            break;
          default:
            throw new IllegalArgumentException();
        }
        break;
      case Opcodes.TABLESWITCH:
        // Skip 0 to 3 padding bytes.
        currentOffset += 4 - (bytecodeOffset & 3);
        // Read the default label and the number of table entries.
        createLabel(bytecodeOffset + readInt(currentOffset), labels);
        int numTableEntries = readInt(currentOffset + 8) - readInt(currentOffset + 4) + 1;
        currentOffset += 12;
        // Read the table labels.
        while (numTableEntries-- > 0) {
          createLabel(bytecodeOffset + readInt(currentOffset), labels);
          currentOffset += 4;
        }
        break;
      case Opcodes.LOOKUPSWITCH:
        // Skip 0 to 3 padding bytes.
        currentOffset += 4 - (bytecodeOffset & 3);
        // Read the default label and the number of switch cases.
        createLabel(bytecodeOffset + readInt(currentOffset), labels);
        int numSwitchCases = readInt(currentOffset + 4);
        currentOffset += 8;
        // Read the switch labels.
        while (numSwitchCases-- > 0) {
          createLabel(bytecodeOffset + readInt(currentOffset + 4), labels);
          currentOffset += 8;
        }
        break;
      case Opcodes.ILOAD:
      case Opcodes.LLOAD:
      case Opcodes.FLOAD:
      case Opcodes.DLOAD:
      case Opcodes.ALOAD:
      case Opcodes.ISTORE:
      case Opcodes.LSTORE:
      case Opcodes.FSTORE:
      case Opcodes.DSTORE:
      case Opcodes.ASTORE:
      case Opcodes.RET:
      case Opcodes.BIPUSH:
      case Opcodes.NEWARRAY:
      case Opcodes.LDC:
        currentOffset += 2;
        break;
      case Opcodes.SIPUSH:
      case Constants.LDC_W:
      case Constants.LDC2_W:
      case Opcodes.GETSTATIC:
      case Opcodes.PUTSTATIC:
      case Opcodes.GETFIELD:
      case Opcodes.PUTFIELD:
      case Opcodes.INVOKEVIRTUAL:
      case Opcodes.INVOKESPECIAL:
      case Opcodes.INVOKESTATIC:
      case Opcodes.NEW:
      case Opcodes.ANEWARRAY:
      case Opcodes.CHECKCAST:
      case Opcodes.INSTANCEOF:
      case Opcodes.IINC:
        currentOffset += 3;
        break;
      case Opcodes.INVOKEINTERFACE:
      case Opcodes.INVOKEDYNAMIC:
        currentOffset += 5;
        break;
      case Opcodes.MULTIANEWARRAY:
        currentOffset += 4;
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  // Read the 'exception_table_length' and 'exception_table' field to create a label for each
  // referenced instruction, and to make methodVisitor visit the corresponding try catch blocks.
  int exceptionTableLength = readUnsignedShort(currentOffset);
  currentOffset += 2;
  while (exceptionTableLength-- > 0) {
    Label start = createLabel(readUnsignedShort(currentOffset), labels);
    Label end = createLabel(readUnsignedShort(currentOffset + 2), labels);
    Label handler = createLabel(readUnsignedShort(currentOffset + 4), labels);
    String catchType = readUTF8(cpInfoOffsets[readUnsignedShort(currentOffset + 6)], charBuffer);
    currentOffset += 8;
    methodVisitor.visitTryCatchBlock(start, end, handler, catchType);
  }

  // Read the Code attributes to create a label for each referenced instruction (the variables
  // are ordered as in Section 4.7 of the JVMS). Attribute offsets exclude the
  // attribute_name_index and attribute_length fields.
  // - The offset of the current 'stack_map_frame' in the StackMap[Table] attribute, or 0.
  // Initially, this is the offset of the first 'stack_map_frame' entry. Then this offset is
  // updated after each stack_map_frame is read.
  int stackMapFrameOffset = 0;
  // - The end offset of the StackMap[Table] attribute, or 0.
  int stackMapTableEndOffset = 0;
  // - Whether the stack map frames are compressed (i.e. in a StackMapTable) or not.
  boolean compressedFrames = true;
  // - The offset of the LocalVariableTable attribute, or 0.
  int localVariableTableOffset = 0;
  // - The offset of the LocalVariableTypeTable attribute, or 0.
  int localVariableTypeTableOffset = 0;
  // - The offset of each 'type_annotation' entry in the RuntimeVisibleTypeAnnotations
  // attribute, or null.
  int[] visibleTypeAnnotationOffsets = null;
  // - The offset of each 'type_annotation' entry in the RuntimeInvisibleTypeAnnotations
  // attribute, or null.
  int[] invisibleTypeAnnotationOffsets = null;
  // - The non standard attributes (linked with their {@link Attribute#nextAttribute} field).
  //   This list in the <i>reverse order</i> or their order in the ClassFile structure.
  Attribute attributes = null;

  int attributesCount = readUnsignedShort(currentOffset);
  currentOffset += 2;
  while (attributesCount-- > 0) {
    // Read the attribute_info's attribute_name and attribute_length fields.
    String attributeName = readUTF8(currentOffset, charBuffer);
    int attributeLength = readInt(currentOffset + 2);
    currentOffset += 6;
    if (Constants.LOCAL_VARIABLE_TABLE.equals(attributeName)) {
      if ((context.parsingOptions & SKIP_DEBUG) == 0) {
        localVariableTableOffset = currentOffset;
        // Parse the attribute to find the corresponding (debug only) labels.
        int currentLocalVariableTableOffset = currentOffset;
        int localVariableTableLength = readUnsignedShort(currentLocalVariableTableOffset);
        currentLocalVariableTableOffset += 2;
        while (localVariableTableLength-- > 0) {
          int startPc = readUnsignedShort(currentLocalVariableTableOffset);
          createDebugLabel(startPc, labels);
          int length = readUnsignedShort(currentLocalVariableTableOffset + 2);
          createDebugLabel(startPc + length, labels);
          // Skip the name_index, descriptor_index and index fields (2 bytes each).
          currentLocalVariableTableOffset += 10;
        }
      }
    } else if (Constants.LOCAL_VARIABLE_TYPE_TABLE.equals(attributeName)) {
      localVariableTypeTableOffset = currentOffset;
      // Here we do not extract the labels corresponding to the attribute content. We assume they
      // are the same or a subset of those of the LocalVariableTable attribute.
    } else if (Constants.LINE_NUMBER_TABLE.equals(attributeName)) {
      if ((context.parsingOptions & SKIP_DEBUG) == 0) {
        // Parse the attribute to find the corresponding (debug only) labels.
        int currentLineNumberTableOffset = currentOffset;
        int lineNumberTableLength = readUnsignedShort(currentLineNumberTableOffset);
        currentLineNumberTableOffset += 2;
        while (lineNumberTableLength-- > 0) {
          int startPc = readUnsignedShort(currentLineNumberTableOffset);
          int lineNumber = readUnsignedShort(currentLineNumberTableOffset + 2);
          currentLineNumberTableOffset += 4;
          createDebugLabel(startPc, labels);
          labels[startPc].addLineNumber(lineNumber);
        }
      }
    } else if (Constants.RUNTIME_VISIBLE_TYPE_ANNOTATIONS.equals(attributeName)) {
      visibleTypeAnnotationOffsets =
          readTypeAnnotations(methodVisitor, context, currentOffset, /* visible = */ true);
      // Here we do not extract the labels corresponding to the attribute content. This would
      // require a full parsing of the attribute, which would need to be repeated when parsing
      // the bytecode instructions (see below). Instead, the content of the attribute is read one
      // type annotation at a time (i.e. after a type annotation has been visited, the next type
      // annotation is read), and the labels it contains are also extracted one annotation at a
      // time. This assumes that type annotations are ordered by increasing bytecode offset.
    } else if (Constants.RUNTIME_INVISIBLE_TYPE_ANNOTATIONS.equals(attributeName)) {
      invisibleTypeAnnotationOffsets =
          readTypeAnnotations(methodVisitor, context, currentOffset, /* visible = */ false);
      // Same comment as above for the RuntimeVisibleTypeAnnotations attribute.
    } else if (Constants.STACK_MAP_TABLE.equals(attributeName)) {
      if ((context.parsingOptions & SKIP_FRAMES) == 0) {
        stackMapFrameOffset = currentOffset + 2;
        stackMapTableEndOffset = currentOffset + attributeLength;
      }
      // Here we do not extract the labels corresponding to the attribute content. This would
      // require a full parsing of the attribute, which would need to be repeated when parsing
      // the bytecode instructions (see below). Instead, the content of the attribute is read one
      // frame at a time (i.e. after a frame has been visited, the next frame is read), and the
      // labels it contains are also extracted one frame at a time. Thanks to the ordering of
      // frames, having only a "one frame lookahead" is not a problem, i.e. it is not possible to
      // see an offset smaller than the offset of the current instruction and for which no Label
      // exist. Except for UNINITIALIZED type offsets. We solve this by parsing the stack map
      // table without a full decoding (see below).
    } else if ("StackMap".equals(attributeName)) {
      if ((context.parsingOptions & SKIP_FRAMES) == 0) {
        stackMapFrameOffset = currentOffset + 2;
        stackMapTableEndOffset = currentOffset + attributeLength;
        compressedFrames = false;
      }
      // IMPORTANT! Here we assume that the frames are ordered, as in the StackMapTable attribute,
      // although this is not guaranteed by the attribute format. This allows an incremental
      // extraction of the labels corresponding to this attribute (see the comment above for the
      // StackMapTable attribute).
    } else {
      Attribute attribute =
          readAttribute(
              context.attributePrototypes,
              attributeName,
              currentOffset,
              attributeLength,
              charBuffer,
              codeOffset,
              labels);
      attribute.nextAttribute = attributes;
      attributes = attribute;
    }
    currentOffset += attributeLength;
  }

  // Initialize the context fields related to stack map frames, and generate the first
  // (implicit) stack map frame, if needed.
  final boolean expandFrames = (context.parsingOptions & EXPAND_FRAMES) != 0;
  if (stackMapFrameOffset != 0) {
    // The bytecode offset of the first explicit frame is not offset_delta + 1 but only
    // offset_delta. Setting the implicit frame offset to -1 allows us to use of the
    // "offset_delta + 1" rule in all cases.
    context.currentFrameOffset = -1;
    context.currentFrameType = 0;
    context.currentFrameLocalCount = 0;
    context.currentFrameLocalCountDelta = 0;
    context.currentFrameLocalTypes = new Object[maxLocals];
    context.currentFrameStackCount = 0;
    context.currentFrameStackTypes = new Object[maxStack];
    if (expandFrames) {
      computeImplicitFrame(context);
    }
    // Find the labels for UNINITIALIZED frame types. Instead of decoding each element of the
    // stack map table, we look for 3 consecutive bytes that "look like" an UNINITIALIZED type
    // (tag ITEM_Uninitialized, offset within bytecode bounds, NEW instruction at this offset).
    // We may find false positives (i.e. not real UNINITIALIZED types), but this should be rare,
    // and the only consequence will be the creation of an unneeded label. This is better than
    // creating a label for each NEW instruction, and faster than fully decoding the whole stack
    // map table.
    for (int offset = stackMapFrameOffset; offset < stackMapTableEndOffset - 2; ++offset) {
      if (classBuffer[offset] == Frame.ITEM_UNINITIALIZED) {
        int potentialBytecodeOffset = readUnsignedShort(offset + 1);
        if (potentialBytecodeOffset >= 0
            && potentialBytecodeOffset < codeLength
            && (classBuffer[bytecodeStartOffset + potentialBytecodeOffset] & 0xFF)
                == Opcodes.NEW) {
          createLabel(potentialBytecodeOffset, labels);
        }
      }
    }
  }
  if (expandFrames && (context.parsingOptions & EXPAND_ASM_INSNS) != 0) {
    // Expanding the ASM specific instructions can introduce F_INSERT frames, even if the method
    // does not currently have any frame. These inserted frames must be computed by simulating the
    // effect of the bytecode instructions, one by one, starting from the implicit first frame.
    // For this, MethodWriter needs to know maxLocals before the first instruction is visited. To
    // ensure this, we visit the implicit first frame here (passing only maxLocals - the rest is
    // computed in MethodWriter).
    methodVisitor.visitFrame(Opcodes.F_NEW, maxLocals, null, 0, null);
  }

  // Visit the bytecode instructions. First, introduce state variables for the incremental parsing
  // of the type annotations.

  // Index of the next runtime visible type annotation to read (in the
  // visibleTypeAnnotationOffsets array).
  int currentVisibleTypeAnnotationIndex = 0;
  // The bytecode offset of the next runtime visible type annotation to read, or -1.
  int currentVisibleTypeAnnotationBytecodeOffset =
      getTypeAnnotationBytecodeOffset(visibleTypeAnnotationOffsets, 0);
  // Index of the next runtime invisible type annotation to read (in the
  // invisibleTypeAnnotationOffsets array).
  int currentInvisibleTypeAnnotationIndex = 0;
  // The bytecode offset of the next runtime invisible type annotation to read, or -1.
  int currentInvisibleTypeAnnotationBytecodeOffset =
      getTypeAnnotationBytecodeOffset(invisibleTypeAnnotationOffsets, 0);

  // Whether a F_INSERT stack map frame must be inserted before the current instruction.
  boolean insertFrame = false;

  // The delta to subtract from a goto_w or jsr_w opcode to get the corresponding goto or jsr
  // opcode, or 0 if goto_w and jsr_w must be left unchanged (i.e. when expanding ASM specific
  // instructions).
  final int wideJumpOpcodeDelta =
      (context.parsingOptions & EXPAND_ASM_INSNS) == 0 ? Constants.WIDE_JUMP_OPCODE_DELTA : 0;

  currentOffset = bytecodeStartOffset;
  while (currentOffset < bytecodeEndOffset) {
    final int currentBytecodeOffset = currentOffset - bytecodeStartOffset;
    readBytecodeInstructionOffset(currentBytecodeOffset);

    // Visit the label and the line number(s) for this bytecode offset, if any.
    Label currentLabel = labels[currentBytecodeOffset];
    if (currentLabel != null) {
      currentLabel.accept(methodVisitor, (context.parsingOptions & SKIP_DEBUG) == 0);
    }

    // Visit the stack map frame for this bytecode offset, if any.
    while (stackMapFrameOffset != 0
        && (context.currentFrameOffset == currentBytecodeOffset
            || context.currentFrameOffset == -1)) {
      // If there is a stack map frame for this offset, make methodVisitor visit it, and read the
      // next stack map frame if there is one.
      if (context.currentFrameOffset != -1) {
        if (!compressedFrames || expandFrames) {
          methodVisitor.visitFrame(
              Opcodes.F_NEW,
              context.currentFrameLocalCount,
              context.currentFrameLocalTypes,
              context.currentFrameStackCount,
              context.currentFrameStackTypes);
        } else {
          methodVisitor.visitFrame(
              context.currentFrameType,
              context.currentFrameLocalCountDelta,
              context.currentFrameLocalTypes,
              context.currentFrameStackCount,
              context.currentFrameStackTypes);
        }
        // Since there is already a stack map frame for this bytecode offset, there is no need to
        // insert a new one.
        insertFrame = false;
      }
      if (stackMapFrameOffset < stackMapTableEndOffset) {
        stackMapFrameOffset =
            readStackMapFrame(stackMapFrameOffset, compressedFrames, expandFrames, context);
      } else {
        stackMapFrameOffset = 0;
      }
    }

    // Insert a stack map frame for this bytecode offset, if requested by setting insertFrame to
    // true during the previous iteration. The actual frame content is computed in MethodWriter.
    if (insertFrame) {
      if ((context.parsingOptions & EXPAND_FRAMES) != 0) {
        methodVisitor.visitFrame(Constants.F_INSERT, 0, null, 0, null);
      }
      insertFrame = false;
    }

    // Visit the instruction at this bytecode offset.
    int opcode = classBuffer[currentOffset] & 0xFF;
    switch (opcode) {
      case Opcodes.NOP:
      case Opcodes.ACONST_NULL:
      case Opcodes.ICONST_M1:
      case Opcodes.ICONST_0:
      case Opcodes.ICONST_1:
      case Opcodes.ICONST_2:
      case Opcodes.ICONST_3:
      case Opcodes.ICONST_4:
      case Opcodes.ICONST_5:
      case Opcodes.LCONST_0:
      case Opcodes.LCONST_1:
      case Opcodes.FCONST_0:
      case Opcodes.FCONST_1:
      case Opcodes.FCONST_2:
      case Opcodes.DCONST_0:
      case Opcodes.DCONST_1:
      case Opcodes.IALOAD:
      case Opcodes.LALOAD:
      case Opcodes.FALOAD:
      case Opcodes.DALOAD:
      case Opcodes.AALOAD:
      case Opcodes.BALOAD:
      case Opcodes.CALOAD:
      case Opcodes.SALOAD:
      case Opcodes.IASTORE:
      case Opcodes.LASTORE:
      case Opcodes.FASTORE:
      case Opcodes.DASTORE:
      case Opcodes.AASTORE:
      case Opcodes.BASTORE:
      case Opcodes.CASTORE:
      case Opcodes.SASTORE:
      case Opcodes.POP:
      case Opcodes.POP2:
      case Opcodes.DUP:
      case Opcodes.DUP_X1:
      case Opcodes.DUP_X2:
      case Opcodes.DUP2:
      case Opcodes.DUP2_X1:
      case Opcodes.DUP2_X2:
      case Opcodes.SWAP:
      case Opcodes.IADD:
      case Opcodes.LADD:
      case Opcodes.FADD:
      case Opcodes.DADD:
      case Opcodes.ISUB:
      case Opcodes.LSUB:
      case Opcodes.FSUB:
      case Opcodes.DSUB:
      case Opcodes.IMUL:
      case Opcodes.LMUL:
      case Opcodes.FMUL:
      case Opcodes.DMUL:
      case Opcodes.IDIV:
      case Opcodes.LDIV:
      case Opcodes.FDIV:
      case Opcodes.DDIV:
      case Opcodes.IREM:
      case Opcodes.LREM:
      case Opcodes.FREM:
      case Opcodes.DREM:
      case Opcodes.INEG:
      case Opcodes.LNEG:
      case Opcodes.FNEG:
      case Opcodes.DNEG:
      case Opcodes.ISHL:
      case Opcodes.LSHL:
      case Opcodes.ISHR:
      case Opcodes.LSHR:
      case Opcodes.IUSHR:
      case Opcodes.LUSHR:
      case Opcodes.IAND:
      case Opcodes.LAND:
      case Opcodes.IOR:
      case Opcodes.LOR:
      case Opcodes.IXOR:
      case Opcodes.LXOR:
      case Opcodes.I2L:
      case Opcodes.I2F:
      case Opcodes.I2D:
      case Opcodes.L2I:
      case Opcodes.L2F:
      case Opcodes.L2D:
      case Opcodes.F2I:
      case Opcodes.F2L:
      case Opcodes.F2D:
      case Opcodes.D2I:
      case Opcodes.D2L:
      case Opcodes.D2F:
      case Opcodes.I2B:
      case Opcodes.I2C:
      case Opcodes.I2S:
      case Opcodes.LCMP:
      case Opcodes.FCMPL:
      case Opcodes.FCMPG:
      case Opcodes.DCMPL:
      case Opcodes.DCMPG:
      case Opcodes.IRETURN:
      case Opcodes.LRETURN:
      case Opcodes.FRETURN:
      case Opcodes.DRETURN:
      case Opcodes.ARETURN:
      case Opcodes.RETURN:
      case Opcodes.ARRAYLENGTH:
      case Opcodes.ATHROW:
      case Opcodes.MONITORENTER:
      case Opcodes.MONITOREXIT:
        methodVisitor.visitInsn(opcode);
        currentOffset += 1;
        break;
      case Constants.ILOAD_0:
      case Constants.ILOAD_1:
      case Constants.ILOAD_2:
      case Constants.ILOAD_3:
      case Constants.LLOAD_0:
      case Constants.LLOAD_1:
      case Constants.LLOAD_2:
      case Constants.LLOAD_3:
      case Constants.FLOAD_0:
      case Constants.FLOAD_1:
      case Constants.FLOAD_2:
      case Constants.FLOAD_3:
      case Constants.DLOAD_0:
      case Constants.DLOAD_1:
      case Constants.DLOAD_2:
      case Constants.DLOAD_3:
      case Constants.ALOAD_0:
      case Constants.ALOAD_1:
      case Constants.ALOAD_2:
      case Constants.ALOAD_3:
        opcode -= Constants.ILOAD_0;
        methodVisitor.visitVarInsn(Opcodes.ILOAD + (opcode >> 2), opcode & 0x3);
        currentOffset += 1;
        break;
      case Constants.ISTORE_0:
      case Constants.ISTORE_1:
      case Constants.ISTORE_2:
      case Constants.ISTORE_3:
      case Constants.LSTORE_0:
      case Constants.LSTORE_1:
      case Constants.LSTORE_2:
      case Constants.LSTORE_3:
      case Constants.FSTORE_0:
      case Constants.FSTORE_1:
      case Constants.FSTORE_2:
      case Constants.FSTORE_3:
      case Constants.DSTORE_0:
      case Constants.DSTORE_1:
      case Constants.DSTORE_2:
      case Constants.DSTORE_3:
      case Constants.ASTORE_0:
      case Constants.ASTORE_1:
      case Constants.ASTORE_2:
      case Constants.ASTORE_3:
        opcode -= Constants.ISTORE_0;
        methodVisitor.visitVarInsn(Opcodes.ISTORE + (opcode >> 2), opcode & 0x3);
        currentOffset += 1;
        break;
      case Opcodes.IFEQ:
      case Opcodes.IFNE:
      case Opcodes.IFLT:
      case Opcodes.IFGE:
      case Opcodes.IFGT:
      case Opcodes.IFLE:
      case Opcodes.IF_ICMPEQ:
      case Opcodes.IF_ICMPNE:
      case Opcodes.IF_ICMPLT:
      case Opcodes.IF_ICMPGE:
      case Opcodes.IF_ICMPGT:
      case Opcodes.IF_ICMPLE:
      case Opcodes.IF_ACMPEQ:
      case Opcodes.IF_ACMPNE:
      case Opcodes.GOTO:
      case Opcodes.JSR:
      case Opcodes.IFNULL:
      case Opcodes.IFNONNULL:
        methodVisitor.visitJumpInsn(
            opcode, labels[currentBytecodeOffset + readShort(currentOffset + 1)]);
        currentOffset += 3;
        break;
      case Constants.GOTO_W:
      case Constants.JSR_W:
        methodVisitor.visitJumpInsn(
            opcode - wideJumpOpcodeDelta,
            labels[currentBytecodeOffset + readInt(currentOffset + 1)]);
        currentOffset += 5;
        break;
      case Constants.ASM_IFEQ:
      case Constants.ASM_IFNE:
      case Constants.ASM_IFLT:
      case Constants.ASM_IFGE:
      case Constants.ASM_IFGT:
      case Constants.ASM_IFLE:
      case Constants.ASM_IF_ICMPEQ:
      case Constants.ASM_IF_ICMPNE:
      case Constants.ASM_IF_ICMPLT:
      case Constants.ASM_IF_ICMPGE:
      case Constants.ASM_IF_ICMPGT:
      case Constants.ASM_IF_ICMPLE:
      case Constants.ASM_IF_ACMPEQ:
      case Constants.ASM_IF_ACMPNE:
      case Constants.ASM_GOTO:
      case Constants.ASM_JSR:
      case Constants.ASM_IFNULL:
      case Constants.ASM_IFNONNULL:
        {
          // A forward jump with an offset > 32767. In this case we automatically replace ASM_GOTO
          // with GOTO_W, ASM_JSR with JSR_W and ASM_IFxxx <l> with IFNOTxxx <L> GOTO_W <l> L:...,
          // where IFNOTxxx is the "opposite" opcode of ASMS_IFxxx (e.g. IFNE for ASM_IFEQ) and
          // where <L> designates the instruction just after the GOTO_W.
          // First, change the ASM specific opcodes ASM_IFEQ ... ASM_JSR, ASM_IFNULL and
          // ASM_IFNONNULL to IFEQ ... JSR, IFNULL and IFNONNULL.
          opcode =
              opcode < Constants.ASM_IFNULL
                  ? opcode - Constants.ASM_OPCODE_DELTA
                  : opcode - Constants.ASM_IFNULL_OPCODE_DELTA;
          Label target = labels[currentBytecodeOffset + readUnsignedShort(currentOffset + 1)];
          if (opcode == Opcodes.GOTO || opcode == Opcodes.JSR) {
            // Replace GOTO with GOTO_W and JSR with JSR_W.
            methodVisitor.visitJumpInsn(opcode + Constants.WIDE_JUMP_OPCODE_DELTA, target);
          } else {
            // Compute the "opposite" of opcode. This can be done by flipping the least
            // significant bit for IFNULL and IFNONNULL, and similarly for IFEQ ... IF_ACMPEQ
            // (with a pre and post offset by 1).
            opcode = opcode < Opcodes.GOTO ? ((opcode + 1) ^ 1) - 1 : opcode ^ 1;
            Label endif = createLabel(currentBytecodeOffset + 3, labels);
            methodVisitor.visitJumpInsn(opcode, endif);
            methodVisitor.visitJumpInsn(Constants.GOTO_W, target);
            // endif designates the instruction just after GOTO_W, and is visited as part of the
            // next instruction. Since it is a jump target, we need to insert a frame here.
            insertFrame = true;
          }
          currentOffset += 3;
          break;
        }
      case Constants.ASM_GOTO_W:
        // Replace ASM_GOTO_W with GOTO_W.
        methodVisitor.visitJumpInsn(
            Constants.GOTO_W, labels[currentBytecodeOffset + readInt(currentOffset + 1)]);
        // The instruction just after is a jump target (because ASM_GOTO_W is used in patterns
        // IFNOTxxx <L> ASM_GOTO_W <l> L:..., see MethodWriter), so we need to insert a frame
        // here.
        insertFrame = true;
        currentOffset += 5;
        break;
      case Constants.WIDE:
        opcode = classBuffer[currentOffset + 1] & 0xFF;
        if (opcode == Opcodes.IINC) {
          methodVisitor.visitIincInsn(
              readUnsignedShort(currentOffset + 2), readShort(currentOffset + 4));
          currentOffset += 6;
        } else {
          methodVisitor.visitVarInsn(opcode, readUnsignedShort(currentOffset + 2));
          currentOffset += 4;
        }
        break;
      case Opcodes.TABLESWITCH:
        {
          // Skip 0 to 3 padding bytes.
          currentOffset += 4 - (currentBytecodeOffset & 3);
          // Read the instruction.
          Label defaultLabel = labels[currentBytecodeOffset + readInt(currentOffset)];
          int low = readInt(currentOffset + 4);
          int high = readInt(currentOffset + 8);
          currentOffset += 12;
          Label[] table = new Label[high - low + 1];
          for (int i = 0; i < table.length; ++i) {
            table[i] = labels[currentBytecodeOffset + readInt(currentOffset)];
            currentOffset += 4;
          }
          methodVisitor.visitTableSwitchInsn(low, high, defaultLabel, table);
          break;
        }
      case Opcodes.LOOKUPSWITCH:
        {
          // Skip 0 to 3 padding bytes.
          currentOffset += 4 - (currentBytecodeOffset & 3);
          // Read the instruction.
          Label defaultLabel = labels[currentBytecodeOffset + readInt(currentOffset)];
          int numPairs = readInt(currentOffset + 4);
          currentOffset += 8;
          int[] keys = new int[numPairs];
          Label[] values = new Label[numPairs];
          for (int i = 0; i < numPairs; ++i) {
            keys[i] = readInt(currentOffset);
            values[i] = labels[currentBytecodeOffset + readInt(currentOffset + 4)];
            currentOffset += 8;
          }
          methodVisitor.visitLookupSwitchInsn(defaultLabel, keys, values);
          break;
        }
      case Opcodes.ILOAD:
      case Opcodes.LLOAD:
      case Opcodes.FLOAD:
      case Opcodes.DLOAD:
      case Opcodes.ALOAD:
      case Opcodes.ISTORE:
      case Opcodes.LSTORE:
      case Opcodes.FSTORE:
      case Opcodes.DSTORE:
      case Opcodes.ASTORE:
      case Opcodes.RET:
        methodVisitor.visitVarInsn(opcode, classBuffer[currentOffset + 1] & 0xFF);
        currentOffset += 2;
        break;
      case Opcodes.BIPUSH:
      case Opcodes.NEWARRAY:
        methodVisitor.visitIntInsn(opcode, classBuffer[currentOffset + 1]);
        currentOffset += 2;
        break;
      case Opcodes.SIPUSH:
        methodVisitor.visitIntInsn(opcode, readShort(currentOffset + 1));
        currentOffset += 3;
        break;
      case Opcodes.LDC:
        methodVisitor.visitLdcInsn(readConst(classBuffer[currentOffset + 1] & 0xFF, charBuffer));
        currentOffset += 2;
        break;
      case Constants.LDC_W:
      case Constants.LDC2_W:
        methodVisitor.visitLdcInsn(readConst(readUnsignedShort(currentOffset + 1), charBuffer));
        currentOffset += 3;
        break;
      case Opcodes.GETSTATIC:
      case Opcodes.PUTSTATIC:
      case Opcodes.GETFIELD:
      case Opcodes.PUTFIELD:
      case Opcodes.INVOKEVIRTUAL:
      case Opcodes.INVOKESPECIAL:
      case Opcodes.INVOKESTATIC:
      case Opcodes.INVOKEINTERFACE:
        {
          int cpInfoOffset = cpInfoOffsets[readUnsignedShort(currentOffset + 1)];
          int nameAndTypeCpInfoOffset = cpInfoOffsets[readUnsignedShort(cpInfoOffset + 2)];
          String owner = readClass(cpInfoOffset, charBuffer);
          String name = readUTF8(nameAndTypeCpInfoOffset, charBuffer);
          String descriptor = readUTF8(nameAndTypeCpInfoOffset + 2, charBuffer);
          if (opcode < Opcodes.INVOKEVIRTUAL) {
            methodVisitor.visitFieldInsn(opcode, owner, name, descriptor);
          } else {
            boolean isInterface =
                classBuffer[cpInfoOffset - 1] == Symbol.CONSTANT_INTERFACE_METHODREF_TAG;
            methodVisitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
          }
          if (opcode == Opcodes.INVOKEINTERFACE) {
            currentOffset += 5;
          } else {
            currentOffset += 3;
          }
          break;
        }
      case Opcodes.INVOKEDYNAMIC:
        {
          int cpInfoOffset = cpInfoOffsets[readUnsignedShort(currentOffset + 1)];
          int nameAndTypeCpInfoOffset = cpInfoOffsets[readUnsignedShort(cpInfoOffset + 2)];
          String name = readUTF8(nameAndTypeCpInfoOffset, charBuffer);
          String descriptor = readUTF8(nameAndTypeCpInfoOffset + 2, charBuffer);
          int bootstrapMethodOffset = bootstrapMethodOffsets[readUnsignedShort(cpInfoOffset)];
          Handle handle =
              (Handle) readConst(readUnsignedShort(bootstrapMethodOffset), charBuffer);
          Object[] bootstrapMethodArguments =
              new Object[readUnsignedShort(bootstrapMethodOffset + 2)];
          bootstrapMethodOffset += 4;
          for (int i = 0; i < bootstrapMethodArguments.length; i++) {
            bootstrapMethodArguments[i] =
                readConst(readUnsignedShort(bootstrapMethodOffset), charBuffer);
            bootstrapMethodOffset += 2;
          }
          methodVisitor.visitInvokeDynamicInsn(
              name, descriptor, handle, bootstrapMethodArguments);
          currentOffset += 5;
          break;
        }
      case Opcodes.NEW:
      case Opcodes.ANEWARRAY:
      case Opcodes.CHECKCAST:
      case Opcodes.INSTANCEOF:
        methodVisitor.visitTypeInsn(opcode, readClass(currentOffset + 1, charBuffer));
        currentOffset += 3;
        break;
      case Opcodes.IINC:
        methodVisitor.visitIincInsn(
            classBuffer[currentOffset + 1] & 0xFF, classBuffer[currentOffset + 2]);
        currentOffset += 3;
        break;
      case Opcodes.MULTIANEWARRAY:
        methodVisitor.visitMultiANewArrayInsn(
            readClass(currentOffset + 1, charBuffer), classBuffer[currentOffset + 3] & 0xFF);
        currentOffset += 4;
        break;
      default:
        throw new AssertionError();
    }

    // Visit the runtime visible instruction annotations, if any.
    while (visibleTypeAnnotationOffsets != null
        && currentVisibleTypeAnnotationIndex < visibleTypeAnnotationOffsets.length
        && currentVisibleTypeAnnotationBytecodeOffset <= currentBytecodeOffset) {
      if (currentVisibleTypeAnnotationBytecodeOffset == currentBytecodeOffset) {
        // Parse the target_type, target_info and target_path fields.
        int currentAnnotationOffset =
            readTypeAnnotationTarget(
                context, visibleTypeAnnotationOffsets[currentVisibleTypeAnnotationIndex]);
        // Parse the type_index field.
        String annotationDescriptor = readUTF8(currentAnnotationOffset, charBuffer);
        currentAnnotationOffset += 2;
        // Parse num_element_value_pairs and element_value_pairs and visit these values.
        readElementValues(
            methodVisitor.visitInsnAnnotation(
                context.currentTypeAnnotationTarget,
                context.currentTypeAnnotationTargetPath,
                annotationDescriptor,
                /* visible = */ true),
            currentAnnotationOffset,
            /* named = */ true,
            charBuffer);
      }
      currentVisibleTypeAnnotationBytecodeOffset =
          getTypeAnnotationBytecodeOffset(
              visibleTypeAnnotationOffsets, ++currentVisibleTypeAnnotationIndex);
    }

    // Visit the runtime invisible instruction annotations, if any.
    while (invisibleTypeAnnotationOffsets != null
        && currentInvisibleTypeAnnotationIndex < invisibleTypeAnnotationOffsets.length
        && currentInvisibleTypeAnnotationBytecodeOffset <= currentBytecodeOffset) {
      if (currentInvisibleTypeAnnotationBytecodeOffset == currentBytecodeOffset) {
        // Parse the target_type, target_info and target_path fields.
        int currentAnnotationOffset =
            readTypeAnnotationTarget(
                context, invisibleTypeAnnotationOffsets[currentInvisibleTypeAnnotationIndex]);
        // Parse the type_index field.
        String annotationDescriptor = readUTF8(currentAnnotationOffset, charBuffer);
        currentAnnotationOffset += 2;
        // Parse num_element_value_pairs and element_value_pairs and visit these values.
        readElementValues(
            methodVisitor.visitInsnAnnotation(
                context.currentTypeAnnotationTarget,
                context.currentTypeAnnotationTargetPath,
                annotationDescriptor,
                /* visible = */ false),
            currentAnnotationOffset,
            /* named = */ true,
            charBuffer);
      }
      currentInvisibleTypeAnnotationBytecodeOffset =
          getTypeAnnotationBytecodeOffset(
              invisibleTypeAnnotationOffsets, ++currentInvisibleTypeAnnotationIndex);
    }
  }
  if (labels[codeLength] != null) {
    methodVisitor.visitLabel(labels[codeLength]);
  }

  // Visit LocalVariableTable and LocalVariableTypeTable attributes.
  if (localVariableTableOffset != 0 && (context.parsingOptions & SKIP_DEBUG) == 0) {
    // The (start_pc, index, signature_index) fields of each entry of the LocalVariableTypeTable.
    int[] typeTable = null;
    if (localVariableTypeTableOffset != 0) {
      typeTable = new int[readUnsignedShort(localVariableTypeTableOffset) * 3];
      currentOffset = localVariableTypeTableOffset + 2;
      int typeTableIndex = typeTable.length;
      while (typeTableIndex > 0) {
        // Store the offset of 'signature_index', and the value of 'index' and 'start_pc'.
        typeTable[--typeTableIndex] = currentOffset + 6;
        typeTable[--typeTableIndex] = readUnsignedShort(currentOffset + 8);
        typeTable[--typeTableIndex] = readUnsignedShort(currentOffset);
        currentOffset += 10;
      }
    }
    int localVariableTableLength = readUnsignedShort(localVariableTableOffset);
    currentOffset = localVariableTableOffset + 2;
    while (localVariableTableLength-- > 0) {
      int startPc = readUnsignedShort(currentOffset);
      int length = readUnsignedShort(currentOffset + 2);
      String name = readUTF8(currentOffset + 4, charBuffer);
      String descriptor = readUTF8(currentOffset + 6, charBuffer);
      int index = readUnsignedShort(currentOffset + 8);
      currentOffset += 10;
      String signature = null;
      if (typeTable != null) {
        for (int i = 0; i < typeTable.length; i += 3) {
          if (typeTable[i] == startPc && typeTable[i + 1] == index) {
            signature = readUTF8(typeTable[i + 2], charBuffer);
            break;
          }
        }
      }
      methodVisitor.visitLocalVariable(
          name, descriptor, signature, labels[startPc], labels[startPc + length], index);
    }
  }

  // Visit the local variable type annotations of the RuntimeVisibleTypeAnnotations attribute.
  if (visibleTypeAnnotationOffsets != null) {
    for (int typeAnnotationOffset : visibleTypeAnnotationOffsets) {
      int targetType = readByte(typeAnnotationOffset);
      if (targetType == TypeReference.LOCAL_VARIABLE
          || targetType == TypeReference.RESOURCE_VARIABLE) {
        // Parse the target_type, target_info and target_path fields.
        currentOffset = readTypeAnnotationTarget(context, typeAnnotationOffset);
        // Parse the type_index field.
        String annotationDescriptor = readUTF8(currentOffset, charBuffer);
        currentOffset += 2;
        // Parse num_element_value_pairs and element_value_pairs and visit these values.
        readElementValues(
            methodVisitor.visitLocalVariableAnnotation(
                context.currentTypeAnnotationTarget,
                context.currentTypeAnnotationTargetPath,
                context.currentLocalVariableAnnotationRangeStarts,
                context.currentLocalVariableAnnotationRangeEnds,
                context.currentLocalVariableAnnotationRangeIndices,
                annotationDescriptor,
                /* visible = */ true),
            currentOffset,
            /* named = */ true,
            charBuffer);
      }
    }
  }

  // Visit the local variable type annotations of the RuntimeInvisibleTypeAnnotations attribute.
  if (invisibleTypeAnnotationOffsets != null) {
    for (int typeAnnotationOffset : invisibleTypeAnnotationOffsets) {
      int targetType = readByte(typeAnnotationOffset);
      if (targetType == TypeReference.LOCAL_VARIABLE
          || targetType == TypeReference.RESOURCE_VARIABLE) {
        // Parse the target_type, target_info and target_path fields.
        currentOffset = readTypeAnnotationTarget(context, typeAnnotationOffset);
        // Parse the type_index field.
        String annotationDescriptor = readUTF8(currentOffset, charBuffer);
        currentOffset += 2;
        // Parse num_element_value_pairs and element_value_pairs and visit these values.
        readElementValues(
            methodVisitor.visitLocalVariableAnnotation(
                context.currentTypeAnnotationTarget,
                context.currentTypeAnnotationTargetPath,
                context.currentLocalVariableAnnotationRangeStarts,
                context.currentLocalVariableAnnotationRangeEnds,
                context.currentLocalVariableAnnotationRangeIndices,
                annotationDescriptor,
                /* visible = */ false),
            currentOffset,
            /* named = */ true,
            charBuffer);
      }
    }
  }

  // Visit the non standard attributes.
  while (attributes != null) {
    // Copy and reset the nextAttribute field so that it can also be used in MethodWriter.
    Attribute nextAttribute = attributes.nextAttribute;
    attributes.nextAttribute = null;
    methodVisitor.visitAttribute(attributes);
    attributes = nextAttribute;
  }

  // Visit the max stack and max locals values.
  methodVisitor.visitMaxs(maxStack, maxLocals);
}