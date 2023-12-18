/**
 * This method is used to initialize the context and the FooObjectHolder.
 * @return StandardEvaluationContext
 */
private StandardEvaluationContext initializeContext() {
    FooObjectHolder foh = new FooObjectHolder();
    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setRootObject(foh);
    return context;
}
/**
 * This method is used to test the non-compiled expressions.
 * @param context StandardEvaluationContext
 * @param foh FooObjectHolder
 * @throws Exception
 */
private void testNonCompiledExpressions(StandardEvaluationContext context, FooObjectHolder foh) throws Exception {
    SpelExpression expression = (SpelExpression) parser.parseExpression("getFoo()?.getObject()");
    assertThat(expression.getValue(context)).isEqualTo("hello");
    foh.foo = null;
    assertThat(expression.getValue(context)).isNull();
    assertCanCompile(expression);
    foh.foo = new FooObject();
    assertThat(expression.getValue(context)).isEqualTo("hello");
    foh.foo = null;
    assertThat(expression.getValue(context)).isNull();
}
/**
 * This method is used to test the static method references.
 * @param context StandardEvaluationContext
 * @throws Exception
 */
private void testStaticMethodReferences(StandardEvaluationContext context) throws Exception {
    SpelExpression expression = (SpelExpression) parser.parseExpression("#var?.methoda()");
    context.setVariable("var", StaticsHelper.class);
    assertThat(expression.getValue(context).toString()).isEqualTo("sh");
    context.setVariable("var", null);
    assertThat(expression.getValue(context)).isNull();
    assertCanCompile(expression);
    context.setVariable("var", StaticsHelper.class);
    assertThat(expression.getValue(context).toString()).isEqualTo("sh");
    context.setVariable("var", null);
    assertThat(expression.getValue(context)).isNull();
}
/**
 * This method is used to test the nullsafe guard on expression element evaluating to primitive/null.
 * @param context StandardEvaluationContext
 * @param expressionString String
 * @param value Object
 * @throws Exception
 */
private void testNullSafeGuard(StandardEvaluationContext context, String expressionString, Object value) throws Exception {
    SpelExpression expression = (SpelExpression) parser.parseExpression(expressionString);
    context.setVariable("var", value);
    assertThat(expression.getValue(context).toString()).isEqualTo(value.toString());
    context.setVariable("var", null);
    assertThat(expression.getValue(context)).isNull();
    assertCanCompile(expression);
    context.setVariable("var", value);
    assertThat(expression.getValue(context).toString()).isEqualTo(value.toString());
    context.setVariable("var", null);
    assertThat(expression.getValue(context)).isNull();
}
public void nullsafeMethodChaining_SPR16489() throws Exception {
    StandardEvaluationContext context = initializeContext();
    FooObjectHolder foh = (FooObjectHolder) context.getRootObject();
    testNonCompiledExpressions(context, foh);
    testStaticMethodReferences(context);
    testNullSafeGuard(context, "#var?.intValue()", 4);
    testNullSafeGuard(context, "#var?.booleanValue()", false);
    testNullSafeGuard(context, "#var?.booleanValue()", true);
    testNullSafeGuard(context, "#var?.longValue()", 5L);
    testNullSafeGuard(context, "#var?.floatValue()", 3f);
    testNullSafeGuard(context, "#var?.shortValue()", (short)8);
}
