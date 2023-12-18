/**
 * This method is used to compile mathematical expressions with different operand types.
 * @throws Exception
 */
public void compilingMathematicalExpressionsWithDifferentOperandTypes() throws Exception {
    NumberHolder nh = new NumberHolder();
    // Parsing and evaluating the first expression
    parseAndEvaluateExpression(nh, "(T(Integer).valueOf(payload).doubleValue())/18D");
    // Parsing and evaluating the second expression
    parseAndEvaluateExpression(nh, "payload/18D");
}
/**
 * This method is used to parse and evaluate a given expression.
 * @param nh The NumberHolder object.
 * @param expr The expression to be parsed and evaluated.
 * @throws Exception
 */
private void parseAndEvaluateExpression(NumberHolder nh, String expr) throws Exception {
    expression = parser.parseExpression(expr);
    Object o = expression.getValue(nh);
    assertThat(o).isEqualTo(2d);
    System.out.println("Performance check for SpEL expression: '" + expr + "'");
    performanceCheck(nh);
    compile(expression);
    System.out.println("Now compiled:");
    o = expression.getValue(nh);
    assertThat(o).isEqualTo(2d);
    performanceCheck(nh);
}
/**
 * This method is used to perform a performance check by evaluating the expression one million times.
 * @param nh The NumberHolder object.
 */
private void performanceCheck(NumberHolder nh) {
    long stime = System.currentTimeMillis();
    Object o;
    for (int i = 0; i < 1000000; i++) {
        o = expression.getValue(nh);
    }
    System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
}
