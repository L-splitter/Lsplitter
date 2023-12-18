public void testSetFutureCancelBash() {
  if (isWindows()) {
    return; // TODO: b/136041958 - Running very slowly on Windows CI.
  }
  final int size = 50;
  final CyclicBarrier barrier =
      new CyclicBarrier(
          2 // for the setter threads
              + size // for the listeners
              + size // for the get threads,
              + 1); // for the main thread
  final ExecutorService executor = Executors.newFixedThreadPool(barrier.getParties());
  final AtomicReference<AbstractFuture<String>> currentFuture = Atomics.newReference();
  final AtomicReference<AbstractFuture<String>> setFutureFuture = Atomics.newReference();
  final AtomicBoolean setFutureSetSuccess = new AtomicBoolean();
  final AtomicBoolean setFutureCompletionSuccess = new AtomicBoolean();
  final AtomicBoolean cancellationSuccess = new AtomicBoolean();
  Runnable cancelRunnable =
      new Runnable() {
        @Override
        public void run() {
          cancellationSuccess.set(currentFuture.get().cancel(true));
          awaitUnchecked(barrier);
        }
      };
  Runnable setFutureCompleteSuccessfullyRunnable =
      new Runnable() {
        @Override
        public void run() {
          AbstractFuture<String> future = setFutureFuture.get();
          setFutureSetSuccess.set(currentFuture.get().setFuture(future));
          setFutureCompletionSuccess.set(future.set("hello-async-world"));
          awaitUnchecked(barrier);
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
  List<Runnable> allTasks = new ArrayList<>();
  allTasks.add(cancelRunnable);
  allTasks.add(setFutureCompleteSuccessfullyRunnable);
  for (int k = 0; k < size; k++) {
    // For each listener we add a task that submits it to the executor directly for the blocking
    // get use case and another task that adds it as a listener to the future to exercise both
    // racing addListener calls and addListener calls completing after the future completes.
    final Runnable listener =
        k % 2 == 0 ? collectResultsRunnable : collectResultsTimedGetRunnable;
    allTasks.add(listener);
    allTasks.add(
        new Runnable() {
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
    // inspect state and ensure it is correct!
    // asserts that all get calling threads received the same value
    Object result = Iterables.getOnlyElement(finalResults);
    if (result == CancellationException.class) {
      assertTrue(future.isCancelled());
      assertTrue(cancellationSuccess.get());
      // cancellation can interleave in 3 ways
      // 1. prior to setFuture
      // 2. after setFuture before set() on the future assigned
      // 3. after setFuture and set() are called but before the listener completes.
      if (!setFutureSetSuccess.get() || !setFutureCompletionSuccess.get()) {
        // If setFuture fails or set on the future fails then it must be because that future was
        // cancelled
        assertTrue(setFuture.isCancelled());
        assertTrue(setFuture.wasInterrupted()); // we only call cancel(true)
      }
    } else {
      // set on the future completed
      assertFalse(cancellationSuccess.get());
      assertTrue(setFutureSetSuccess.get());
      assertTrue(setFutureCompletionSuccess.get());
    }
    // reset for next iteration
    setFutureSetSuccess.set(false);
    setFutureCompletionSuccess.set(false);
    cancellationSuccess.set(false);
    finalResults.clear();
  }
  executor.shutdown();
}