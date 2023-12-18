protected void handleTransportRequest(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler handler, String sessionId, String transport) throws SockJsException {

	TransportType transportType = TransportType.fromValue(transport);
	if (transportType == null) {
		if (logger.isWarnEnabled()) {
			logger.warn(LogFormatUtils.formatValue("Unknown transport type for " + request.getURI(), -1, true));
		}
		response.setStatusCode(HttpStatus.NOT_FOUND);
		return;
	}

	TransportHandler transportHandler = this.handlers.get(transportType);
	if (transportHandler == null) {
		if (logger.isWarnEnabled()) {
			logger.warn(LogFormatUtils.formatValue("No TransportHandler for " + request.getURI(), -1, true));
		}
		response.setStatusCode(HttpStatus.NOT_FOUND);
		return;
	}

	SockJsException failure = null;
	HandshakeInterceptorChain chain = new HandshakeInterceptorChain(this.interceptors, handler);

	try {
		HttpMethod supportedMethod = transportType.getHttpMethod();
		if (supportedMethod != request.getMethod()) {
			if (request.getMethod() == HttpMethod.OPTIONS && transportType.supportsCors()) {
				if (checkOrigin(request, response, HttpMethod.OPTIONS, supportedMethod)) {
					response.setStatusCode(HttpStatus.NO_CONTENT);
					addCacheHeaders(response);
				}
			}
			else if (transportType.supportsCors()) {
				sendMethodNotAllowed(response, supportedMethod, HttpMethod.OPTIONS);
			}
			else {
				sendMethodNotAllowed(response, supportedMethod);
			}
			return;
		}

		SockJsSession session = this.sessions.get(sessionId);
		boolean isNewSession = false;
		if (session == null) {
			if (transportHandler instanceof SockJsSessionFactory sessionFactory) {
				Map<String, Object> attributes = new HashMap<>();
				if (!chain.applyBeforeHandshake(request, response, attributes)) {
					return;
				}
				session = createSockJsSession(sessionId, sessionFactory, handler, attributes);
				isNewSession = true;
			}
			else {
				response.setStatusCode(HttpStatus.NOT_FOUND);
				if (logger.isDebugEnabled()) {
					logger.debug("Session not found, sessionId=" + sessionId +
							". The session may have been closed " +
							"(e.g. missed heart-beat) while a message was coming in.");
				}
				return;
			}
		}
		else {
			Principal principal = session.getPrincipal();
			if (principal != null && !principal.equals(request.getPrincipal())) {
				logger.debug("The user for the session does not match the user for the request.");
				response.setStatusCode(HttpStatus.NOT_FOUND);
				return;
			}
			if (!transportHandler.checkSessionType(session)) {
				logger.debug("Session type does not match the transport type for the request.");
				response.setStatusCode(HttpStatus.NOT_FOUND);
				return;
			}
		}

		if (transportType.sendsNoCacheInstruction()) {
			addNoCacheHeaders(response);
		}
		if (transportType.supportsCors() && !checkOrigin(request, response)) {
			return;
		}

		transportHandler.handleRequest(request, response, handler, session);

		if (isNewSession && response instanceof ServletServerHttpResponse servletResponse) {
			int status = servletResponse.getServletResponse().getStatus();
			if (HttpStatusCode.valueOf(status).is4xxClientError()) {
				this.sessions.remove(sessionId);
			}
		}

		chain.applyAfterHandshake(request, response, null);
	}
	catch (SockJsException ex) {
		failure = ex;
	}
	catch (Exception ex) {
		failure = new SockJsException("Uncaught failure for request " + request.getURI(), sessionId, ex);
	}
	finally {
		if (failure != null) {
			chain.applyAfterHandshake(request, response, failure);
			throw failure;
		}
	}
}