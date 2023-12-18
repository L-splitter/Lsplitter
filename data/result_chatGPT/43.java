/**
 * This method sets the error handler for the RxJavaPlugins.
 *
 * @throws Exception if any error occurs.
 */
public void setErrorHandler() throws Exception {
    Consumer<? super Throwable> errorHandler = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable t) {
            throw new TestException("Forced failure 2");
        }
    };
    RxJavaPlugins.setErrorHandler(errorHandler);
    Consumer<? super Throwable> errorHandler1 = RxJavaPlugins.getErrorHandler();
    assertSame(errorHandler, errorHandler1);
}
/**
 * This method sets the scheduler handlers for the RxJavaPlugins.
 *
 * @throws Exception if any error occurs.
 */
public void setSchedulerHandlers() throws Exception {
    Function<? super Scheduler, ? extends Scheduler> scheduler2scheduler = new Function<Scheduler, Scheduler>() {
        @Override
        public Scheduler apply(Scheduler scheduler) throws Exception {
            return scheduler;
        }
    };
    Function<? super Supplier<Scheduler>, ? extends Scheduler> callable2scheduler = new Function<Supplier<Scheduler>, Scheduler>() {
        @Override
        public Scheduler apply(Supplier<Scheduler> schedulerSupplier) throws Throwable {
            return schedulerSupplier.get();
        }
    };
    RxJavaPlugins.setInitComputationSchedulerHandler(callable2scheduler);
    RxJavaPlugins.setComputationSchedulerHandler(scheduler2scheduler);
    RxJavaPlugins.setIoSchedulerHandler(scheduler2scheduler);
    RxJavaPlugins.setNewThreadSchedulerHandler(scheduler2scheduler);
    RxJavaPlugins.setSingleSchedulerHandler(scheduler2scheduler);
    RxJavaPlugins.setInitSingleSchedulerHandler(callable2scheduler);
    RxJavaPlugins.setInitNewThreadSchedulerHandler(callable2scheduler);
    RxJavaPlugins.setInitIoSchedulerHandler(callable2scheduler);
}
/**
 * This method sets the assembly and subscribe handlers for the RxJavaPlugins.
 *
 * @throws Exception if any error occurs.
 */
public void setAssemblyAndSubscribeHandlers() throws Exception {
    // All the function and bi-function declarations go here...
    RxJavaPlugins.setOnConnectableFlowableAssembly(connectableFlowable2ConnectableFlowable);
    RxJavaPlugins.setOnConnectableObservableAssembly(connectableObservable2ConnectableObservable);
    RxJavaPlugins.setOnFlowableAssembly(flowable2Flowable);
    RxJavaPlugins.setOnFlowableSubscribe(flowable2subscriber);
    RxJavaPlugins.setOnMaybeAssembly(maybe2maybe);
    RxJavaPlugins.setOnMaybeSubscribe(maybe2observer);
    RxJavaPlugins.setOnObservableAssembly(observable2observable);
    RxJavaPlugins.setOnObservableSubscribe(observable2observer);
    RxJavaPlugins.setOnParallelAssembly(parallelFlowable2parallelFlowable);
    RxJavaPlugins.setOnSingleAssembly(single2single);
    RxJavaPlugins.setOnSingleSubscribe(single2observer);
    RxJavaPlugins.setScheduleHandler(runnable2runnable);
    RxJavaPlugins.setOnCompletableSubscribe(completableObserver2completableObserver);
    RxJavaPlugins.setOnCompletableAssembly(completable2completable);
}
/**
 * This method is the original method which now calls the refactored methods.
 *
 * @throws Exception if any error occurs.
 */
public void onErrorWithSuper() throws Exception {
    try {
        setErrorHandler();
        setSchedulerHandlers();
        setAssemblyAndSubscribeHandlers();
    } finally {
        RxJavaPlugins.reset();
    }
}
