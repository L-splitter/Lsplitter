void drain() {
    if (getAndIncrement() == 0) {

        for (;;) {
            if (cancelled) {
                return;
            }

            if (!active) {

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
                    Exceptions.throwIfFatal(e);
                    upstream.cancel();
                    errors.tryAddThrowableOrReport(e);
                    errors.tryTerminateConsumer(downstream);
                    return;
                }

                boolean empty = v == null;

                if (d && empty) {
                    errors.tryTerminateConsumer(downstream);
                    return;
                }

                if (!empty) {
                    Publisher<? extends R> p;

                    try {
                        p = Objects.requireNonNull(mapper.apply(v), "The mapper returned a null Publisher");
                    } catch (Throwable e) {
                        Exceptions.throwIfFatal(e);

                        upstream.cancel();
                        errors.tryAddThrowableOrReport(e);
                        errors.tryTerminateConsumer(downstream);
                        return;
                    }

                    if (sourceMode != QueueSubscription.SYNC) {
                        int c = consumed + 1;
                        if (c == limit) {
                            consumed = 0;
                            upstream.request(c);
                        } else {
                            consumed = c;
                        }
                    }

                    if (p instanceof Supplier) {
                        @SuppressWarnings("unchecked")
                        Supplier<R> supplier = (Supplier<R>) p;

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
                            continue;
                        }

                        if (inner.isUnbounded()) {
                            downstream.onNext(vr);
                            continue;
                        } else {
                            active = true;
                            inner.setSubscription(new SimpleScalarSubscription<>(vr, inner));
                        }
                    } else {
                        active = true;
                        p.subscribe(inner);
                    }
                }
            }
            if (decrementAndGet() == 0) {
                break;
            }
        }
    }
}