public static <T> void checkBadSourceFlowable(Function<Flowable<T>, Object> mapper,
        final boolean error, final T goodValue, final T badValue, final Object... expected) {
    List<Throwable> errors = trackPluginErrors();
    try {
        Flowable<T> bad = new Flowable<T>() {
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

        Object o = mapper.apply(bad);

        if (o instanceof ObservableSource) {
            ObservableSource<?> os = (ObservableSource<?>) o;
            TestObserverEx<Object> to = new TestObserverEx<>();

            os.subscribe(to);

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

        if (o instanceof Publisher) {
            Publisher<?> os = (Publisher<?>) o;
            TestSubscriberEx<Object> ts = new TestSubscriberEx<>();

            os.subscribe(ts);

            ts.awaitDone(5, TimeUnit.SECONDS);

            ts.assertSubscribed();

            if (expected != null) {
                ts.assertValues(expected);
            }
            if (error) {
                ts.assertError(TestException.class)
                .assertErrorMessage("error")
                .assertNotComplete();
            } else {
                ts.assertNoErrors().assertComplete();
            }
        }

        if (o instanceof SingleSource) {
            SingleSource<?> os = (SingleSource<?>) o;
            TestObserverEx<Object> to = new TestObserverEx<>();

            os.subscribe(to);

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

        if (o instanceof MaybeSource) {
            MaybeSource<?> os = (MaybeSource<?>) o;
            TestObserverEx<Object> to = new TestObserverEx<>();

            os.subscribe(to);

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

        if (o instanceof CompletableSource) {
            CompletableSource os = (CompletableSource) o;
            TestObserverEx<Object> to = new TestObserverEx<>();

            os.subscribe(to);

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

        assertUndeliverable(errors, 0, TestException.class, "second");
    } catch (AssertionError ex) {
        throw ex;
    } catch (Throwable ex) {
        throw new RuntimeException(ex);
    } finally {
        RxJavaPlugins.reset();
    }
}