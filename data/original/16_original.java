public void compilingMathematicalExpressionsWithDifferentOperandTypes() throws Exception {
	NumberHolder nh = new NumberHolder();
	expression = parser.parseExpression("(T(Integer).valueOf(payload).doubleValue())/18D");
	Object o = expression.getValue(nh);
	assertThat(o).isEqualTo(2d);
	System.out.println("Performance check for SpEL expression: '(T(Integer).valueOf(payload).doubleValue())/18D'");
	long stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	compile(expression);
	System.out.println("Now compiled:");
	o = expression.getValue(nh);
	assertThat(o).isEqualTo(2d);

	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");

	expression = parser.parseExpression("payload/18D");
	o = expression.getValue(nh);
	assertThat(o).isEqualTo(2d);
	System.out.println("Performance check for SpEL expression: 'payload/18D'");
	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	compile(expression);
	System.out.println("Now compiled:");
	o = expression.getValue(nh);
	assertThat(o).isEqualTo(2d);

	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
	stime = System.currentTimeMillis();
	for (int i = 0; i < 1000000; i++) {
		o = expression.getValue(nh);
	}
	System.out.println("One million iterations: " + (System.currentTimeMillis()-stime) + "ms");
}