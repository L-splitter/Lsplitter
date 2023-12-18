/**
 * This method is responsible for following all flows building up context
 * and using context to fill out addresses on certain instructions.
 *
 * @param program the program to be processed.
 * @param flowStart the address where the flow starts.
 * @param flowSet the set of addresses to be processed.
 * @param symEval the symbolic propagator.
 * @param monitor the task monitor.
 * @return the set of addresses processed.
 * @throws CancelledException if the task is cancelled.
 */
public AddressSet flowConstants(final Program program, Address flowStart,
		AddressSetView flowSet, final SymbolicPropogator symEval, final TaskMonitor monitor)
		throws CancelledException {
	ConstantPropagationContextEvaluator eval = createConstantPropagationContextEvaluator(program, monitor);
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
/**
 * This method creates a new instance of ConstantPropagationContextEvaluator.
 *
 * @param program the program to be processed.
 * @param monitor the task monitor.
 * @return a new instance of ConstantPropagationContextEvaluator.
 */
private ConstantPropagationContextEvaluator createConstantPropagationContextEvaluator(final Program program, final TaskMonitor monitor) {
	return new ConstantPropagationContextEvaluator(monitor, trustWriteMemOption) {
		@Override
		public boolean evaluateContext(VarnodeContext context, Instruction instr) {
			return evaluateContextMethod(context, instr, program);
		}
		@Override
		public boolean evaluateReference(VarnodeContext context, Instruction instr,
				int pcodeop, Address address, int size, DataType dataType, RefType refType) {
			return evaluateReferenceMethod(context, instr, pcodeop, address, size, dataType, refType, program, monitor, symEval);
		}
		@Override
		public boolean evaluateDestination(VarnodeContext context,
				Instruction instruction) {
			return evaluateDestinationMethod(context, instruction, symEval);
		}
		@Override
		public boolean evaluateReturn(Varnode retVN, VarnodeContext context, Instruction instruction) {
			return evaluateReturnMethod(retVN, context, instruction);
		}
	};
}
/**
 * This method evaluates the context of an instruction.
 *
 * @param context the context of the instruction.
 * @param instr the instruction to be evaluated.
 * @param program the program to be processed.
 * @return true if the evaluation is successful, false otherwise.
 */
private boolean evaluateContextMethod(VarnodeContext context, Instruction instr, Program program) {
	// Your code here
}
/**
 * This method evaluates a reference.
 *
 * @param context the context of the instruction.
 * @param instr the instruction to be evaluated.
 * @param pcodeop the pcode operation.
 * @param address the address of the reference.
 * @param size the size of the reference.
 * @param dataType the data type of the reference.
 * @param refType the reference type.
 * @param program the program to be processed.
 * @param monitor the task monitor.
 * @param symEval the symbolic propagator.
 * @return true if the evaluation is successful, false otherwise.
 */
private boolean evaluateReferenceMethod(VarnodeContext context, Instruction instr,
		int pcodeop, Address address, int size, DataType dataType, RefType refType, Program program, TaskMonitor monitor, SymbolicPropogator symEval) {
	// Your code here
}
/**
 * This method evaluates the destination of an instruction.
 *
 * @param context the context of the instruction.
 * @param instruction the instruction to be evaluated.
 * @param symEval the symbolic propagator.
 * @return true if the evaluation is successful, false otherwise.
 */
private boolean evaluateDestinationMethod(VarnodeContext context,
		Instruction instruction, SymbolicPropogator symEval) {
	// Your code here
}
/**
 * This method evaluates a return instruction.
 *
 * @param retVN the return varnode.
 * @param context the context of the instruction.
 * @param instruction the instruction to be evaluated.
 * @return true if the evaluation is successful, false otherwise.
 */
private boolean evaluateReturnMethod(Varnode retVN, VarnodeContext context, Instruction instruction) {
	// Your code here
}
