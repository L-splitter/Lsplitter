/**
 * Checks if the operating system is Windows.
 *
 * @return true if the operating system is Windows, false otherwise.
 */
private boolean isWindows() {
    return System.getProperty("os.name").startsWith("Windows");
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
    Runnable cancelRunnable = createFutureOperationRunnable(currentFuture, null, barrier, cancellationSuccess, "cancel");
createFutureOperationRunnable(currentFuture, setFutureFuture, barrier, setFutureSetSuccess, "setFuture");
    final Set<Object> finalResults = Collections.synchronizedSet(Sets.newIdentityHashSet());
    Runnable collectResultsRunnable = createCollectResultsRunnable(currentFuture, barrier, finalResults, false);
createCollectResultsRunnable(currentFuture, barrier, finalResults, true);
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

/**
 * Creates a new Runnable for collecting the results of the current future.
 * @param currentFuture the current future to collect results from.
 * @param barrier the CyclicBarrier to synchronize threads.
 * @param finalResults the Set to store the results.
 * @param timedGet a boolean to determine if a timed get should be used.
 * @return the created Runnable.
 */
private Runnable createCollectResultsRunnable(final AtomicReference<AbstractFuture<String>> currentFuture, final CyclicBarrier barrier, final Set<Object> finalResults, boolean timedGet) {
    return new Runnable() {
        @Override
        public void run() {
            if (timedGet) {
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
                    }
                }
            } else {
                try {
                    String result = Uninterruptibles.getUninterruptibly(currentFuture.get());
                    finalResults.add(result);
                } catch (ExecutionException e) {
                    finalResults.add(e.getCause());
                } catch (CancellationException e) {
                    finalResults.add(CancellationException.class);
                }
            }
            awaitUnchecked(barrier);
        }
    };
}


/** 
 * Creates a new Runnable for performing the specified operation on the current future.
 * @param currentFuture the current future to be operated on.
 * @param setFutureFuture the future to be set to the current future.
 * @param barrier the CyclicBarrier to synchronize threads.
 * @param operationSuccess the AtomicBoolean to store the success status of the operation.
 * @param operationType the type of operation to be performed.
 * @return the created Runnable.
 */
private Runnable createFutureOperationRunnable(final AtomicReference<AbstractFuture<String>> currentFuture, final AtomicReference<AbstractFuture<String>> setFutureFuture, final CyclicBarrier barrier, final AtomicBoolean operationSuccess, final String operationType){
  return new Runnable(){
    @Override public void run(){
      if(operationType.equals("cancel")) {
        operationSuccess.set(currentFuture.get().cancel(true));
      } else if(operationType.equals("setFuture")) {
        AbstractFuture<String> future=setFutureFuture.get();
        operationSuccess.set(currentFuture.get().setFuture(future));
        operationSuccess.set(future.set("hello-async-world"));
      }
      awaitUnchecked(barrier);
    }
  };
}
