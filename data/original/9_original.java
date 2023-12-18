public void testStopAtCheckpoint() throws Exception {
    TransformConfig config = new TransformConfig(
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

    for (IndexerState state : IndexerState.values()) {
        // skip indexing case, tested below
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
            // shouldStopAtCheckpoint should not be set, because the indexer is already stopped, stopping or aborting
            assertFalse(context.shouldStopAtCheckpoint());
            assertFalse(indexer.getPersistedState().shouldStopAtNextCheckpoint());
        }
    }

    // test the case that the indexer is at a checkpoint already
    {
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
        // shouldStopAtCheckpoint should not be set, the indexer was started, however at a checkpoint
        assertFalse(context.shouldStopAtCheckpoint());
        assertFalse(indexer.getPersistedState().shouldStopAtNextCheckpoint());
    }

    // lets test a running indexer
    AtomicReference<IndexerState> state = new AtomicReference<>(IndexerState.STARTED);
    {
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

        // listener must have been called by the indexing thread
        assertEquals(1, indexer.getSaveStateListenerCallCount());

        // as the state is stopped it should go back to directly
        assertResponse(listener -> setStopAtCheckpoint(indexer, true, listener));
        assertEquals(1, indexer.getSaveStateListenerCallCount());
    }

    // do another round
    {
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

        // this time call it 3 times
        assertResponse(listener -> setStopAtCheckpoint(indexer, true, listener));
        assertResponse(listener -> setStopAtCheckpoint(indexer, true, listener));
        assertResponse(listener -> setStopAtCheckpoint(indexer, true, listener));

        indexer.stop();
        assertBusy(() -> assertThat(indexer.getState(), equalTo(IndexerState.STOPPED)), 5, TimeUnit.SECONDS);

        // listener must have been called by the indexing thread between 1 and 3 times
        assertThat(indexer.getSaveStateListenerCallCount(), greaterThanOrEqualTo(1));
        assertThat(indexer.getSaveStateListenerCallCount(), lessThanOrEqualTo(3));
    }

    // 3rd round with some back and forth
    {
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

        // slow down the indexer
        CountDownLatch searchLatch = indexer.createAwaitForSearchLatch(1);

        // this time call 5 times and change stopAtCheckpoint every time
        List<CountDownLatch> responseLatches = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            CountDownLatch latch = new CountDownLatch(1);
            boolean stopAtCheckpoint = i % 2 == 0;
            countResponse(listener -> setStopAtCheckpoint(indexer, stopAtCheckpoint, listener), latch);
            responseLatches.add(latch);
        }

        // now let the indexer run again
        searchLatch.countDown();

        indexer.stop();
        assertBusy(() -> assertThat(indexer.getState(), equalTo(IndexerState.STOPPED)), 5, TimeUnit.SECONDS);

        // wait for all listeners
        for (CountDownLatch l : responseLatches) {
            assertTrue("timed out after 5s", l.await(5, TimeUnit.SECONDS));
        }

        // listener must have been called 5 times, because the value changed every time and we slowed down the indexer
        assertThat(indexer.getSaveStateListenerCallCount(), equalTo(5));
    }

    // 4th round: go wild
    {
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

        // slow down the indexer
        CountDownLatch searchLatch = indexer.createAwaitForSearchLatch(1);

        List<CountDownLatch> responseLatches = new ArrayList<>();
        // default stopAtCheckpoint is false
        boolean previousStopAtCheckpoint = false;

        for (int i = 0; i < 3; ++i) {
            CountDownLatch latch = new CountDownLatch(1);
            boolean stopAtCheckpoint = randomBoolean();
            previousStopAtCheckpoint = stopAtCheckpoint;
            countResponse(listener -> setStopAtCheckpoint(indexer, stopAtCheckpoint, listener), latch);
            responseLatches.add(latch);
        }

        // now let the indexer run again
        searchLatch.countDown();

        // call it 3 times again
        for (int i = 0; i < 3; ++i) {
            boolean stopAtCheckpoint = randomBoolean();
            previousStopAtCheckpoint = stopAtCheckpoint;
            assertResponse(listener -> setStopAtCheckpoint(indexer, stopAtCheckpoint, listener));
        }

        indexer.stop();
        assertBusy(() -> assertThat(indexer.getState(), equalTo(IndexerState.STOPPED)), 5, TimeUnit.SECONDS);

        // wait for all listeners
        for (CountDownLatch l : responseLatches) {
            assertTrue("timed out after 5s", l.await(5, TimeUnit.SECONDS));
        }

        // there should be no listeners waiting
        assertEquals(0, indexer.getSaveStateListenerCount());

        // listener must have been called by the indexing thread between timesStopAtCheckpointChanged and 6 times
        // this is not exact, because we do not know _when_ the other thread persisted the flag
        assertThat(indexer.getSaveStateListenerCallCount(), lessThanOrEqualTo(6));
    }
}