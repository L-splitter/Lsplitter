/**
 * This method is used to register a SockJsService.
 *
 * @param element          The element to be registered.
 * @param schedulerName    The name of the scheduler.
 * @param context          The context of the parser.
 * @param source           The source object.
 * @return                 A reference to the registered runtime bean.
 */
public static RuntimeBeanReference registerSockJsService(
		Element element, String schedulerName, ParserContext context, @Nullable Object source) {
	Element sockJsElement = DomUtils.getChildElementByTagName(element, "sockjs");
	if (sockJsElement != null) {
		RootBeanDefinition sockJsServiceDef = createSockJsServiceDefinition(sockJsElement, schedulerName, context, source);
		String sockJsServiceName = context.getReaderContext().registerWithGeneratedName(sockJsServiceDef);
		return new RuntimeBeanReference(sockJsServiceName);
	}
	return null;
}
/**
 * This method is used to create a SockJsService definition.
 *
 * @param sockJsElement    The SockJs element.
 * @param schedulerName    The name of the scheduler.
 * @param context          The context of the parser.
 * @param source           The source object.
 * @return                 The created SockJsService definition.
 */
private static RootBeanDefinition createSockJsServiceDefinition(
		Element sockJsElement, String schedulerName, ParserContext context, @Nullable Object source) {
	RootBeanDefinition sockJsServiceDef = new RootBeanDefinition(DefaultSockJsService.class);
	sockJsServiceDef.setSource(source);
	Object scheduler = getScheduler(sockJsElement, schedulerName, context, source);
	sockJsServiceDef.getConstructorArgumentValues().addIndexedArgumentValue(0, scheduler);
	Element transportHandlersElement = DomUtils.getChildElementByTagName(sockJsElement, "transport-handlers");
	if (transportHandlersElement != null) {
		handleTransportHandlers(sockJsServiceDef, transportHandlersElement, context, source);
	}
	ManagedList<Object> interceptors = handleInterceptors(sockJsElement, context, source);
	sockJsServiceDef.getPropertyValues().add("handshakeInterceptors", interceptors);
	handleSockJsAttributes(sockJsServiceDef, sockJsElement);
	sockJsServiceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
	return sockJsServiceDef;
}
/**
 * This method is used to get the scheduler.
 *
 * @param sockJsElement    The SockJs element.
 * @param schedulerName    The name of the scheduler.
 * @param context          The context of the parser.
 * @param source           The source object.
 * @return                 The scheduler object.
 */
private static Object getScheduler(
		Element sockJsElement, String schedulerName, ParserContext context, @Nullable Object source) {
	String customTaskSchedulerName = sockJsElement.getAttribute("scheduler");
	if (!customTaskSchedulerName.isEmpty()) {
		return new RuntimeBeanReference(customTaskSchedulerName);
	}
	else {
		return registerScheduler(schedulerName, context, source);
	}
}
/**
 * This method is used to handle transport handlers.
 *
 * @param sockJsServiceDef The SockJsService definition.
 * @param transportHandlersElement The transport handlers element.
 * @param context          The context of the parser.
 * @param source           The source object.
 */
private static void handleTransportHandlers(
		RootBeanDefinition sockJsServiceDef, Element transportHandlersElement, ParserContext context, @Nullable Object source) {
	String registerDefaults = transportHandlersElement.getAttribute("register-defaults");
	if (registerDefaults.equals("false")) {
		sockJsServiceDef.setBeanClass(TransportHandlingSockJsService.class);
	}
	ManagedList<?> transportHandlers = parseBeanSubElements(transportHandlersElement, context);
	sockJsServiceDef.getConstructorArgumentValues().addIndexedArgumentValue(1, transportHandlers);
}
/**
 * This method is used to handle interceptors.
 *
 * @param sockJsElement    The SockJs element.
 * @param context          The context of the parser.
 * @param source           The source object.
 * @return                 The list of interceptors.
 */
private static ManagedList<Object> handleInterceptors(
		Element sockJsElement, ParserContext context, @Nullable Object source) {
	Element interceptElem = DomUtils.getChildElementByTagName(sockJsElement, "handshake-interceptors");
	ManagedList<Object> interceptors = WebSocketNamespaceUtils.parseBeanSubElements(interceptElem, context);
	String allowedOrigins = sockJsElement.getAttribute("allowed-origins");
	List<String> origins = Arrays.asList(StringUtils.tokenizeToStringArray(allowedOrigins, ","));
	String allowedOriginPatterns = sockJsElement.getAttribute("allowed-origin-patterns");
	List<String> originPatterns = Arrays.asList(StringUtils.tokenizeToStringArray(allowedOriginPatterns, ","));
	RootBeanDefinition originHandshakeInterceptor = new RootBeanDefinition(OriginHandshakeInterceptor.class);
	originHandshakeInterceptor.getPropertyValues().add("allowedOrigins", origins);
	originHandshakeInterceptor.getPropertyValues().add("allowedOriginPatterns", originPatterns);
	interceptors.add(originHandshakeInterceptor);
	return interceptors;
}
/**
 * This method is used to handle SockJs attributes.
 *
 * @param sockJsServiceDef The SockJsService definition.
 * @param sockJsElement    The SockJs element.
 */
private static void handleSockJsAttributes(
		RootBeanDefinition sockJsServiceDef, Element sockJsElement) {
	String[] attributes = new String[] {
		"name", "websocket-enabled", "session-cookie-needed", "stream-bytes-limit",
		"disconnect-delay", "message-cache-size", "heartbeat-time", "client-library-url",
		"message-codec", "suppress-cors"
	};
	for (String attribute : attributes) {
		String attrValue = sockJsElement.getAttribute(attribute);
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add(attribute, attrValue);
		}
	}
}
