private void disassembleRegion(PrintWriter out) {

	int alignment = currentProgram.getLanguage().getInstructionAlignment();

	Disassembler disassembler =
		Disassembler.getDisassembler(currentProgram, false, false, false, monitor, null);

	DumbMemBufferImpl memBuffer =
		new DumbMemBufferImpl(currentProgram.getMemory(), region.getMinAddress());

	ParallelInstructionLanguageHelper helper =
		currentProgram.getLanguage().getParallelInstructionHelper();

	int cnt = 0;

	for (AddressRange range : region.getAddressRanges(true)) {

		Address nextAddr = range.getMinAddress();

		InstructionBlock lastPseudoInstructionBlock = null;

		while (nextAddr != null && nextAddr.compareTo(range.getMaxAddress()) <= 0) {

			if ((nextAddr.getOffset() % alignment) != 0) {
				nextAddr = nextAddr.next();
				continue;
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
				lastPseudoInstructionBlock =
					disassembler.pseudoDisassembleBlock(memBuffer, null, 1);
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
					}
					else {
						prefix = StringUtilities.pad(prefix, ' ', -4);
					}
					out.println(prefix);

					out.println(pseudoInstruction.toString());

					nextAddr = pseudoInstruction.getMaxAddress().next();
				}
				else {
					out.print(nextAddr.toString());
					out.print(" ");
					out.print(formatBytes(new byte[] { memBuffer.getByte(0) }));
					out.print(" ERROR: ");
					out.println(error.getConflictMessage());

					nextAddr = nextAddr.add(alignment);
				}

				if ((++cnt % 20000) == 0) {
					Msg.info(this, "Disassembled: " + cnt);
				}
			}
			catch (AddressOutOfBoundsException e) {
				nextAddr = null; // next range
			}
			catch (MemoryAccessException e) {
				out.print(nextAddr.toString());
				out.println(" ERROR: " + e.getMessage());
				break;
			}
		}
	}
	Msg.info(this, "Disassembled: " + cnt + " instructions to " + outFile);
}