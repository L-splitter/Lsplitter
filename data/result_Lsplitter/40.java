/**
 * This method checks if the process is cancelled or not active.
 * If it is not active, it will handle the process.
 * If it is cancelled, it will terminate the process.
 */
void drain() {
    if (getAndIncrement() == 0) {
        for (;;) {
            if (cancelled) {
                return;
            }
            if (!active) {
                handleProcess();
            }
            if (decrementAndGet() == 0) {
                break;
            }
        }
    }
}
/**
 * This method handles the process.
 * It checks if the process is done and if there are any errors.
 * If there are no errors, it will poll the queue and handle the result.
 */
void handleProcess() {
    boolean d = done;
    if (d && !veryEnd) {
        Throwable ex = errors.get();
        if (ex != null) {
            errors.tryTerminateConsumer(downstream);
            return;
        }
    }
    T v;
    try {
        v = queue.poll();
    } catch (Throwable e) {
        handleException(e);
        return;
    }
    boolean empty = v == null;
    if (d && empty) {
        errors.tryTerminateConsumer(downstream);
        return;
    }
    if (!empty) {
        handleNonEmptyValue(v);
    }
}
/**
 * This method handles exceptions.
 * It cancels the upstream, adds the exception to the errors and terminates the consumer.
 *
 * @param e the exception to handle
 */
void handleException(Throwable e) {
    Exceptions.throwIfFatal(e);
    upstream.cancel();
    errors.tryAddThrowableOrReport(e);
    errors.tryTerminateConsumer(downstream);
}
/**
 * This method handles non-empty values.
 * It applies the mapper to the value and handles the result.
 *
 * @param v the non-empty value to handle
 */
void handleNonEmptyValue(T v) {
    Publisher<? extends R> p;
    try {
        p = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null Publisher");
    } catch (Throwable e) {
        handleException(e);
        return;
    }
    if (sourceMode != QueueSubscription.SYNC) {
        handleSourceMode();
    }
    if (p instanceof Supplier) {
        handleSupplier((Supplier<R>) p);
    } else {
        active = true;
        p.subscribe(inner);
    }
}
/**
 * This method handles the source mode.
 * It increments the consumed count and requests more from the upstream if necessary.
 */
void handleSourceMode() {
    int c = consumed + 1;
    if (c == limit) {
        consumed = 0;
        upstream.request(c);
    } else {
        consumed = c;
    }
}
/**
 * This method handles suppliers.
 * It gets the value from the supplier and handles the result.
 *
 * @param supplier the supplier to handle
 */
void handleSupplier(Supplier<R> supplier) {
    R vr;
    try {
        vr = supplier.get();
    } catch (Throwable e) {
        Exceptions.throwIfFatal(e);
        errors.tryAddThrowableOrReport(e);
        if (!veryEnd) {
            upstream.cancel();
            errors.tryTerminateConsumer(downstream);
            return;
        }
        vr = null;
    }
    if (vr == null) {
        return;
    }
    if (inner.isUnbounded()) {
        downstream.onNext(vr);
    } else {
        active = true;
        inner.setSubscription(new SimpleScalarSubscription<>(vr, inner));
    }
}
