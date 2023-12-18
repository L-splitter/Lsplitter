void drainLoop() {
    int missed = 1;

    JoinInnerSubscriber<T>[] s = this.subscribers;
    int n = s.length;
    Subscriber<? super T> a = this.downstream;

    for (;;) {

        long r = requested.get();
        long e = 0;

        middle:
        while (e != r) {
            if (cancelled) {
                cleanup();
                return;
            }

            Throwable ex = errors.get();
            if (ex != null) {
                cleanup();
                a.onError(ex);
                return;
            }

            boolean d = done.get() == 0;

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
                            break middle;
                        }
                    }
                }
            }

            if (d && empty) {
                a.onComplete();
                return;
            }

            if (empty) {
                break;
            }
        }

        if (e == r) {
            if (cancelled) {
                cleanup();
                return;
            }

            Throwable ex = errors.get();
            if (ex != null) {
                cleanup();
                a.onError(ex);
                return;
            }

            boolean d = done.get() == 0;

            boolean empty = true;

            for (int i = 0; i < n; i++) {
                JoinInnerSubscriber<T> inner = s[i];

                SimpleQueue<T> q = inner.queue;
                if (q != null && !q.isEmpty()) {
                    empty = false;
                    break;
                }
            }

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