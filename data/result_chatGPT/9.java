/**
 * Tests stopping at a checkpoint for different indexer states.
 *
 * @throws Exception if an error occurs during the test
 */
public void testStopAtCheckpoint() throws Exception {
    // Set up the transform configuration
    TransformConfig config = setupTransformConfig();
    // Test different indexer states
    testStopAtCheckpointForIndexerStates(config);
    // Test the case when the indexer is already at a checkpoint
    testStopAtCheckpointForCheckpointIndexer(config);
    // Test a running indexer
    testStopAtCheckpointForRunningIndexer(config);
    // Test multiple rounds with back and forth
    testStopAtCheckpointWithBackAndForth(config);
    // Test wild scenarios
    testStopAtCheckpointWildScenarios(config);
}
/**
 * Sets up the transform configuration.
 *
 * @return the transform configuration
 */
private TransformConfig setupTransformConfig() {
    return new TransformConfig(
            randomAlphaOfLength(10),
            randomSourceConfig(),
            randomDestConfig(),
            null,
            new TimeSyncConfig("timestamp", TimeValue.timeValueSeconds(1)),
            null,
            randomPivotConfig(),
            null,
            randomBoolean() ? null : randomAlphaOfLengthBetween(1, 1000),
            null,
            null,
            null,
            null,
            null
    );
}
/**
 * Tests stopping at a checkpoint for different indexer states.
 *
 * @param config the transform configuration
 * @throws Exception if an error occurs during the test
 */
private void testStopAtCheckpointForIndexerStates(TransformConfig config) throws Exception {
    for (IndexerState state : IndexerState.values()) {
        if (IndexerState.INDEXING.equals(state)) {
            continue;
        }
        AtomicReference<IndexerState> stateRef = new AtomicReference<>(state);
        TransformContext context = new TransformContext(TransformTaskState.STARTED, "", 0, mock(TransformContext.Listener.class));
        final MockedTransformIndexer indexer = createMockIndexer(
                config,
                stateRef,
                null,
                threadPool,
                auditor,
                new TransformIndexerPosition(Collections.singletonMap("afterkey", "value"), Collections.emptyMap()),
                new TransformIndexerStats(),
                context
        );
        assertResponse(listener -> setStopAtCheckpoint(indexer, true, listener));
        assertEquals(0, indexer.getSaveStateListenerCallCount());
        if (IndexerState.STARTED.equals(state)) {
            assertTrue(context.shouldStopAtCheckpoint());
            assertTrue(indexer.getPersistedState().shouldStopAtNextCheckpoint());
        } else {
            assertFalse(context.shouldStopAtCheckpoint());
            assertFalse(indexer.getPersistedState().shouldStopAtNextCheckpoint());
        }
    }
}
/**
 * Tests stopping at a checkpoint when the indexer is already at a checkpoint.
 *
 * @param config the transform configuration
 * @throws Exception if an error occurs during the test
 */
private void testStopAtCheckpointForCheckpointIndexer(TransformConfig config) throws Exception {
    AtomicReference<IndexerState> stateRef = new AtomicReference<>(IndexerState.STARTED);
    TransformContext context = new TransformContext(TransformTaskState.STARTED, "", 0, mock(TransformContext.Listener.class));
    final MockedTransformIndexer indexer = createMockIndexer(
            config,
            stateRef,
            null,
            threadPool,
            auditor,
            null,
            new TransformIndexerStats(),
            context
    );
    assertResponse(listener -> setStopAtCheckpoint(indexer, true, listener));
    assertEquals(0, indexer.getSaveStateListenerCallCount());
    assertFalse(context.shouldStopAtCheckpoint());
    assertFalse(indexer.getPersistedState().shouldStopAtNextCheckpoint());
}
/**
 * Tests stopping at a checkpoint for a running indexer.
 *
 * @param config the transform configuration
 * @throws Exception if an error occurs during the test
 */
