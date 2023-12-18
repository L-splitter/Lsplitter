public void onErrorWithSuper() throws Exception {
    try {
        Consumer<? super Throwable> errorHandler = new Consumer<Throwable>() {
            @Override
            public void accept(Throwable t) {
                throw new TestException("Forced failure 2");
            }
        };
        RxJavaPlugins.setErrorHandler(errorHandler);

        Consumer<? super Throwable> errorHandler1 = RxJavaPlugins.getErrorHandler();
        assertSame(errorHandler, errorHandler1);

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
        Function<? super ConnectableFlowable, ? extends ConnectableFlowable> connectableFlowable2ConnectableFlowable = new Function<ConnectableFlowable, ConnectableFlowable>() {
            @Override
            public ConnectableFlowable apply(ConnectableFlowable connectableFlowable) throws Exception {
                return connectableFlowable;
            }
        };
        Function<? super ConnectableObservable, ? extends ConnectableObservable> connectableObservable2ConnectableObservable = new Function<ConnectableObservable, ConnectableObservable>() {
            @Override
            public ConnectableObservable apply(ConnectableObservable connectableObservable) throws Exception {
                return connectableObservable;
            }
        };
        Function<? super Flowable, ? extends Flowable> flowable2Flowable = new Function<Flowable, Flowable>() {
            @Override
            public Flowable apply(Flowable flowable) throws Exception {
                return flowable;
            }
        };
        BiFunction<? super Flowable, ? super Subscriber, ? extends Subscriber> flowable2subscriber = new BiFunction<Flowable, Subscriber, Subscriber>() {
            @Override
            public Subscriber apply(Flowable flowable, Subscriber subscriber) throws Exception {
                return subscriber;
            }
        };
        Function<Maybe, Maybe> maybe2maybe = new Function<Maybe, Maybe>() {
            @Override
            public Maybe apply(Maybe maybe) throws Exception {
                return maybe;
            }
        };
        BiFunction<Maybe, MaybeObserver, MaybeObserver> maybe2observer = new BiFunction<Maybe, MaybeObserver, MaybeObserver>() {
            @Override
            public MaybeObserver apply(Maybe maybe, MaybeObserver maybeObserver) throws Exception {
                return maybeObserver;
            }
        };
        Function<Observable, Observable> observable2observable = new Function<Observable, Observable>() {
            @Override
            public Observable apply(Observable observable) throws Exception {
                return observable;
            }
        };
        BiFunction<? super Observable, ? super Observer, ? extends Observer> observable2observer = new BiFunction<Observable, Observer, Observer>() {
            @Override
            public Observer apply(Observable observable, Observer observer) throws Exception {
                return observer;
            }
        };
        Function<? super ParallelFlowable, ? extends ParallelFlowable> parallelFlowable2parallelFlowable = new Function<ParallelFlowable, ParallelFlowable>() {
            @Override
            public ParallelFlowable apply(ParallelFlowable parallelFlowable) throws Exception {
                return parallelFlowable;
            }
        };
        Function<Single, Single> single2single = new Function<Single, Single>() {
            @Override
            public Single apply(Single single) throws Exception {
                return single;
            }
        };
        BiFunction<? super Single, ? super SingleObserver, ? extends SingleObserver> single2observer = new BiFunction<Single, SingleObserver, SingleObserver>() {
            @Override
            public SingleObserver apply(Single single, SingleObserver singleObserver) throws Exception {
                return singleObserver;
            }
        };
        Function<? super Runnable, ? extends Runnable> runnable2runnable = new Function<Runnable, Runnable>() {
            @Override
            public Runnable apply(Runnable runnable) throws Exception {
                return runnable;
            }
        };
        BiFunction<? super Completable, ? super CompletableObserver, ? extends CompletableObserver> completableObserver2completableObserver = new BiFunction<Completable, CompletableObserver, CompletableObserver>() {
            @Override
            public CompletableObserver apply(Completable completable, CompletableObserver completableObserver) throws Exception {
                return completableObserver;
            }
        };
        Function<? super Completable, ? extends Completable> completable2completable = new Function<Completable, Completable>() {
            @Override
            public Completable apply(Completable completable) throws Exception {
                return completable;
            }
        };

        RxJavaPlugins.setInitComputationSchedulerHandler(callable2scheduler);
        RxJavaPlugins.setComputationSchedulerHandler(scheduler2scheduler);
        RxJavaPlugins.setIoSchedulerHandler(scheduler2scheduler);
        RxJavaPlugins.setNewThreadSchedulerHandler(scheduler2scheduler);
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
        RxJavaPlugins.setSingleSchedulerHandler(scheduler2scheduler);
        RxJavaPlugins.setOnCompletableSubscribe(completableObserver2completableObserver);
        RxJavaPlugins.setOnCompletableAssembly(completable2completable);
        RxJavaPlugins.setInitSingleSchedulerHandler(callable2scheduler);
        RxJavaPlugins.setInitNewThreadSchedulerHandler(callable2scheduler);
        RxJavaPlugins.setInitIoSchedulerHandler(callable2scheduler);
    } finally {
        RxJavaPlugins.reset();
    }
}