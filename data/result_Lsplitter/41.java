/**
 * This method checks if the process is done and not at the very end.
 */
private void checkIfDoneAndNotVeryEnd() {
    boolean d = done;
    if (d && !veryEnd) {
        Throwable ex = errors.get();
        if (ex != null) {
            errors.tryTerminateConsumer(downstream);
            worker.dispose();
            return;
        }
    }
}
/**
 * This method polls the queue.
 */
private T pollQueue() {
    T v;
    try {
        v = queue.poll();
    } catch (Throwable e) {
        Exceptions.throwIfFatal(e);
        upstream.cancel();
        errors.tryAddThrowableOrReport(e);
        errors.tryTerminateConsumer(downstream);
        worker.dispose();
        return null;
    }
    return v;
}
/**
 * This method checks if the process is done and the queue is empty.
 */
private void checkIfDoneAndEmpty() {
    boolean d = done;
    boolean empty = v == null;
    if (d && empty) {
        errors.tryTerminateConsumer(downstream);
        worker.dispose();
        return;
    }
}
/**
 * This method applies the mapper to the value.
 */
private Publisher<? extends R> applyMapper(T v) {
    Publisher<? extends R> p;
    try {
        p = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null Publisher");
    } catch (Throwable e) {
        Exceptions.throwIfFatal(e);
        upstream.cancel();
        errors.tryAddThrowableOrReport(e);
        errors.tryTerminateConsumer(downstream);
        worker.dispose();
        return null;
    }
    return p;
}
/**
 * This method checks the source mode and updates the consumed value.
 */
private void checkSourceModeAndUpdateConsumed() {
    if (sourceMode != QueueSubscription.SYNC) {
        int c = consumed + 1;
        if (c == limit) {
            consumed = 0;
            upstream.request(c);
        } else {
            consumed = c;
        }
    }
}
/**
 * This method checks if the publisher is a supplier and gets the value.
 */
private R checkIfSupplierAndGet(Publisher<? extends R> p) {
    R vr;
    if (p instanceof Supplier) {
        @SuppressWarnings("unchecked")
        Supplier<R> supplier = (Supplier<R>) p;
        try {
            vr = supplier.get();
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            errors.tryAddThrowableOrReport(e);
            if (!veryEnd) {
                upstream.cancel();
                errors.tryTerminateConsumer(downstream);
                worker.dispose();
                return null;
            }
            vr = null;
        }
        return vr;
    }
    return null;
}
/**
 * This method checks if the value is null or cancelled and sets the subscription.
 */
private void checkIfNullOrCancelledAndSetSubscription(R vr) {
    if (vr == null || cancelled) {
        return;
    }
    if (inner.isUnbounded()) {
        downstream.onNext(vr);
    } else {
        active = true;
        inner.setSubscription(new SimpleScalarSubscription<>(vr, inner));
    }
}


public void run() {
    
  if (cancelled) {
    return;
  }

    checkIfDoneAndNotVeryEnd();
    T v = pollQueue();
    checkIfDoneAndEmpty();
    Publisher<? extends R> p = applyMapper(v);
    checkSourceModeAndUpdateConsumed();
    R vr = checkIfSupplierAndGet(p);
    checkIfNullOrCancelledAndSetSubscription(vr);
    
  if (active) {
    p.subscribe(inner);
  }

    
  if (decrementAndGet() == 0) {
    return;
  }

}
