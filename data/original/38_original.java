void drain() {
    if (getAndIncrement() != 0) {
        return;
    }

    final SimplePlainQueue<Object> queue = this.queue;
    final Subscriber<? super Flowable<T>> downstream = this.downstream;
    UnicastProcessor<T> window = this.window;

    int missed = 1;
    for (;;) {

        if (upstreamCancelled) {
            queue.clear();
            window = null;
            this.window = null;
        } else {
            boolean isDone = done;
            Object o = queue.poll();
            boolean isEmpty = o == null;

            if (isDone && isEmpty) {
                Throwable ex = error;
                if (ex != null) {
                    if (window != null) {
                        window.onError(ex);
                    }
                    downstream.onError(ex);
                } else {
                    if (window != null) {
                        window.onComplete();
                    }
                    downstream.onComplete();
                }
                cleanupResources();
                upstreamCancelled = true;
                continue;
            }
            else if (!isEmpty) {

                if (o == NEXT_WINDOW) {
                    if (window != null) {
                        window.onComplete();
                        window = null;
                        this.window = null;
                    }
                    if (downstreamCancelled.get()) {
                        timer.dispose();
                    } else {
                        if (requested.get() == emitted) {
                            upstream.cancel();
                            cleanupResources();
                            upstreamCancelled = true;

                            downstream.onError(missingBackpressureMessage(emitted));
                        } else {
                            emitted++;

                            windowCount.getAndIncrement();
                            window = UnicastProcessor.create(bufferSize, windowRunnable);
                            this.window = window;

                            FlowableWindowSubscribeIntercept<T> intercept = new FlowableWindowSubscribeIntercept<>(window);
                            downstream.onNext(intercept);

                            if (intercept.tryAbandon()) {
                                window.onComplete();
                            }
                        }
                    }
                } else if (window != null) {
                    @SuppressWarnings("unchecked")
                    T item = (T)o;
                    window.onNext(item);
                }

                continue;
            }
        }

        missed = addAndGet(-missed);
        if (missed == 0) {
            break;
        }
    }
}