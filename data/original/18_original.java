public final void handleRequest(ServerHttpRequest request, ServerHttpResponse response,
		@Nullable String sockJsPath, WebSocketHandler wsHandler) throws SockJsException {

	if (sockJsPath == null) {
		if (logger.isWarnEnabled()) {
			logger.warn(LogFormatUtils.formatValue(
					"Expected SockJS path. Failing request: " + request.getURI(), -1, true));
		}
		response.setStatusCode(HttpStatus.NOT_FOUND);
		return;
	}

	try {
		request.getHeaders();
	}
	catch (InvalidMediaTypeException ex) {
		// As per SockJS protocol content-type can be ignored (it's always json)
	}

	String requestInfo = (logger.isDebugEnabled() ? request.getMethod() + " " + request.getURI() : null);

	try {
		if (sockJsPath.isEmpty() || sockJsPath.equals("/")) {
			if (requestInfo != null) {
				logger.debug("Processing transport request: " + requestInfo);
			}
			if ("websocket".equalsIgnoreCase(request.getHeaders().getUpgrade())) {
				response.setStatusCode(HttpStatus.BAD_REQUEST);
				return;
			}
			response.getHeaders().setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
			response.getBody().write("Welcome to SockJS!\n".getBytes(StandardCharsets.UTF_8));
		}

		else if (sockJsPath.equals("/info")) {
			if (requestInfo != null) {
				logger.debug("Processing transport request: " + requestInfo);
			}
			this.infoHandler.handle(request, response);
		}

		else if (sockJsPath.matches("/iframe[0-9-.a-z_]*.html")) {
			if (!getAllowedOrigins().isEmpty() && !getAllowedOrigins().contains("*") ||
					!getAllowedOriginPatterns().isEmpty()) {
				if (requestInfo != null) {
					logger.debug("Iframe support is disabled when an origin check is required. " +
							"Ignoring transport request: " + requestInfo);
				}
				response.setStatusCode(HttpStatus.NOT_FOUND);
				return;
			}
			if (getAllowedOrigins().isEmpty()) {
				response.getHeaders().add(XFRAME_OPTIONS_HEADER, "SAMEORIGIN");
			}
			if (requestInfo != null) {
				logger.debug("Processing transport request: " + requestInfo);
			}
			this.iframeHandler.handle(request, response);
		}

		else if (sockJsPath.equals("/websocket")) {
			if (isWebSocketEnabled()) {
				if (requestInfo != null) {
					logger.debug("Processing transport request: " + requestInfo);
				}
				handleRawWebSocketRequest(request, response, wsHandler);
			}
			else if (requestInfo != null) {
				logger.debug("WebSocket disabled. Ignoring transport request: " + requestInfo);
			}
		}

		else {
			String[] pathSegments = StringUtils.tokenizeToStringArray(sockJsPath.substring(1), "/");
			if (pathSegments.length != 3) {
				if (logger.isWarnEnabled()) {
					logger.warn(LogFormatUtils.formatValue("Invalid SockJS path '" + sockJsPath + "' - " +
							"required to have 3 path segments", -1, true));
				}
				if (requestInfo != null) {
					logger.debug("Ignoring transport request: " + requestInfo);
				}
				response.setStatusCode(HttpStatus.NOT_FOUND);
				return;
			}

			String serverId = pathSegments[0];
			String sessionId = pathSegments[1];
			String transport = pathSegments[2];

			if (!isWebSocketEnabled() && transport.equals("websocket")) {
				if (requestInfo != null) {
					logger.debug("WebSocket disabled. Ignoring transport request: " + requestInfo);
				}
				response.setStatusCode(HttpStatus.NOT_FOUND);
				return;
			}
			else if (!validateRequest(serverId, sessionId, transport) || !validatePath(request)) {
				if (requestInfo != null) {
					logger.debug("Ignoring transport request: " + requestInfo);
				}
				response.setStatusCode(HttpStatus.NOT_FOUND);
				return;
			}

			if (requestInfo != null) {
				logger.debug("Processing transport request: " + requestInfo);
			}
			handleTransportRequest(request, response, wsHandler, sessionId, transport);
		}
		response.close();
	}
	catch (IOException ex) {
		throw new SockJsException("Failed to write to the response", null, ex);
	}
}