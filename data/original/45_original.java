void drain() {
    if (getAndIncrement() != 0) {
        return;
    }

    int missed = 1;
    SpscLinkedArrayQueue<Object> q = queue;
    Observer<? super R> a = downstream;

    for (;;) {
        for (;;) {
            if (cancelled) {
                q.clear();
                return;
            }

            Throwable ex = error.get();
            if (ex != null) {
                q.clear();
                cancelAll();
                errorAll(a);
                return;
            }

            boolean d = active.get() == 0;

            Integer mode = (Integer)q.poll();

            boolean empty = mode == null;

            if (d && empty) {

                lefts.clear();
                rights.clear();
                disposables.dispose();

                a.onComplete();
                return;
            }

            if (empty) {
                break;
            }

            Object val = q.poll();

            if (mode == LEFT_VALUE) {
                @SuppressWarnings("unchecked")
                TLeft left = (TLeft)val;

                int idx = leftIndex++;
                lefts.put(idx, left);

                ObservableSource<TLeftEnd> p;

                try {
                    p = Objects.requireNonNull(leftEnd.apply(left), "The leftEnd returned a null ObservableSource");
                } catch (Throwable exc) {
                    fail(exc, a, q);
                    return;
                }

                LeftRightEndObserver end = new LeftRightEndObserver(this, true, idx);
                disposables.add(end);

                p.subscribe(end);

                ex = error.get();
                if (ex != null) {
                    q.clear();
                    cancelAll();
                    errorAll(a);
                    return;
                }

                for (TRight right : rights.values()) {

                    R w;

                    try {
                        w = Objects.requireNonNull(resultSelector.apply(left, right), "The resultSelector returned a null value");
                    } catch (Throwable exc) {
                        fail(exc, a, q);
                        return;
                    }

                    a.onNext(w);
                }
            }
            else if (mode == RIGHT_VALUE) {
                @SuppressWarnings("unchecked")
                TRight right = (TRight)val;

                int idx = rightIndex++;

                rights.put(idx, right);

                ObservableSource<TRightEnd> p;

                try {
                    p = Objects.requireNonNull(rightEnd.apply(right), "The rightEnd returned a null ObservableSource");
                } catch (Throwable exc) {
                    fail(exc, a, q);
                    return;
                }

                LeftRightEndObserver end = new LeftRightEndObserver(this, false, idx);
                disposables.add(end);

                p.subscribe(end);

                ex = error.get();
                if (ex != null) {
                    q.clear();
                    cancelAll();
                    errorAll(a);
                    return;
                }

                for (TLeft left : lefts.values()) {

                    R w;

                    try {
                        w = Objects.requireNonNull(resultSelector.apply(left, right), "The resultSelector returned a null value");
                    } catch (Throwable exc) {
                        fail(exc, a, q);
                        return;
                    }

                    a.onNext(w);
                }
            }
            else if (mode == LEFT_CLOSE) {
                LeftRightEndObserver end = (LeftRightEndObserver)val;

                lefts.remove(end.index);
                disposables.remove(end);
            } else {
                LeftRightEndObserver end = (LeftRightEndObserver)val;

                rights.remove(end.index);
                disposables.remove(end);
            }
        }

        missed = addAndGet(-missed);
        if (missed == 0) {
            break;
        }
    }
}