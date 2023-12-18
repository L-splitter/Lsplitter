/**
 * Handles the transport request.
 *
 * @param request the server HTTP request
 * @param response the server HTTP response
 * @param handler the WebSocket handler
 * @param sessionId the session ID
 * @param transport the transport type
 * @throws SockJsException if an error occurs
 */
protected void handleTransportRequest(ServerHttpRequest request, ServerHttpResponse response,
                                      WebSocketHandler handler, String sessionId, String transport) throws SockJsException {
    TransportType transportType = getTransportType(request, response, transport);
    if (transportType == null) {
        return;
    }
    TransportHandler transportHandler = getTransportHandler(request, response, transportType);
    if (transportHandler == null) {
        return;
    }
    handleRequest(request, response, handler, sessionId, transportType, transportHandler);
}
/**
 * Gets the transport type.
 *
 * @param request the server HTTP request
 * @param response the server HTTP response
 * @param transport the transport type
 * @return the transport type
 */
private TransportType getTransportType(ServerHttpRequest request, ServerHttpResponse response, String transport) {
    TransportType transportType = TransportType.fromValue(transport);
    if (transportType == null) {
        logAndSetNotFound(request, response, "Unknown transport type for ");
    }
    return transportType;
}
/**
 * Gets the transport handler.
 *
 * @param request the server HTTP request
 * @param response the server HTTP response
 * @param transportType the transport type
 * @return the transport handler
 */
private TransportHandler getTransportHandler(ServerHttpRequest request, ServerHttpResponse response, TransportType transportType) {
    TransportHandler transportHandler = this.handlers.get(transportType);
    if (transportHandler == null) {
        logAndSetNotFound(request, response, "No TransportHandler for ");
    }
    return transportHandler;
}
/**
 * Logs a warning and sets the HTTP status to NOT_FOUND.
 *
 * @param request the server HTTP request
 * @param response the server HTTP response
 * @param message the log message
 */
private void logAndSetNotFound(ServerHttpRequest request, ServerHttpResponse response, String message) {
    if (logger.isWarnEnabled()) {
        logger.warn(LogFormatUtils.formatValue(message + request.getURI(), -1, true));
    }
    response.setStatusCode(HttpStatus.NOT_FOUND);
}
/**
 * Handles the request.
 *
 * @param request the server HTTP request
 * @param response the server HTTP response
 * @param handler the WebSocket handler
 * @param sessionId the session ID
 * @param transportType the transport type
 * @param transportHandler the transport handler
 * @throws SockJsException if an error occurs
 */
private void handleRequest(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
                           String sessionId, TransportType transportType, TransportHandler transportHandler) throws SockJsException {
    SockJsException failure = null;
    HandshakeInterceptorChain chain = new HandshakeInterceptorChain(this.interceptors, handler);
    try {
        handleMethod(request, response, transportType);
        SockJsSession session = handleSession(request, response, handler, sessionId, transportHandler, chain);
        if (session == null) {
            return;
        }
        handleHeadersAndCors(request, response, transportType);
        transportHandler.handleRequest(request, response, handler, session);
        handleNewSession(response, sessionId, session);
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
// The rest of the methods (handleMethod, handleSession, handleHeadersAndCors, handleNewSession) would be similar to the above methods.
// They would contain the corresponding parts of the original method's code.
