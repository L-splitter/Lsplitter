void drain() {
    if (getAndIncrement() != 0) {
        return;
    }

    int missed = 1;

    AtomicReference<T> latest = this.latest;
    Observer<? super T> downstream = this.downstream;

    for (;;) {

        for (;;) {
            if (cancelled) {
                clear();
                return;
            }

            boolean d = done;
            Throwable error = this.error;

            if (d && error != null) {
                if (onDropped != null) {
                    T v = latest.getAndSet(null);
                    if (v != null) {
                        try {
                            onDropped.accept(v);
                        } catch (Throwable ex) {
                            Exceptions.throwIfFatal(ex);
                            error = new CompositeException(error, ex);
                        }
                    }
                } else {
                    latest.lazySet(null);
                }
                downstream.onError(error);
                worker.dispose();
                return;
            }

            T v = latest.get();
            boolean empty = v == null;

            if (d) {
                if (!empty) {
                    v = latest.getAndSet(null);
                    if (emitLast) {
                        downstream.onNext(v);
                    } else {
                        if (onDropped != null) {
                            try {
                                onDropped.accept(v);
                            } catch (Throwable ex) {
                                Exceptions.throwIfFatal(ex);
                                downstream.onError(ex);
                                worker.dispose();
                                return;
                            }
                        }
                    }
                }
                downstream.onComplete();
                worker.dispose();
                return;
            }

            if (empty) {
                if (timerFired) {
                    timerRunning = false;
                    timerFired = false;
                }
                break;
            }

            if (!timerRunning || timerFired) {
                v = latest.getAndSet(null);
                downstream.onNext(v);

                timerFired = false;
                timerRunning = true;
                worker.schedule(this, timeout, unit);
            } else {
                break;
            }
        }

        missed = addAndGet(-missed);
        if (missed == 0) {
            break;
        }
    }
}