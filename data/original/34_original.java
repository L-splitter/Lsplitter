public void testFutureBash() {
  if (isWindows()) {
    return; // TODO: b/136041958 - Running very slowly on Windows CI.
  }
  final CyclicBarrier barrier =
      new CyclicBarrier(
          6 // for the setter threads
              + 50 // for the listeners
              + 50 // for the blocking get threads,
              + 1); // for the main thread
  final ExecutorService executor = Executors.newFixedThreadPool(barrier.getParties());
  final AtomicReference<AbstractFuture<String>> currentFuture = Atomics.newReference();
  final AtomicInteger numSuccessfulSetCalls = new AtomicInteger();
  Callable<@Nullable Void> completeSuccessfullyRunnable =
      new Callable<@Nullable Void>() {
        @Override
        public @Nullable Void call() {
          if (currentFuture.get().set("set")) {
            numSuccessfulSetCalls.incrementAndGet();
          }
          awaitUnchecked(barrier);
          return null;
        }
      };
  Callable<@Nullable Void> completeExceptionallyRunnable =
      new Callable<@Nullable Void>() {
        Exception failureCause = new Exception("setException");

        @Override
        public @Nullable Void call() {
          if (currentFuture.get().setException(failureCause)) {
            numSuccessfulSetCalls.incrementAndGet();
          }
          awaitUnchecked(barrier);
          return null;
        }
      };
  Callable<@Nullable Void> cancelRunnable =
      new Callable<@Nullable Void>() {
        @Override
        public @Nullable Void call() {
          if (currentFuture.get().cancel(true)) {
            numSuccessfulSetCalls.incrementAndGet();
          }
          awaitUnchecked(barrier);
          return null;
        }
      };
  Callable<@Nullable Void> setFutureCompleteSuccessfullyRunnable =
      new Callable<@Nullable Void>() {
        ListenableFuture<String> future = Futures.immediateFuture("setFuture");

        @Override
        public @Nullable Void call() {
          if (currentFuture.get().setFuture(future)) {
            numSuccessfulSetCalls.incrementAndGet();
          }
          awaitUnchecked(barrier);
          return null;
        }
      };
  Callable<@Nullable Void> setFutureCompleteExceptionallyRunnable =
      new Callable<@Nullable Void>() {
        ListenableFuture<String> future =
            Futures.immediateFailedFuture(new Exception("setFuture"));

        @Override
        public @Nullable Void call() {
          if (currentFuture.get().setFuture(future)) {
            numSuccessfulSetCalls.incrementAndGet();
          }
          awaitUnchecked(barrier);
          return null;
        }
      };
  Callable<@Nullable Void> setFutureCancelRunnable =
      new Callable<@Nullable Void>() {
        ListenableFuture<String> future = Futures.immediateCancelledFuture();

        @Override
        public @Nullable Void call() {
          if (currentFuture.get().setFuture(future)) {
            numSuccessfulSetCalls.incrementAndGet();
          }
          awaitUnchecked(barrier);
          return null;
        }
      };
  final Set<Object> finalResults = Collections.synchronizedSet(Sets.newIdentityHashSet());
  Runnable collectResultsRunnable =
      new Runnable() {
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
  Runnable collectResultsTimedGetRunnable =
      new Runnable() {
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
  List<Callable<?>> allTasks = new ArrayList<>();
  allTasks.add(completeSuccessfullyRunnable);
  allTasks.add(completeExceptionallyRunnable);
  allTasks.add(cancelRunnable);
  allTasks.add(setFutureCompleteSuccessfullyRunnable);
  allTasks.add(setFutureCompleteExceptionallyRunnable);
  allTasks.add(setFutureCancelRunnable);
  for (int k = 0; k < 50; k++) {
    // For each listener we add a task that submits it to the executor directly for the blocking
    // get use case and another task that adds it as a listener to the future to exercise both
    // racing addListener calls and addListener calls completing after the future completes.
    final Runnable listener =
        k % 2 == 0 ? collectResultsRunnable : collectResultsTimedGetRunnable;
    allTasks.add(Executors.callable(listener));
    allTasks.add(
        new Callable<@Nullable Void>() {
          @Override
          public @Nullable Void call() throws Exception {
            currentFuture.get().addListener(listener, executor);
            return null;
          }
        });
  }
  assertEquals(allTasks.size() + 1, barrier.getParties());
  for (int i = 0; i < 1000; i++) {
    Collections.shuffle(allTasks);
    final AbstractFuture<String> future = new AbstractFuture<String>() {};
    currentFuture.set(future);
    for (Callable<?> task : allTasks) {
      @SuppressWarnings("unused") // https://errorprone.info/bugpattern/FutureReturnValueIgnored
      Future<?> possiblyIgnoredError = executor.submit(task);
    }
    awaitUnchecked(barrier);
    assertThat(future.isDone()).isTrue();
    // inspect state and ensure it is correct!
    // asserts that all get calling threads received the same value
    Object result = Iterables.getOnlyElement(finalResults);
    if (result == CancellationException.class) {
      assertTrue(future.isCancelled());
      if (future.wasInterrupted()) {
        // We were cancelled, it is possible that setFuture could have succeeded too.
        assertThat(numSuccessfulSetCalls.get()).isIn(Range.closed(1, 2));
      } else {
        assertThat(numSuccessfulSetCalls.get()).isEqualTo(1);
      }
    } else {
      assertThat(numSuccessfulSetCalls.get()).isEqualTo(1);
    }
    // reset for next iteration
    numSuccessfulSetCalls.set(0);
    finalResults.clear();
  }
  executor.shutdown();
}