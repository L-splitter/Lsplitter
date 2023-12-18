/**
 * Checks if the operating system is Windows.
 *
 * @return true if the operating system is Windows, false otherwise.
 */
private boolean isWindows() {
    return System.getProperty("os.name").startsWith("Windows");
}
/**
 * Creates a new Runnable for cancelling the current future.
 *
 * @param currentFuture the current future to be cancelled.
 * @param barrier the CyclicBarrier to synchronize threads.
 * @param cancellationSuccess the AtomicBoolean to store the success status of the cancellation.
 * @return the created Runnable.
 */
private Runnable createCancelRunnable(final AtomicReference<AbstractFuture<String>> currentFuture, final CyclicBarrier barrier, final AtomicBoolean cancellationSuccess) {
    return new Runnable() {
        @Override
        public void run() {
            cancellationSuccess.set(currentFuture.get().cancel(true));
            awaitUnchecked(barrier);
        }
    };
}
/**
 * Creates a new Runnable for setting the future and completing it successfully.
 *
 * @param currentFuture the current future to be set.
 * @param setFutureFuture the future to be set to the current future.
 * @param barrier the CyclicBarrier to synchronize threads.
 * @param setFutureSetSuccess the AtomicBoolean to store the success status of setting the future.
 * @param setFutureCompletionSuccess the AtomicBoolean to store the success status of completing the future.
 * @return the created Runnable.
 */
private Runnable createSetFutureCompleteSuccessfullyRunnable(final AtomicReference<AbstractFuture<String>> currentFuture, final AtomicReference<AbstractFuture<String>> setFutureFuture, final CyclicBarrier barrier, final AtomicBoolean setFutureSetSuccess, final AtomicBoolean setFutureCompletionSuccess) {
    return new Runnable() {
        @Override
        public void run() {
            AbstractFuture<String> future = setFutureFuture.get();
            setFutureSetSuccess.set(currentFuture.get().setFuture(future));
            setFutureCompletionSuccess.set(future.set("hello-async-world"));
            awaitUnchecked(barrier);
        }
    };
}
/**
 * Creates a new Runnable for collecting the results of the current future.
 *
 * @param currentFuture the current future to collect results from.
 * @param barrier the CyclicBarrier to synchronize threads.
 * @param finalResults the Set to store the results.
 * @return the created Runnable.
 */
private Runnable createCollectResultsRunnable(final AtomicReference<AbstractFuture<String>> currentFuture, final CyclicBarrier barrier, final Set<Object> finalResults) {
    return new Runnable() {
        @Override
        public void run() {
            try {
                String result = Uninterruptibles.getUninterruptibly(currentFuture.get());
                finalResults.add(result);
            } catch (ExecutionException e) {
                finalResults.add(e.getCause());
            } catch (CancellationException e) {
                finalResults.add(CancellationException.class);
            } finally {
                awaitUnchecked(barrier);
            }
        }
    };
}
/**
 * Creates a new Runnable for collecting the results of the current future with a timed get.
 *
 * @param currentFuture the current future to collect results from.
 * @param barrier the CyclicBarrier to synchronize threads.
 * @param finalResults the Set to store the results.
 * @return the created Runnable.
 */
private Runnable createCollectResultsTimedGetRunnable(final AtomicReference<AbstractFuture<String>> currentFuture, final CyclicBarrier barrier, final Set<Object> finalResults) {
    return new Runnable() {
        @Override
        public void run() {
            Future<String> future = currentFuture.get();
            while (true) {
                try {
                    String result = Uninterruptibles.getUninterruptibly(future, 0, TimeUnit.SECONDS);
                    finalResults.add(result);
                    break;
                } catch (ExecutionException e) {
                    finalResults.add(e.getCause());
                    break;
                } catch (CancellationException e) {
                    finalResults.add(CancellationException.class);
                    break;
                } catch (TimeoutException e) {
                    // loop
                }
            }
            awaitUnchecked(barrier);
        }
    };
}
public void testSetFutureCancelBash() {
    if (isWindows()) {
        return; // TODO: b/136041958 - Running very slowly on Windows CI.
    }
    final int size = 50;
    final CyclicBarrier barrier = new CyclicBarrier(2 + size + size + 1);
    final ExecutorService executor = Executors.newFixedThreadPool(barrier.getParties());
    final AtomicReference<AbstractFuture<String>> currentFuture = Atomics.newReference();
    final AtomicReference<AbstractFuture<String>> setFutureFuture = Atomics.newReference();
    final AtomicBoolean setFutureSetSuccess = new AtomicBoolean();
    final AtomicBoolean setFutureCompletionSuccess = new AtomicBoolean();
    final AtomicBoolean cancellationSuccess = new AtomicBoolean();
    Runnable cancelRunnable = createCancelRunnable(currentFuture, barrier, cancellationSuccess);
    Runnable setFutureCompleteSuccessfullyRunnable = createSetFutureCompleteSuccessfullyRunnable(currentFuture, setFutureFuture, barrier, setFutureSetSuccess, setFutureCompletionSuccess);
    final Set<Object> finalResults = Collections.synchronizedSet(Sets.newIdentityHashSet());
    Runnable collectResultsRunnable = createCollectResultsRunnable(currentFuture, barrier, finalResults);
    Runnable collectResultsTimedGetRunnable = createCollectResultsTimedGetRunnable(currentFuture, barrier, finalResults);
    List<Runnable> allTasks = new ArrayList<>();
    allTasks.add(cancelRunnable);
    allTasks.add(setFutureCompleteSuccessfullyRunnable);
    for (int k = 0; k < size; k++) {
        final Runnable listener = k % 2 == 0 ? collectResultsRunnable : collectResultsTimedGetRunnable;
        allTasks.add(listener);
        allTasks.add(new Runnable() {
            @Override
            public void run() {
                currentFuture.get().addListener(listener, executor);
            }
        });
    }
    assertEquals(allTasks.size() + 1, barrier.getParties()); // sanity check
    for (int i = 0; i < 1000; i++) {
        Collections.shuffle(allTasks);
        final AbstractFuture<String> future = new AbstractFuture<String>() {};
        final AbstractFuture<String> setFuture = new AbstractFuture<String>() {};
        currentFuture.set(future);
        setFutureFuture.set(setFuture);
        for (Runnable task : allTasks) {
            executor.execute(task);
        }
        awaitUnchecked(barrier);
        assertThat(future.isDone()).isTrue();
        Object result = Iterables.getOnlyElement(finalResults);
        if (result == CancellationException.class) {
            assertTrue(future.isCancelled());
            assertTrue(cancellationSuccess.get());
            if (!setFutureSetSuccess.get() || !setFutureCompletionSuccess.get()) {
                assertTrue(setFuture.isCancelled());
                assertTrue(setFuture.wasInterrupted());
            }
        } else {
            assertFalse(cancellationSuccess.get());
            assertTrue(setFutureSetSuccess.get());
            assertTrue(setFutureCompletionSuccess.get());
        }
        setFutureSetSuccess.set(false);
        setFutureCompletionSuccess.set(false);
        cancellationSuccess.set(false);
        finalResults.clear();
    }
    executor.shutdown();
}
