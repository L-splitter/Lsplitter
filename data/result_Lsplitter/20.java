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
    testExpressions(context, foh);
    testNullSafeGuard(context, "#var?.intValue()", 4);
    testNullSafeGuard(context, "#var?.booleanValue()", false);
    testNullSafeGuard(context, "#var?.booleanValue()", true);
    testNullSafeGuard(context, "#var?.longValue()", 5L);
    testNullSafeGuard(context, "#var?.floatValue()", 3f);
    testNullSafeGuard(context, "#var?.shortValue()", (short)8);
}

/** 
 * This method is used to test the non-compiled expressions and static method references.
 * @param context StandardEvaluationContext
 * @param foh FooObjectHolder
 * @throws Exception
 */
private void testExpressions(StandardEvaluationContext context, FooObjectHolder foh) throws Exception {
  // Test non-compiled expressions
  SpelExpression nonCompiledExpression = (SpelExpression)parser.parseExpression("getFoo()?.getObject()");
  assertThat(nonCompiledExpression.getValue(context)).isEqualTo("hello");
  foh.foo = null;
  assertThat(nonCompiledExpression.getValue(context)).isNull();
  assertCanCompile(nonCompiledExpression);
  foh.foo = new FooObject();
  assertThat(nonCompiledExpression.getValue(context)).isEqualTo("hello");
  foh.foo = null;
  assertThat(nonCompiledExpression.getValue(context)).isNull();
  // Test static method references
  SpelExpression staticMethodExpression = (SpelExpression)parser.parseExpression("#var?.methoda()");
  context.setVariable("var", StaticsHelper.class);
  assertThat(staticMethodExpression.getValue(context).toString()).isEqualTo("sh");
  context.setVariable("var", null);
  assertThat(staticMethodExpression.getValue(context)).isNull();
  assertCanCompile(staticMethodExpression);
  context.setVariable("var", StaticsHelper.class);
  assertThat(staticMethodExpression.getValue(context).toString()).isEqualTo("sh");
  context.setVariable("var", null);
  assertThat(staticMethodExpression.getValue(context)).isNull();
}
