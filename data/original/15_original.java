public void testOpenEntityManagerInViewFilterAsyncScenario() throws Exception {
	given(manager.isOpen()).willReturn(true);

	final EntityManagerFactory factory2 = mock();
	final EntityManager manager2 = mock();

	given(factory2.createEntityManager()).willReturn(manager2);
	given(manager2.isOpen()).willReturn(true);

	MockServletContext sc = new MockServletContext();
	StaticWebApplicationContext wac = new StaticWebApplicationContext();
	wac.setServletContext(sc);
	wac.getDefaultListableBeanFactory().registerSingleton("entityManagerFactory", factory);
	wac.getDefaultListableBeanFactory().registerSingleton("myEntityManagerFactory", factory2);
	wac.refresh();
	sc.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);

	MockFilterConfig filterConfig = new MockFilterConfig(wac.getServletContext(), "filter");
	MockFilterConfig filterConfig2 = new MockFilterConfig(wac.getServletContext(), "filter2");
	filterConfig2.addInitParameter("entityManagerFactoryBeanName", "myEntityManagerFactory");

	final OpenEntityManagerInViewFilter filter = new OpenEntityManagerInViewFilter();
	filter.init(filterConfig);
	final OpenEntityManagerInViewFilter filter2 = new OpenEntityManagerInViewFilter();
	filter2.init(filterConfig2);

	final AtomicInteger count = new AtomicInteger();

	final FilterChain filterChain = (servletRequest, servletResponse) -> {
		assertThat(TransactionSynchronizationManager.hasResource(factory)).isTrue();
		servletRequest.setAttribute("invoked", Boolean.TRUE);
		count.incrementAndGet();
	};

	final AtomicInteger count2 = new AtomicInteger();

	final FilterChain filterChain2 = (servletRequest, servletResponse) -> {
		assertThat(TransactionSynchronizationManager.hasResource(factory2)).isTrue();
		filter.doFilter(servletRequest, servletResponse, filterChain);
		count2.incrementAndGet();
	};

	FilterChain filterChain3 = new PassThroughFilterChain(filter2, filterChain2);

	AsyncWebRequest asyncWebRequest = mock();
	given(asyncWebRequest.isAsyncStarted()).willReturn(true);

	WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(this.request);
	asyncManager.setTaskExecutor(this.taskExecutor);
	asyncManager.setAsyncWebRequest(asyncWebRequest);
	asyncManager.startCallableProcessing((Callable<String>) () -> "anything");

	this.taskExecutor.await();
	assertThat(asyncManager.getConcurrentResult()).as("Concurrent result ").isEqualTo("anything");

	assertThat(TransactionSynchronizationManager.hasResource(factory)).isFalse();
	assertThat(TransactionSynchronizationManager.hasResource(factory2)).isFalse();
	filter2.doFilter(this.request, this.response, filterChain3);
	assertThat(TransactionSynchronizationManager.hasResource(factory)).isFalse();
	assertThat(TransactionSynchronizationManager.hasResource(factory2)).isFalse();
	assertThat(count.get()).isEqualTo(1);
	assertThat(count2.get()).isEqualTo(1);
	assertThat(request.getAttribute("invoked")).isNotNull();
	verify(asyncWebRequest, times(2)).addCompletionHandler(any(Runnable.class));
	verify(asyncWebRequest).addTimeoutHandler(any(Runnable.class));
	verify(asyncWebRequest, times(2)).addCompletionHandler(any(Runnable.class));
	verify(asyncWebRequest).startAsync();

	// Async dispatch after concurrent handling produces result ...

	reset(asyncWebRequest);
	given(asyncWebRequest.isAsyncStarted()).willReturn(false);

	assertThat(TransactionSynchronizationManager.hasResource(factory)).isFalse();
	assertThat(TransactionSynchronizationManager.hasResource(factory2)).isFalse();
	filter.doFilter(this.request, this.response, filterChain3);
	assertThat(TransactionSynchronizationManager.hasResource(factory)).isFalse();
	assertThat(TransactionSynchronizationManager.hasResource(factory2)).isFalse();
	assertThat(count.get()).isEqualTo(2);
	assertThat(count2.get()).isEqualTo(2);

	verify(this.manager).close();
	verify(manager2).close();

	wac.close();
}