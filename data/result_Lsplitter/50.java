/**
 * Disassembles a region of code.
 *
 * @param out the PrintWriter to output the disassembled code.
 */
private void disassembleRegion(PrintWriter out) {
    int alignment = currentProgram.getLanguage().getInstructionAlignment();
    Disassembler disassembler = ( Disassembler.getDisassembler(currentProgram,false,false,false,monitor,null));
    DumbMemBufferImpl memBuffer = ( new DumbMemBufferImpl(currentProgram.getMemory(),region.getMinAddress()));
    ParallelInstructionLanguageHelper helper = ( currentProgram.getLanguage().getParallelInstructionHelper());
    int cnt = 0;
    for (AddressRange range : region.getAddressRanges(true)) {
        cnt = disassembleRange(out, alignment, disassembler, memBuffer, helper, cnt, range);
    }
    Msg.info(this, "Disassembled: " + cnt + " instructions to " + outFile);
}



/**
 * Disassembles a range of addresses.
 *
 * @param out the PrintWriter to output the disassembled code.
 * @param alignment the alignment of the instructions.
 * @param disassembler the disassembler.
 * @param memBuffer the memory buffer.
 * @param helper the parallel instruction helper.
 * @param cnt the count of disassembled instructions.
 * @param range the range of addresses to disassemble.
 * @return the updated count of disassembled instructions.
 */
private int disassembleRange(PrintWriter out, int alignment, Disassembler disassembler, DumbMemBufferImpl memBuffer, ParallelInstructionLanguageHelper helper, int cnt, AddressRange range) {
    Address nextAddr = range.getMinAddress();
    InstructionBlock lastPseudoInstructionBlock = null;
    while (nextAddr != null && nextAddr.compareTo(range.getMaxAddress()) <= 0) {
        cnt = disassembleAddress(out, alignment, disassembler, memBuffer, helper, cnt, nextAddr, lastPseudoInstructionBlock);
        if (cnt == -1) {
            break;
        }
    }
    return cnt;
}
/**
 * Disassembles an address.
 *
 * @param out the PrintWriter to output the disassembled code.
 * @param alignment the alignment of the instructions.
 * @param disassembler the disassembler.
 * @param memBuffer the memory buffer.
 * @param helper the parallel instruction helper.
 * @param cnt the count of disassembled instructions.
 * @param nextAddr the address to disassemble.
 * @param lastPseudoInstructionBlock the last pseudo instruction block.
 * @return the updated count of disassembled instructions, or -1 if an error occurred.
 */
private int disassembleAddress(PrintWriter out, int alignment, Disassembler disassembler, DumbMemBufferImpl memBuffer, ParallelInstructionLanguageHelper helper, int cnt, Address nextAddr, InstructionBlock lastPseudoInstructionBlock) {
    if ((nextAddr.getOffset() % alignment) != 0) {
        nextAddr = nextAddr.next();
        return cnt;
    }
    Instruction pseudoInstruction = null;
    InstructionError error = null;
    if (lastPseudoInstructionBlock != null) {
        pseudoInstruction = lastPseudoInstructionBlock.getInstructionAt(nextAddr);
        if (pseudoInstruction == null) {
            error = lastPseudoInstructionBlock.getInstructionConflict();
            if (error != null && !nextAddr.equals(error.getInstructionAddress())) {
                error = null;
            }
        }
    }
    if (pseudoInstruction == null && error == null) {
        memBuffer.setPosition(nextAddr);
        lastPseudoInstructionBlock = disassembler.pseudoDisassembleBlock(memBuffer, null, 1);
        if (lastPseudoInstructionBlock != null) {
            pseudoInstruction = lastPseudoInstructionBlock.getInstructionAt(nextAddr);
            if (pseudoInstruction == null) {
                error = lastPseudoInstructionBlock.getInstructionConflict();
                if (error != null && !nextAddr.equals(error.getInstructionAddress())) {
                    error = null;
                }
            }
        }
    }
    try {
        cnt = printInstruction(out, alignment, memBuffer, helper, cnt, nextAddr, pseudoInstruction, error);
    } catch (AddressOutOfBoundsException e) {
        nextAddr = null; // next range
    } catch (MemoryAccessException e) {
        out.print(nextAddr.toString());
        out.println(" ERROR: " + e.getMessage());
        return -1;
    }
    return cnt;
}
/**
 * Prints an instruction.
 *
 * @param out the PrintWriter to output the disassembled code.
 * @param alignment the alignment of the instructions.
 * @param memBuffer the memory buffer.
 * @param helper the parallel instruction helper.
 * @param cnt the count of disassembled instructions.
 * @param nextAddr the address of the instruction.
 * @param pseudoInstruction the pseudo instruction.
 * @param error the instruction error.
 * @return the updated count of disassembled instructions.
 * @throws MemoryAccessException if a memory access error occurred.
 */
private int printInstruction(PrintWriter out, int alignment, DumbMemBufferImpl memBuffer, ParallelInstructionLanguageHelper helper, int cnt, Address nextAddr, Instruction pseudoInstruction, InstructionError error) throws MemoryAccessException {
    if (pseudoInstruction != null) {
        out.print(nextAddr.toString());
        out.print(" ");
        out.print(formatBytes(pseudoInstruction.getBytes()));
        out.print(" ");
        String prefix = null;
        if (helper != null) {
            prefix = helper.getMnemonicPrefix(pseudoInstruction);
        }
        if (prefix == null) {
            prefix = "    ";
        } else {
            prefix = StringUtilities.pad(prefix, ' ', -4);
        }
        out.println(prefix);
        out.println(pseudoInstruction.toString());
        nextAddr = pseudoInstruction.getMaxAddress().next();
    } else {
        out.print(nextAddr.toString());
        out.print(" ");
        out.print(formatBytes(new byte[]{memBuffer.getByte(0)}));
        out.print(" ERROR: ");
        out.println(error.getConflictMessage());
        nextAddr = nextAddr.add(alignment);
    }
    if ((++cnt % 20000) == 0) {
        Msg.info(this, "Disassembled: " + cnt);
    }
    return cnt;
}
