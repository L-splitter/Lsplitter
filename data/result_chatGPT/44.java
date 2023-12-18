/**
 * This method checks the bad source flowable.
 * @param mapper The function to map the flowable.
 * @param error The error flag.
 * @param goodValue The good value.
 * @param badValue The bad value.
 * @param expected The expected values.
 */
public static <T> void checkBadSourceFlowable(Function<Flowable<T>, Object> mapper,
        final boolean error, final T goodValue, final T badValue, final Object... expected) {
    List<Throwable> errors = trackPluginErrors();
    try {
        Flowable<T> bad = createBadFlowable(goodValue, badValue, error);
        Object o = mapper.apply(bad);
        checkObservableSource(o, error, expected);
        checkPublisher(o, error, expected);
        checkSingleSource(o, error, expected);
        checkMaybeSource(o, error, expected);
        checkCompletableSource(o, error, expected);
        assertUndeliverable(errors, 0, TestException.class, "second");
    } catch (AssertionError ex) {
        throw ex;
    } catch (Throwable ex) {
        throw new RuntimeException(ex);
    } finally {
        RxJavaPlugins.reset();
    }
}
/**
 * This method creates a bad flowable.
 * @param goodValue The good value.
 * @param badValue The bad value.
 * @param error The error flag.
 * @return The bad flowable.
 */
private static <T> Flowable<T> createBadFlowable(final T goodValue, final T badValue, final boolean error) {
    return new Flowable<T>() {
        @Override
        protected void subscribeActual(Subscriber<? super T> subscriber) {
            subscriber.onSubscribe(new BooleanSubscription());
            if (goodValue != null) {
                subscriber.onNext(goodValue);
            }
            if (error) {
                subscriber.onError(new TestException("error"));
            } else {
                subscriber.onComplete();
            }
            if (badValue != null) {
                subscriber.onNext(badValue);
            }
            subscriber.onError(new TestException("second"));
            subscriber.onComplete();
        }
    };
}
/**
 * This method checks the observable source.
 * @param o The object to check.
 * @param error The error flag.
 * @param expected The expected values.
 */
private static void checkObservableSource(Object o, boolean error, Object... expected) {
    if (o instanceof ObservableSource) {
        ObservableSource<?> os = (ObservableSource<?>) o;
        TestObserverEx<Object> to = new TestObserverEx<>();
        os.subscribe(to);
        performAssertions(to, error, expected);
    }
}
/**
 * This method checks the publisher.
 * @param o The object to check.
 * @param error The error flag.
 * @param expected The expected values.
 */
private static void checkPublisher(Object o, boolean error, Object... expected) {
    if (o instanceof Publisher) {
        Publisher<?> os = (Publisher<?>) o;
        TestSubscriberEx<Object> ts = new TestSubscriberEx<>();
        os.subscribe(ts);
        performAssertions(ts, error, expected);
    }
}
/**
 * This method checks the single source.
 * @param o The object to check.
 * @param error The error flag.
 * @param expected The expected values.
 */
private static void checkSingleSource(Object o, boolean error, Object... expected) {
    if (o instanceof SingleSource) {
        SingleSource<?> os = (SingleSource<?>) o;
        TestObserverEx<Object> to = new TestObserverEx<>();
        os.subscribe(to);
        performAssertions(to, error, expected);
    }
}
/**
 * This method checks the maybe source.
 * @param o The object to check.
 * @param error The error flag.
 * @param expected The expected values.
 */
private static void checkMaybeSource(Object o, boolean error, Object... expected) {
    if (o instanceof MaybeSource) {
        MaybeSource<?> os = (MaybeSource<?>) o;
        TestObserverEx<Object> to = new TestObserverEx<>();
        os.subscribe(to);
        performAssertions(to, error, expected);
    }
}
/**
 * This method checks the completable source.
 * @param o The object to check.
 * @param error The error flag.
 * @param expected The expected values.
 */
private static void checkCompletableSource(Object o, boolean error, Object... expected) {
    if (o instanceof CompletableSource) {
        CompletableSource os = (CompletableSource) o;
        TestObserverEx<Object> to = new TestObserverEx<>();
        os.subscribe(to);
        performAssertions(to, error, expected);
    }
}
/**
 * This method performs assertions on the test observer.
 * @param to The test observer.
 * @param error The error flag.
 * @param expected The expected values.
 */
private static void performAssertions(TestObserverEx<Object> to, boolean error, Object... expected) {
    to.awaitDone(5, TimeUnit.SECONDS);
    to.assertSubscribed();
    if (expected != null) {
        to.assertValues(expected);
    }
    if (error) {
        to.assertError(TestException.class)
        .assertErrorMessage("error")
        .assertNotComplete();
    } else {
        to.assertNoErrors().assertComplete();
    }
}
