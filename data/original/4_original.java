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
        readerContext.putInContext(
            AuthenticationField.AUTHENTICATION_KEY,
            AuthenticationTestHelper.builder()
                .user(new User("test", "role"))
                .realmRef(new RealmRef("realm", "file", "node"))
                .build(false)
        );
        final IndicesAccessControl indicesAccessControl = mock(IndicesAccessControl.class);
        readerContext.putInContext(AuthorizationServiceField.INDICES_PERMISSIONS_KEY, indicesAccessControl);
        MockLicenseState licenseState = mock(MockLicenseState.class);
        when(licenseState.isAllowed(Security.AUDITING_FEATURE)).thenReturn(true);
        ThreadContext threadContext = new ThreadContext(Settings.EMPTY);
        final SecurityContext securityContext = new SecurityContext(Settings.EMPTY, threadContext);
        AuditTrail auditTrail = mock(AuditTrail.class);
        AuditTrailService auditTrailService = new AuditTrailService(auditTrail, licenseState);

        SecuritySearchOperationListener listener = new SecuritySearchOperationListener(securityContext, auditTrailService);
        try (StoredContext ignore = threadContext.newStoredContext()) {
            Authentication authentication = AuthenticationTestHelper.builder()
                .user(new User("test", "role"))
                .realmRef(new RealmRef("realm", "file", "node"))
                .build(false);
            authentication.writeToContext(threadContext);
            listener.validateReaderContext(readerContext, Empty.INSTANCE);
            assertThat(threadContext.getTransient(AuthorizationServiceField.INDICES_PERMISSIONS_KEY), is(indicesAccessControl));
            verifyNoMoreInteractions(auditTrail);
        }

        try (StoredContext ignore = threadContext.newStoredContext()) {
            final String nodeName = randomAlphaOfLengthBetween(1, 8);
            final String realmName = randomAlphaOfLengthBetween(1, 16);
            Authentication authentication = AuthenticationTestHelper.builder()
                .user(new User("test", "role"))
                .realmRef(new RealmRef(realmName, "file", nodeName))
                .build(false);
            authentication.writeToContext(threadContext);
            listener.validateReaderContext(readerContext, Empty.INSTANCE);
            assertThat(threadContext.getTransient(AuthorizationServiceField.INDICES_PERMISSIONS_KEY), is(indicesAccessControl));
            verifyNoMoreInteractions(auditTrail);
        }

        try (StoredContext ignore = threadContext.newStoredContext()) {
            final String nodeName = randomBoolean() ? "node" : randomAlphaOfLengthBetween(1, 8);
            final String realmName = randomBoolean() ? "realm" : randomAlphaOfLengthBetween(1, 16);
            final String type = randomAlphaOfLengthBetween(5, 16);
            Authentication authentication = AuthenticationTestHelper.builder()
                .user(new User("test", "role"))
                .realmRef(new RealmRef(realmName, type, nodeName))
                .build(false);
            authentication.writeToContext(threadContext);
            threadContext.putTransient(ORIGINATING_ACTION_KEY, "action");
            threadContext.putTransient(
                AUTHORIZATION_INFO_KEY,
                (AuthorizationInfo) () -> Collections.singletonMap(
                    PRINCIPAL_ROLES_FIELD_NAME,
                    authentication.getEffectiveSubject().getUser().roles()
                )
            );
            final InternalScrollSearchRequest request = new InternalScrollSearchRequest();
            SearchContextMissingException expected = expectThrows(
                SearchContextMissingException.class,
                () -> listener.validateReaderContext(readerContext, request)
            );
            assertEquals(readerContext.id(), expected.contextId());
            assertThat(threadContext.getTransient(AuthorizationServiceField.INDICES_PERMISSIONS_KEY), nullValue());
            verify(auditTrail).accessDenied(
                eq(null),
                eq(authentication),
                eq("action"),
                eq(request),
                authzInfoRoles(authentication.getEffectiveSubject().getUser().roles())
            );
        }

        // another user running as the original user
        try (StoredContext ignore = threadContext.newStoredContext()) {
            final String nodeName = randomBoolean() ? "node" : randomAlphaOfLengthBetween(1, 8);
            final String realmName = randomBoolean() ? "realm" : randomAlphaOfLengthBetween(1, 16);
            final String type = randomAlphaOfLengthBetween(5, 16);
            Authentication authentication = AuthenticationTestHelper.builder()
                .user(new User("authenticated", "runas"))
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

        // the user that authenticated for the run as request
        try (StoredContext ignore = threadContext.newStoredContext()) {
            final String nodeName = randomBoolean() ? "node" : randomAlphaOfLengthBetween(1, 8);
            final String realmName = randomBoolean() ? "realm" : randomAlphaOfLengthBetween(1, 16);
            final String type = randomAlphaOfLengthBetween(5, 16);
            Authentication authentication = AuthenticationTestHelper.builder()
                .user(new User("authenticated", "runas"))
                .realmRef(new RealmRef(realmName, type, nodeName))
                .build(false);
            authentication.writeToContext(threadContext);
            threadContext.putTransient(ORIGINATING_ACTION_KEY, "action");
            threadContext.putTransient(
                AUTHORIZATION_INFO_KEY,
                (AuthorizationInfo) () -> Collections.singletonMap(
                    PRINCIPAL_ROLES_FIELD_NAME,
                    authentication.getEffectiveSubject().getUser().roles()
                )
            );
            final InternalScrollSearchRequest request = new InternalScrollSearchRequest();
            SearchContextMissingException expected = expectThrows(
                SearchContextMissingException.class,
                () -> listener.validateReaderContext(readerContext, request)
            );
            assertEquals(readerContext.id(), expected.contextId());
            assertThat(threadContext.getTransient(AuthorizationServiceField.INDICES_PERMISSIONS_KEY), nullValue());
            verify(auditTrail).accessDenied(
                eq(null),
                eq(authentication),
                eq("action"),
                eq(request),
                authzInfoRoles(authentication.getEffectiveSubject().getUser().roles())
            );
        }
    }
}