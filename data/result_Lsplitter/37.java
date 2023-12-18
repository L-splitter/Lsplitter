/**
 * This method checks if the process is cancelled and clears if true.
 * @return true if the process is cancelled, false otherwise.
 */
private boolean checkAndClearIfCancelled() {
    if (cancelled) {
        clear();
        return true;
    }
    return false;
}
/**
 * This method handles the error if the process is done and there is an error.
 * @param latest The latest atomic reference.
 * @param downstream The downstream observer.
 * @return true if the process is done and there is an error, false otherwise.
 */
private boolean handleErrorIfDone(AtomicReference<T> latest, Observer<? super T> downstream) {
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
        return true;
    }
    return false;
}
/**
 * This method handles the completion of the process.
 * @param latest The latest atomic reference.
 * @param downstream The downstream observer.
 * @return true if the process is done, false otherwise.
 */
private boolean handleCompletion(AtomicReference<T> latest, Observer<? super T> downstream) {
    T v = latest.get();
    boolean empty = v == null;
    if (done) {
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
                        return true;
                    }
                }
            }
        }
        downstream.onComplete();
        worker.dispose();
        return true;
    }
    return false;
}
/**
 * This method handles the timer.
 * @param latest The latest atomic reference.
 * @param downstream The downstream observer.
 * @return true if the timer is not running or has fired, false otherwise.
 */
private boolean handleTimer(AtomicReference<T> latest, Observer<? super T> downstream) {
    T v = latest.get();
    boolean empty = v == null;
    if (empty) {
        if (timerFired) {
            timerRunning = false;
            timerFired = false;
        }
        return false;
    }
    if (!timerRunning || timerFired) {
        v = latest.getAndSet(null);
        downstream.onNext(v);
        timerFired = false;
        timerRunning = true;
        worker.schedule(this, timeout, unit);
        return true;
    }
    return false;
}
void drain() {
    if (getAndIncrement() != 0) {
        return;
    }
    int missed = 1;
    AtomicReference<T> latest = this.latest;
    Observer<? super T> downstream = this.downstream;
    for (;;) {
        for (;;) {
            if (checkAndClearIfCancelled()) {
                return;
            }
            if (handleErrorIfDone(latest, downstream)) {
                return;
            }
            if (handleCompletion(latest, downstream)) {
                return;
            }
            if (handleTimer(latest, downstream)) {
                break;
            }
        }
        missed = addAndGet(-missed);
        if (missed == 0) {
            break;
        }
    }
}
