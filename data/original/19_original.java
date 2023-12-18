public static RuntimeBeanReference registerSockJsService(
		Element element, String schedulerName, ParserContext context, @Nullable Object source) {

	Element sockJsElement = DomUtils.getChildElementByTagName(element, "sockjs");

	if (sockJsElement != null) {
		Element handshakeHandler = DomUtils.getChildElementByTagName(element, "handshake-handler");

		RootBeanDefinition sockJsServiceDef = new RootBeanDefinition(DefaultSockJsService.class);
		sockJsServiceDef.setSource(source);

		Object scheduler;
		String customTaskSchedulerName = sockJsElement.getAttribute("scheduler");
		if (!customTaskSchedulerName.isEmpty()) {
			scheduler = new RuntimeBeanReference(customTaskSchedulerName);
		}
		else {
			scheduler = registerScheduler(schedulerName, context, source);
		}
		sockJsServiceDef.getConstructorArgumentValues().addIndexedArgumentValue(0, scheduler);

		Element transportHandlersElement = DomUtils.getChildElementByTagName(sockJsElement, "transport-handlers");
		if (transportHandlersElement != null) {
			String registerDefaults = transportHandlersElement.getAttribute("register-defaults");
			if (registerDefaults.equals("false")) {
				sockJsServiceDef.setBeanClass(TransportHandlingSockJsService.class);
			}
			ManagedList<?> transportHandlers = parseBeanSubElements(transportHandlersElement, context);
			sockJsServiceDef.getConstructorArgumentValues().addIndexedArgumentValue(1, transportHandlers);
		}
		else if (handshakeHandler != null) {
			RuntimeBeanReference handshakeHandlerRef = new RuntimeBeanReference(handshakeHandler.getAttribute("ref"));
			RootBeanDefinition transportHandler = new RootBeanDefinition(WebSocketTransportHandler.class);
			transportHandler.setSource(source);
			transportHandler.getConstructorArgumentValues().addIndexedArgumentValue(0, handshakeHandlerRef);
			sockJsServiceDef.getConstructorArgumentValues().addIndexedArgumentValue(1, transportHandler);
		}

		Element interceptElem = DomUtils.getChildElementByTagName(element, "handshake-interceptors");
		ManagedList<Object> interceptors = WebSocketNamespaceUtils.parseBeanSubElements(interceptElem, context);

		String allowedOrigins = element.getAttribute("allowed-origins");
		List<String> origins = Arrays.asList(StringUtils.tokenizeToStringArray(allowedOrigins, ","));
		sockJsServiceDef.getPropertyValues().add("allowedOrigins", origins);

		String allowedOriginPatterns = element.getAttribute("allowed-origin-patterns");
		List<String> originPatterns = Arrays.asList(StringUtils.tokenizeToStringArray(allowedOriginPatterns, ","));
		sockJsServiceDef.getPropertyValues().add("allowedOriginPatterns", originPatterns);

		RootBeanDefinition originHandshakeInterceptor = new RootBeanDefinition(OriginHandshakeInterceptor.class);
		originHandshakeInterceptor.getPropertyValues().add("allowedOrigins", origins);
		originHandshakeInterceptor.getPropertyValues().add("allowedOriginPatterns", originPatterns);
		interceptors.add(originHandshakeInterceptor);
		sockJsServiceDef.getPropertyValues().add("handshakeInterceptors", interceptors);

		String attrValue = sockJsElement.getAttribute("name");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("name", attrValue);
		}
		attrValue = sockJsElement.getAttribute("websocket-enabled");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("webSocketEnabled", Boolean.valueOf(attrValue));
		}
		attrValue = sockJsElement.getAttribute("session-cookie-needed");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("sessionCookieNeeded", Boolean.valueOf(attrValue));
		}
		attrValue = sockJsElement.getAttribute("stream-bytes-limit");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("streamBytesLimit", Integer.valueOf(attrValue));
		}
		attrValue = sockJsElement.getAttribute("disconnect-delay");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("disconnectDelay", Long.valueOf(attrValue));
		}
		attrValue = sockJsElement.getAttribute("message-cache-size");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("httpMessageCacheSize", Integer.valueOf(attrValue));
		}
		attrValue = sockJsElement.getAttribute("heartbeat-time");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("heartbeatTime", Long.valueOf(attrValue));
		}
		attrValue = sockJsElement.getAttribute("client-library-url");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("sockJsClientLibraryUrl", attrValue);
		}
		attrValue = sockJsElement.getAttribute("message-codec");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("messageCodec", new RuntimeBeanReference(attrValue));
		}
		attrValue = sockJsElement.getAttribute("suppress-cors");
		if (!attrValue.isEmpty()) {
			sockJsServiceDef.getPropertyValues().add("suppressCors", Boolean.valueOf(attrValue));
		}
		sockJsServiceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		String sockJsServiceName = context.getReaderContext().registerWithGeneratedName(sockJsServiceDef);
		return new RuntimeBeanReference(sockJsServiceName);
	}
	return null;
}