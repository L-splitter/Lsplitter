public void nullsafeMethodChaining_SPR16489() throws Exception {
	FooObjectHolder foh = new FooObjectHolder();
	StandardEvaluationContext context = new StandardEvaluationContext();
	context.setRootObject(foh);

	// First non compiled:
	SpelExpression expression = (SpelExpression) parser.parseExpression("getFoo()?.getObject()");
	assertThat(expression.getValue(context)).isEqualTo("hello");
	foh.foo = null;
	assertThat(expression.getValue(context)).isNull();
	assertCanCompile(expression);
	foh.foo = new FooObject();
	assertThat(expression.getValue(context)).isEqualTo("hello");
	foh.foo = null;
	assertThat(expression.getValue(context)).isNull();

	// Static method references
	expression = (SpelExpression) parser.parseExpression("#var?.methoda()");
	context.setVariable("var", StaticsHelper.class);
	assertThat(expression.getValue(context).toString()).isEqualTo("sh");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();
	assertCanCompile(expression);
	context.setVariable("var", StaticsHelper.class);
	assertThat(expression.getValue(context).toString()).isEqualTo("sh");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();

	// Nullsafe guard on expression element evaluating to primitive/null
	expression = (SpelExpression) parser.parseExpression("#var?.intValue()");
	context.setVariable("var", 4);
	assertThat(expression.getValue(context).toString()).isEqualTo("4");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();
	assertCanCompile(expression);
	context.setVariable("var", 4);
	assertThat(expression.getValue(context).toString()).isEqualTo("4");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();

	// Nullsafe guard on expression element evaluating to primitive/null
	expression = (SpelExpression) parser.parseExpression("#var?.booleanValue()");
	context.setVariable("var", false);
	assertThat(expression.getValue(context).toString()).isEqualTo("false");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();
	assertCanCompile(expression);
	context.setVariable("var", false);
	assertThat(expression.getValue(context).toString()).isEqualTo("false");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();

	// Nullsafe guard on expression element evaluating to primitive/null
	expression = (SpelExpression) parser.parseExpression("#var?.booleanValue()");
	context.setVariable("var", true);
	assertThat(expression.getValue(context).toString()).isEqualTo("true");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();
	assertCanCompile(expression);
	context.setVariable("var", true);
	assertThat(expression.getValue(context).toString()).isEqualTo("true");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();

	// Nullsafe guard on expression element evaluating to primitive/null
	expression = (SpelExpression) parser.parseExpression("#var?.longValue()");
	context.setVariable("var", 5L);
	assertThat(expression.getValue(context).toString()).isEqualTo("5");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();
	assertCanCompile(expression);
	context.setVariable("var", 5L);
	assertThat(expression.getValue(context).toString()).isEqualTo("5");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();

	// Nullsafe guard on expression element evaluating to primitive/null
	expression = (SpelExpression) parser.parseExpression("#var?.floatValue()");
	context.setVariable("var", 3f);
	assertThat(expression.getValue(context).toString()).isEqualTo("3.0");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();
	assertCanCompile(expression);
	context.setVariable("var", 3f);
	assertThat(expression.getValue(context).toString()).isEqualTo("3.0");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();

	// Nullsafe guard on expression element evaluating to primitive/null
	expression = (SpelExpression) parser.parseExpression("#var?.shortValue()");
	context.setVariable("var", (short)8);
	assertThat(expression.getValue(context).toString()).isEqualTo("8");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();
	assertCanCompile(expression);
	context.setVariable("var", (short)8);
	assertThat(expression.getValue(context).toString()).isEqualTo("8");
	context.setVariable("var", null);
	assertThat(expression.getValue(context)).isNull();
}