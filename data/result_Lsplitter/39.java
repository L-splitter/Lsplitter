/**
 * Main loop for draining the queue.
 */
void drainLoop() {
    int missed = 1;
    JoinInnerSubscriber<T>[] s = this.subscribers;
    int n = s.length;
    Subscriber<? super T> a = this.downstream;
    for (;;) {
        long r = requested.get();
        long e = processQueue(s, a, r);
        if (e == r) {
            if (checkCancelledOrError(a)) {
                return;
            }
            boolean d = done.get() == 0;
            boolean empty = checkEmpty(s, n);
            if (d && empty) {
                a.onComplete();
                return;
            }
        }
        if (e != 0) {
            BackpressureHelper.produced(requested, e);
        }
        missed = addAndGet(-missed);
        if (missed == 0) {
            break;
        }
    }
}
/**
 * Process the queue of subscribers.
 *
 * @param s the array of subscribers
 * @param a the downstream subscriber
 * @param r the requested number of items
 * @return the number of items processed
 */
long processQueue(JoinInnerSubscriber<T>[] s, Subscriber<? super T> a, long r) {
    long e = 0;
    while (e != r) {
        if (checkCancelledOrError(a)) {
            return e;
        }
        boolean d = done.get() == 0;
        boolean empty = processSubscribers(s, a, r, e);
        if (d && empty) {
            a.onComplete();
            return e;
        }
        if (empty) {
            break;
        }
    }
    return e;
}
/**
 * Process each subscriber.
 *
 * @param s the array of subscribers
 * @param a the downstream subscriber
 * @param r the requested number of items
 * @param e the number of items processed so far
 * @return true if all queues are empty, false otherwise
 */
boolean processSubscribers(JoinInnerSubscriber<T>[] s, Subscriber<? super T> a, long r, long e) {
    boolean empty = true;
    for (int i = 0; i < s.length; i++) {
        JoinInnerSubscriber<T> inner = s[i];
        SimplePlainQueue<T> q = inner.queue;
        if (q != null) {
            T v = q.poll();
            if (v != null) {
                empty = false;
                a.onNext(v);
                inner.requestOne();
                if (++e == r) {
                    break;
                }
            }
        }
    }
    return empty;
}
/**
 * Check if the operation is cancelled or there is an error.
 *
 * @param a the downstream subscriber
 * @return true if cancelled or there is an error, false otherwise
 */
boolean checkCancelledOrError(Subscriber<? super T> a) {
    if (cancelled) {
        cleanup();
        return true;
    }
    Throwable ex = errors.get();
    if (ex != null) {
        cleanup();
        a.onError(ex);
        return true;
    }
    return false;
}
/**
 * Check if all queues are empty.
 *
 * @param s the array of subscribers
 * @param n the number of subscribers
 * @return true if all queues are empty, false otherwise
 */
boolean checkEmpty(JoinInnerSubscriber<T>[] s, int n) {
    boolean empty = true;
    for (int i = 0; i < n; i++) {
        JoinInnerSubscriber<T> inner = s[i];
        SimpleQueue<T> q = inner.queue;
        if (q != null && !q.isEmpty()) {
            empty = false;
            break;
        }
    }
    return empty;
}
