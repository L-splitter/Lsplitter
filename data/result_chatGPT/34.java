/**
 * Checks if the current operating system is Windows.
 *
 * @return true if the operating system is Windows, false otherwise.
 */
private boolean isWindows() {
    return System.getProperty("os.name").startsWith("Windows");
}
/**
 * Creates a new CyclicBarrier with the specified number of parties.
 *
 * @return a new CyclicBarrier.
 */
private CyclicBarrier createBarrier() {
    return new CyclicBarrier(
        6 // for the setter threads
            + 50 // for the listeners
            + 50 // for the blocking get threads,
            + 1); // for the main thread
}
/**
 * Creates a new ExecutorService with a fixed thread pool.
 *
 * @param barrier the CyclicBarrier to determine the number of threads in the pool.
 * @return a new ExecutorService.
 */
private ExecutorService createExecutorService(CyclicBarrier barrier) {
    return Executors.newFixedThreadPool(barrier.getParties());
}
/**
 * Creates a new AtomicReference for the current future.
 *
 * @return a new AtomicReference.
 */
private AtomicReference<AbstractFuture<String>> createCurrentFuture() {
    return Atomics.newReference();
}
/**
 * Creates a new AtomicInteger for the number of successful set calls.
 *
 * @return a new AtomicInteger.
 */
private AtomicInteger createNumSuccessfulSetCalls() {
    return new AtomicInteger();
}
/**
 * Creates a new Set for the final results.
 *
 * @return a new Set.
 */
private Set<Object> createFinalResults() {
    return Collections.synchronizedSet(Sets.newIdentityHashSet());
}
/**
 * Creates a new List for all tasks.
 *
 * @return a new List.
 */
private List<Callable<?>> createAllTasks() {
    return new ArrayList<>();
}
/**
 * Shuffles the tasks and submits them to the executor.
 *
 * @param allTasks the list of all tasks.
 * @param executor the executor service.
 * @param currentFuture the current future.
 */
private void shuffleAndSubmitTasks(List<Callable<?>> allTasks, ExecutorService executor, AtomicReference<AbstractFuture<String>> currentFuture) {
    for (int i = 0; i < 1000; i++) {
        Collections.shuffle(allTasks);
        final AbstractFuture<String> future = new AbstractFuture<String>() {};
        currentFuture.set(future);
        for (Callable<?> task : allTasks) {
            @SuppressWarnings("unused") // https://errorprone.info/bugpattern/FutureReturnValueIgnored
            Future<?> possiblyIgnoredError = executor.submit(task);
        }
    }
}
/**
 * Checks the state of the future and ensures it is correct.
 *
 * @param future the future to check.
 * @param numSuccessfulSetCalls the number of successful set calls.
 * @param finalResults the final results.
 */
private void checkFutureState(AbstractFuture<String> future, AtomicInteger numSuccessfulSetCalls, Set<Object> finalResults) {
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
public void testFutureBash() {
    if (isWindows()) {
        return; // TODO: b/136041958 - Running very slowly on Windows CI.
    }
    final CyclicBarrier barrier = createBarrier();
    final ExecutorService executor = createExecutorService(barrier);
    final AtomicReference<AbstractFuture<String>> currentFuture = createCurrentFuture();
    final AtomicInteger numSuccessfulSetCalls = createNumSuccessfulSetCalls();
    // ... create the Callable and Runnable instances here ...
    final Set<Object> finalResults = createFinalResults();
    List<Callable<?>> allTasks = createAllTasks();
    // ... add the Callable and Runnable instances to allTasks here ...
    shuffleAndSubmitTasks(allTasks, executor, currentFuture);
    checkFutureState(currentFuture.get(), numSuccessfulSetCalls, finalResults);
    executor.shutdown();
}
