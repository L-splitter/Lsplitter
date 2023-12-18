/**
 * This method sets up the test environment for validating the search context.
 * @throws Exception
 */
public void testValidateSearchContext() throws Exception {
    final ShardSearchRequest shardSearchRequest = mock(ShardSearchRequest.class);
    when(shardSearchRequest.scroll()).thenReturn(new Scroll(TimeValue.timeValueMinutes(between(1, 10))));
    try (
        LegacyReaderContext readerContext = new LegacyReaderContext(
            new ShardSearchContextId(UUIDs.randomBase64UUID(), 0L),
            indexService,
            shard,
            shard.acquireSearcherSupplier(),
            shardSearchRequest,
            Long.MAX_VALUE
        )
    ) {
        setupReaderContext(readerContext);
        final IndicesAccessControl indicesAccessControl = mock(IndicesAccessControl.class);
        readerContext.putInContext(AuthorizationServiceField.INDICES_PERMISSIONS_KEY, indicesAccessControl);
        ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        final SecurityContext securityContext = new SecurityContext(Settings.EMPTY, threadContext);
        AuditTrail auditTrail = mock(AuditTrail.class);
        AuditTrailService auditTrailService = new AuditTrailService(auditTrail, mockLicenseState(true));
        SecuritySearchOperationListener listener = new SecuritySearchOperationListener(securityContext, auditTrailService);
        validateReaderContextWithDifferentAuthentications(readerContext, threadContext, listener, indicesAccessControl, auditTrail);
    }
}
/**
 * This method sets up the reader context with authentication.
 * @param readerContext
 */
private void setupReaderContext(LegacyReaderContext readerContext) {
    readerContext.putInContext(
        AuthenticationField.AUTHENTICATION_KEY,
        AuthenticationTestHelper.builder()
            .user(new User("test", "role"))
            .realmRef(new RealmRef("realm", "file", "node"))
            .build(false)
    );
}
/**
 * This method mocks the license state.
 * @param isAllowed
 * @return MockLicenseState
 */
private MockLicenseState mockLicenseState(boolean isAllowed) {
    MockLicenseState licenseState = mock(MockLicenseState.class);
    when(licenseState.isAllowed(Security.AUDITING_FEATURE)).thenReturn(isAllowed);
    return licenseState;
}
/**
 * This method validates the reader context with different authentications.
 * @param readerContext
 * @param threadContext
 * @param listener
 * @param indicesAccessControl
 * @param auditTrail
 */
private void validateReaderContextWithDifferentAuthentications(LegacyReaderContext readerContext, ThreadContext threadContext, SecuritySearchOperationListener listener, IndicesAccessControl indicesAccessControl, AuditTrail auditTrail) throws Exception {
    validateReaderContextWithAuthentication(readerContext, threadContext, listener, indicesAccessControl, auditTrail, "test", "role", "realm", "file", "node");
    validateReaderContextWithAuthentication(readerContext, threadContext, listener, indicesAccessControl, auditTrail, "test", "role", randomAlphaOfLengthBetween(1, 16), "file", randomAlphaOfLengthBetween(1, 8));
    validateReaderContextWithAuthentication(readerContext, threadContext, listener, indicesAccessControl, auditTrail, "test", "role", randomBoolean() ? "realm" : randomAlphaOfLengthBetween(1, 16), randomAlphaOfLengthBetween(5, 16), randomBoolean() ? "node" : randomAlphaOfLengthBetween(1, 8));
    validateReaderContextWithRunAsAuthentication(readerContext, threadContext, listener, indicesAccessControl, auditTrail, "authenticated", "runas", randomBoolean() ? "realm" : randomAlphaOfLengthBetween(1, 16), randomAlphaOfLengthBetween(5, 16), randomBoolean() ? "node" : randomAlphaOfLengthBetween(1, 8));
    validateReaderContextWithAuthentication(readerContext, threadContext, listener, indicesAccessControl, auditTrail, "authenticated", "runas", randomBoolean() ? "realm" : randomAlphaOfLengthBetween(1, 16), randomAlphaOfLengthBetween(5, 16), randomBoolean() ? "node" : randomAlphaOfLengthBetween(1, 8));
}
/**
 * This method validates the reader context with a specific authentication.
 * @param readerContext
 * @param threadContext
 * @param listener
 * @param indicesAccessControl
 * @param auditTrail
 * @param username
 * @param role
 * @param realmName
 * @param type
 * @param nodeName
 */
private void validateReaderContextWithAuthentication(LegacyReaderContext readerContext, ThreadContext threadContext, SecuritySearchOperationListener listener, IndicesAccessControl indicesAccessControl, AuditTrail auditTrail, String username, String role, String realmName, String type, String nodeName) throws Exception {
    try (StoredContext ignore = threadContext.newStoredContext()) {
        Authentication authentication = AuthenticationTestHelper.builder()
            .user(new User(username, role))
            .realmRef(new RealmRef(realmName, type, nodeName))
            .build(false);
        authentication.writeToContext(threadContext);
        listener.validateReaderContext(readerContext, Empty.INSTANCE);
        assertThat(threadContext.getTransient(AuthorizationServiceField.INDICES_PERMISSIONS_KEY), is(indicesAccessControl));
        verifyNoMoreInteractions(auditTrail);
    }
}
/**
 * This method validates the reader context with a specific run as authentication.
 * @param readerContext
 * @param threadContext
 * @param listener
 * @param indicesAccessControl
 * @param auditTrail
 * @param username
 * @param role
 * @param realmName
 * @param type
 * @param nodeName
 */
private void validateReaderContextWithRunAsAuthentication(LegacyReaderContext readerContext, ThreadContext threadContext, SecuritySearchOperationListener listener, IndicesAccessControl indicesAccessControl, AuditTrail auditTrail, String username, String role, String realmName, String type, String nodeName) throws Exception {
    try (StoredContext ignore = threadContext.newStoredContext()) {
        Authentication authentication = AuthenticationTestHelper.builder()
            .user(new User(username, role))
            .realmRef(new RealmRef(realmName, type, nodeName))
            .runAs()
            .user(new User("test", "role"))
            .realmRef(new RealmRef(randomAlphaOfLengthBetween(1, 16), "file", nodeName))
            .build();
        authentication.writeToContext(threadContext);
        threadContext.putTransient(ORIGINATING_ACTION_KEY, "action");
        final InternalScrollSearchRequest request = new InternalScrollSearchRequest();
        listener.validateReaderContext(readerContext, request);
        assertThat(threadContext.getTransient(AuthorizationServiceField.INDICES_PERMISSIONS_KEY), is(indicesAccessControl));
        verifyNoMoreInteractions(auditTrail);
    }
}
