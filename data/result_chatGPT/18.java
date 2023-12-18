/**
 * Handles the incoming request.
 *
 * @param request the incoming server request
 * @param response the server response
 * @param sockJsPath the SockJS path
 * @param wsHandler the WebSocket handler
 * @throws SockJsException if an error occurs while handling the request
 */
public final void handleRequest(ServerHttpRequest request, ServerHttpResponse response,
		@Nullable String sockJsPath, WebSocketHandler wsHandler) throws SockJsException {
	if (sockJsPath == null) {
		handleNullSockJsPath(request, response);
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
			handleRootPath(request, response, requestInfo);
		}
		else if (sockJsPath.equals("/info")) {
			handleInfoPath(request, response, requestInfo);
		}
		else if (sockJsPath.matches("/iframe[0-9-.a-z_]*.html")) {
			handleIframePath(request, response, requestInfo);
		}
		else if (sockJsPath.equals("/websocket")) {
			handleWebSocketPath(request, response, wsHandler, requestInfo);
		}
		else {
			handleOtherPaths(request, response, sockJsPath, requestInfo, wsHandler);
		}
		response.close();
	}
	catch (IOException ex) {
		throw new SockJsException("Failed to write to the response", null, ex);
	}
}
/**
 * Handles the case when the SockJS path is null.
 *
 * @param request the incoming server request
 * @param response the server response
 */
private void handleNullSockJsPath(ServerHttpRequest request, ServerHttpResponse response) {
	if (logger.isWarnEnabled()) {
		logger.warn(LogFormatUtils.formatValue(
				"Expected SockJS path. Failing request: " + request.getURI(), -1, true));
	}
	response.setStatusCode(HttpStatus.NOT_FOUND);
}
/**
 * Handles the case when the SockJS path is root ("/").
 *
 * @param request the incoming server request
 * @param response the server response
 * @param requestInfo the request information
 * @throws IOException if an error occurs while writing to the response
 */
private void handleRootPath(ServerHttpRequest request, ServerHttpResponse response, String requestInfo) throws IOException {
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
/**
 * Handles the case when the SockJS path is "/info".
 *
 * @param request the incoming server request
 * @param response the server response
 * @param requestInfo the request information
 */
private void handleInfoPath(ServerHttpRequest request, ServerHttpResponse response, String requestInfo) {
	if (requestInfo != null) {
		logger.debug("Processing transport request: " + requestInfo);
	}
	this.infoHandler.handle(request, response);
}
/**
 * Handles the case when the SockJS path is an iframe path.
 *
 * @param request the incoming server request
 * @param response the server response
 * @param requestInfo the request information
 */
private void handleIframePath(ServerHttpRequest request, ServerHttpResponse response, String requestInfo) {
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
/**
 * Handles the case when the SockJS path is "/websocket".
 *
 * @param request the incoming server request
 * @param response the server response
 * @param wsHandler the WebSocket handler
 * @param requestInfo the request information
 */
private void handleWebSocketPath(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, String requestInfo) {
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
/**
 * Handles the case when the SockJS path is not one of the predefined paths.
 *
 * @param request the incoming server request
 * @param response the server response
 * @param sockJsPath the SockJS path
 * @param requestInfo the request information
 * @param wsHandler the WebSocket handler
 */
private void handleOtherPaths(ServerHttpRequest request, ServerHttpResponse response, String sockJsPath, String requestInfo, WebSocketHandler wsHandler) {
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
