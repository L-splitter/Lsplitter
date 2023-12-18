public AddressSet flowConstants(final Program program, Address flowStart,
		AddressSetView flowSet, final SymbolicPropogator symEval, final TaskMonitor monitor)
		throws CancelledException {

	// follow all flows building up context
	// use context to fill out addresses on certain instructions
	ConstantPropagationContextEvaluator eval =
		new ConstantPropagationContextEvaluator(monitor, trustWriteMemOption) {

			@Override
			public boolean evaluateContext(VarnodeContext context, Instruction instr) {

				FlowType ftype = instr.getFlowType();
				if (ftype.isComputed() && ftype.isJump()) {
					Varnode pcVal = context.getRegisterVarnodeValue(
						program.getLanguage().getProgramCounter());
					if (pcVal != null) {
						if (isLinkRegister(context, pcVal) &&
							!instr.getFlowType().isTerminal()) {
							// need to set the return override
							instr.setFlowOverride(FlowOverride.RETURN);
							// get rid of any references that might have been put on from
							// bad flows
							ReferenceManager refMgr = program.getReferenceManager();
							refMgr.removeAllReferencesFrom(instr.getAddress());
						}
					}
					// if LR is a constant and is set right after this, this is a call
					Varnode lrVal = context.getRegisterVarnodeValue(lrRegister);
					if (lrVal != null) {
						if (context.isConstant(lrVal)) {
							long target = lrVal.getAddress().getOffset();
							Address addr = instr.getMaxAddress().add(1);
							if (target == addr.getOffset() && !instr.getFlowType().isCall()) {
								// if there are is a read reference there as well,
								//  then this is really a branch, not a call
								if (hasDataReferenceTo(program, addr)) {
									return false;
								}
								// if flow already over-ridden don't override again
								if (instr.getFlowOverride() != FlowOverride.NONE) {
									return false;
								}
								instr.setFlowOverride(FlowOverride.CALL);
								// need to trigger disassembly below! if not already
								doArmThumbDisassembly(program, instr, context, addr,
									instr.getFlowType(), false, monitor);
								// need to trigger re-function creation!
								Function f = program.getFunctionManager().getFunctionContaining(
									instr.getMinAddress());
								if (f != null) {
									try {
										CreateFunctionCmd.fixupFunctionBody(program, f,
											monitor);
									}
									catch (CancelledException e) {
										return true;
									}
									//AutoAnalysisManager.getAnalysisManager(program).functionDefined(
									//	func.getBody());
								}
							}
						}
					}

				}

				return false;
			}

			/**
			 * Check if there are any data references to this location.
			 * @param program
			 * @param addr
			 * @return true if there are any data references to addr
			 */
			private boolean hasDataReferenceTo(Program program, Address addr) {
				ReferenceManager refMgr = program.getReferenceManager();
				if (!refMgr.hasReferencesTo(addr)) {
					return false;
				}
				ReferenceIterator referencesTo = refMgr.getReferencesTo(addr);
				while (referencesTo.hasNext()) {
					Reference reference = referencesTo.next();
					if (reference.getReferenceType().isData()) {
						return true;
					}
				}
				return false;
			}

			private boolean isLinkRegister(VarnodeContext context, Varnode pcVal) {
				return (pcVal.isRegister() &&
					pcVal.getAddress().equals(lrRegister.getAddress())) ||
					(context.isSymbol(pcVal) &&
						pcVal.getAddress().getAddressSpace().getName().equals(
							lrRegister.getName()) &&
						pcVal.getOffset() == 0);
			}

			@Override
			public boolean evaluateReference(VarnodeContext context, Instruction instr,
					int pcodeop, Address address, int size, DataType dataType, RefType refType) {
				if (refType.isJump() && refType.isComputed() &&
					program.getMemory().contains(address) && address.getOffset() != 0) {
					if (instr.getMnemonicString().startsWith("tb")) {
						return false;
					}
					doArmThumbDisassembly(program, instr, context, address, instr.getFlowType(),
						true, monitor);
					super.evaluateReference(context, instr, pcodeop, address, size, dataType, refType);
					return !symEval.encounteredBranch();
				}
				if (refType.isData() && program.getMemory().contains(address)) {
					if (refType.isRead() || refType.isWrite()) {
						int numOperands = instr.getNumOperands();
						// if two operands, then all read/write refs go on the 2nd operand
						createData(program, address, size);
						if (numOperands <= 2) {
							instr.addOperandReference(instr.getNumOperands() - 1, address, refType,
								SourceType.ANALYSIS);
							return false;
						}
						return true;
					}
				}
				else if (refType.isCall() && refType.isComputed() && !address.isExternalAddress()) {
					// must disassemble right now, because TB flag could get set back at end of blx
					doArmThumbDisassembly(program, instr, context, address, instr.getFlowType(),
						true, monitor);
				}

				return super.evaluateReference(context, instr, pcodeop, address, size, dataType, refType);
			}

			@Override
			public boolean evaluateDestination(VarnodeContext context,
					Instruction instruction) {
				FlowType flowType = instruction.getFlowType();
				if (!flowType.isJump()) {
					return false;
				}

				Reference[] refs = instruction.getReferencesFrom();
				if (refs.length <= 0 ||
					(refs.length == 1 && refs[0].getReferenceType().isData()) ||
					symEval.encounteredBranch()) {
					destSet.addRange(instruction.getMinAddress(), instruction.getMinAddress());
				}
				return false;
			}
			
			@Override
			public boolean evaluateReturn(Varnode retVN, VarnodeContext context, Instruction instruction) {
				// check if a return is actually returning, or is branching with a constant PC
				
				// if flow already overridden, don't override again
				if (instruction.getFlowOverride() != FlowOverride.NONE) {
					return false;
				}
				
				if (retVN != null && context.isConstant(retVN)) {
					long offset = retVN.getOffset();
					if (offset > 3 && offset != -1) {
						// need to override the return flow to a branch
						instruction.setFlowOverride(FlowOverride.BRANCH);
					}
				}
				
				return false;
			}
		};
		
	eval.setTrustWritableMemory(trustWriteMemOption)
	    .setMinpeculativeOffset(minSpeculativeRefAddress)
	    .setMaxSpeculativeOffset(maxSpeculativeRefAddress)
	    .setMinStoreLoadOffset(minStoreLoadRefAddress)
	    .setCreateComplexDataFromPointers(createComplexDataFromPointers);

	AddressSet resultSet = symEval.flowConstants(flowStart, flowSet, eval, true, monitor);

	if (recoverSwitchTables) {
		recoverSwitches(program, eval.getDestinationSet(), symEval, monitor);
	}

	return resultSet;
}