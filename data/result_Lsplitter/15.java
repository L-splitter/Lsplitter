/**
 * This method sets up the mock environment for the test.
 */
private void setupMockEnvironment(EntityManagerFactory factory2, EntityManager manager2, MockServletContext sc, StaticWebApplicationContext wac) {
    given(manager.isOpen()).willReturn(true);
    given(factory2.createEntityManager()).willReturn(manager2);
    given(manager2.isOpen()).willReturn(true);
    wac.setServletContext(sc);
    wac.getDefaultListableBeanFactory().registerSingleton("entityManagerFactory", factory);
    wac.getDefaultListableBeanFactory().registerSingleton("myEntityManagerFactory", factory2);
    wac.refresh();
    sc.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
}
/**
 * This method initializes the filters for the test.
 */
private void initializeFilters(StaticWebApplicationContext wac, OpenEntityManagerInViewFilter filter, OpenEntityManagerInViewFilter filter2) {
    MockFilterConfig filterConfig = new MockFilterConfig(wac.getServletContext(), "filter");
    MockFilterConfig filterConfig2 = new MockFilterConfig(wac.getServletContext(), "filter2");
    filterConfig2.addInitParameter("entityManagerFactoryBeanName", "myEntityManagerFactory");
    filter.init(filterConfig);
    filter2.init(filterConfig2);
}
/**
 * This method sets up the filter chains for the test.
 */
private void setupFilterChains(AtomicInteger count, AtomicInteger count2, OpenEntityManagerInViewFilter filter, OpenEntityManagerInViewFilter filter2) {
    final FilterChain filterChain = (servletRequest, servletResponse) -> {
        assertThat(TransactionSynchronizationManager.hasResource(factory)).isTrue();
        servletRequest.setAttribute("invoked", Boolean.TRUE);
        count.incrementAndGet();
    };
    final FilterChain filterChain2 = (servletRequest, servletResponse) -> {
        assertThat(TransactionSynchronizationManager.hasResource(factory2)).isTrue();
        filter.doFilter(servletRequest, servletResponse, filterChain);
        count2.incrementAndGet();
    };
    FilterChain filterChain3 = new PassThroughFilterChain(filter2, filterChain2);
}
/**
 * This method sets up the async web request for the test.
 */
private void setupAsyncWebRequest(AsyncWebRequest asyncWebRequest, WebAsyncManager asyncManager) {
    given(asyncWebRequest.isAsyncStarted()).willReturn(true);
    asyncManager.setTaskExecutor(this.taskExecutor);
    asyncManager.setAsyncWebRequest(asyncWebRequest);
    asyncManager.startCallableProcessing((Callable<String>) () -> "anything");
}
/**
 * This method verifies the results of the test.
 */
private void verifyResults(AtomicInteger count, AtomicInteger count2, AsyncWebRequest asyncWebRequest, EntityManager manager2) {
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
    assertThat(count.get()).isEqualTo(2);
    assertThat(count2.get()).isEqualTo(2);
    verify(this.manager).close();
    verify(manager2).close();
}
public void testOpenEntityManagerInViewFilterAsyncScenario() throws Exception {
    final EntityManagerFactory factory2 = mock();
    final EntityManager manager2 = mock();
    MockServletContext sc = new MockServletContext();
    StaticWebApplicationContext wac = new StaticWebApplicationContext();
    setupMockEnvironment(factory2, manager2, sc, wac);
    final OpenEntityManagerInViewFilter filter = new OpenEntityManagerInViewFilter();
    final OpenEntityManagerInViewFilter filter2 = new OpenEntityManagerInViewFilter();
    initializeFilters(wac, filter, filter2);
    final AtomicInteger count = new AtomicInteger();
    final AtomicInteger count2 = new AtomicInteger();
    setupFilterChains(count, count2, filter, filter2);
    AsyncWebRequest asyncWebRequest = mock();
    WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(this.request);
    setupAsyncWebRequest(asyncWebRequest, asyncManager);
    this.taskExecutor.await();
    assertThat(asyncManager.getConcurrentResult()).as("Concurrent result ").isEqualTo("anything");
    filter2.doFilter(this.request, this.response, filterChain3);
    verifyResults(count, count2, asyncWebRequest, manager2);
    wac.close();
}
