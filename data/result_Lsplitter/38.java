/**
 * This method is used to drain the queue.
 */
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
  this.window=null;

            window = null;
        } else {
            boolean isDone = done;
            Object o = queue.poll();
            boolean isEmpty = o == null;
            if (isDone && isEmpty) {
                handleDoneState(window, downstream);
                upstreamCancelled = true;
                continue;
            }
            else if (!isEmpty) {
                window = handleNotEmptyState(o, window, downstream);
                continue;
            }
        }
        missed = addAndGet(-missed);
        if (missed == 0) {
            break;
        }
    }
}

/**
 * This method is used to handle the done state.
 * @param window The window to be handled.
 * @param downstream The downstream to be handled.
 */
void handleDoneState(UnicastProcessor<T> window, Subscriber<? super Flowable<T>> downstream) {
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
}
/**
 * This method is used to handle the not empty state.
 * @param o The object to be handled.
 * @param window The window to be handled.
 * @param downstream The downstream to be handled.
 * @return The updated window.
 */
UnicastProcessor<T> handleNotEmptyState(Object o, UnicastProcessor<T> window, Subscriber<? super Flowable<T>> downstream) {
    if (o == NEXT_WINDOW) {
        window = handleNextWindow(window, downstream);
    } else if (window != null) {
        @SuppressWarnings("unchecked")
        T item = (T)o;
        window.onNext(item);
    }
    return window;
}
/**
 * This method is used to handle the next window.
 * @param window The window to be handled.
 * @param downstream The downstream to be handled.
 * @return The updated window.
 */
UnicastProcessor<T> handleNextWindow(UnicastProcessor<T> window, Subscriber<? super Flowable<T>> downstream) {
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
    return window;
}