private void testStopAtCheckpointForRunningIndexer(TransformConfig config) throws Exception {
    AtomicReference<IndexerState> state = new AtomicReference<>(IndexerState.STARTED);
    TransformContext context = new TransformContext(TransformTaskState.STARTED, "", 0, mock(TransformContext.Listener.class));
    final MockedTransformIndexer indexer = createMockIndexer(
            config,
            state,
            null,
            threadPool,
            auditor,
            null,
            new TransformIndexerStats(),
            context
    );
    indexer.start();
    assertTrue(indexer.maybeTriggerAsyncJob(System.currentTimeMillis()));
    assertEquals(indexer.getState(), IndexerState.INDEXING);
    assertResponse(listener -> setStopAtCheckpoint(indexer, true, listener));
    indexer.stop();
    assertBusy(() -> assertThat(indexer.getState(), equalTo(IndexerState.STOPPED)), 5, TimeUnit.SECONDS);
    assertEquals(1, indexer.getSaveStateListenerCallCount());
    assertResponse(listener -> setStopAtCheckpoint(indexer, true, listener));
    assertEquals(1, indexer.getSaveStateListenerCallCount());
}
/**
 * Tests stopping at a checkpoint for multiple rounds with back and forth.
 *
 * @param config the transform configuration
 * @throws Exception if an error occurs during the test
 */
private void testStopAtCheckpointWithBackAndForth(TransformConfig config) throws Exception {
    AtomicReference<IndexerState> state = new AtomicReference<>(IndexerState.STARTED);
    TransformContext context = new TransformContext(TransformTaskState.STARTED, "", 0, mock(TransformContext.Listener.class));
    final MockedTransformIndexer indexer = createMockIndexer(
            config,
            state,
            null,
            threadPool,
            auditor,
            null,
            new TransformIndexerStats(),
            context
    );
    indexer.start();
    assertTrue(indexer.maybeTriggerAsyncJob(System.currentTimeMillis()));
    assertEquals(indexer.getState(), IndexerState.INDEXING);
    CountDownLatch searchLatch = indexer.createAwaitForSearchLatch(1);
    List<CountDownLatch> responseLatches = new ArrayList<>();
    for (int i = 0; i < 5; ++i) {
        CountDownLatch latch = new CountDownLatch(1);
        boolean stopAtCheckpoint = i % 2 == 0;
        countResponse(listener -> setStopAtCheckpoint(indexer, stopAtCheckpoint, listener), latch);
        responseLatches.add(latch);
    }
    searchLatch.countDown();
    indexer.stop();
    assertBusy(() -> assertThat(indexer.getState(), equalTo(IndexerState.STOPPED)), 5, TimeUnit.SECONDS);
    for (CountDownLatch l : responseLatches) {
        assertTrue("timed out after 5s", l.await(5, TimeUnit.SECONDS));
    }
    assertEquals(5, indexer.getSaveStateListenerCallCount());
}
/**
 * Tests stopping at a checkpoint for wild scenarios.
 *
 * @param config the transform configuration
 * @throws Exception if an error occurs during the test
 */
private void testStopAtCheckpointWildScenarios(TransformConfig config) throws Exception {
    AtomicReference<IndexerState> state = new AtomicReference<>(IndexerState.STARTED);
    TransformContext context = new TransformContext(TransformTaskState.STARTED, "", 0, mock(TransformContext.Listener.class));
    final MockedTransformIndexer indexer = createMockIndexer(
            config,
            state,
            null,
            threadPool,
            auditor,
            null,
            new TransformIndexerStats(),
            context
    );
    indexer.start();
    assertTrue(indexer.maybeTriggerAsyncJob(System.currentTimeMillis()));
    assertEquals(indexer.getState(), IndexerState.INDEXING);
    CountDownLatch searchLatch = indexer.createAwaitForSearchLatch(1);
    List<CountDownLatch> responseLatches = new ArrayList<>();
    boolean previousStopAtCheckpoint = false;
    for (int i = 0; i < 3; ++i) {
        CountDownLatch latch = new CountDownLatch(1);
        boolean stopAtCheckpoint = randomBoolean();
        previousStopAtCheckpoint = stopAtCheckpoint;
        countResponse(listener -> setStopAtCheckpoint(indexer, stopAtCheckpoint, listener), latch);
        responseLatches.add(latch);
    }
    searchLatch.countDown();
    for (int i = 0; i < 3; ++i) {
        boolean stopAtCheckpoint = randomBoolean();
        previousStopAtCheckpoint = stopAtCheckpoint;
        assertResponse(listener -> setStopAtCheckpoint(indexer, stopAtCheckpoint, listener));
    }
    indexer.stop();
    assertBusy(() -> assertThat(indexer.getState(), equalTo(IndexerState.STOPPED)), 5, TimeUnit.SECONDS);
    for (CountDownLatch l : responseLatches) {
        assertTrue("timed out after 5s", l.await(5, TimeUnit.SECONDS));
    }
    assertEquals(0, indexer.getSaveStateListenerCount());
    assertThat(indexer.getSaveStateListenerCallCount(), lessThanOrEqualTo(6));
}
