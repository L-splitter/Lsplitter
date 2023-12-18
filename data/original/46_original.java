void drain() {
    if (getAndIncrement() != 0) {
        return;
    }

    int missed = 1;
    SpscLinkedArrayQueue<Object> q = queue;
    Subscriber<? super R> a = downstream;

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
                for (UnicastProcessor<?> up : lefts.values()) {
                    up.onComplete();
                }

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

                UnicastProcessor<TRight> up = UnicastProcessor.create();
                int idx = leftIndex++;
                lefts.put(idx, up);

                Publisher<TLeftEnd> p;

                try {
                    p = Objects.requireNonNull(leftEnd.apply(left), "The leftEnd returned a null Publisher");
                } catch (Throwable exc) {
                    fail(exc, a, q);
                    return;
                }

                LeftRightEndSubscriber end = new LeftRightEndSubscriber(this, true, idx);
                disposables.add(end);

                p.subscribe(end);

                ex = error.get();
                if (ex != null) {
                    q.clear();
                    cancelAll();
                    errorAll(a);
                    return;
                }

                R w;

                try {
                    w = Objects.requireNonNull(resultSelector.apply(left, up), "The resultSelector returned a null value");
                } catch (Throwable exc) {
                    fail(exc, a, q);
                    return;
                }

                // TODO since only left emission calls the actual, it is possible to link downstream backpressure with left's source and not error out
                if (requested.get() != 0L) {
                    a.onNext(w);
                    BackpressureHelper.produced(requested, 1);
                } else {
                    fail(MissingBackpressureException.createDefault(), a, q);
                    return;
                }

                for (TRight right : rights.values()) {
                    up.onNext(right);
                }
            }
            else if (mode == RIGHT_VALUE) {
                @SuppressWarnings("unchecked")
                TRight right = (TRight)val;

                int idx = rightIndex++;

                rights.put(idx, right);

                Publisher<TRightEnd> p;

                try {
                    p = Objects.requireNonNull(rightEnd.apply(right), "The rightEnd returned a null Publisher");
                } catch (Throwable exc) {
                    fail(exc, a, q);
                    return;
                }

                LeftRightEndSubscriber end = new LeftRightEndSubscriber(this, false, idx);
                disposables.add(end);

                p.subscribe(end);

                ex = error.get();
                if (ex != null) {
                    q.clear();
                    cancelAll();
                    errorAll(a);
                    return;
                }

                for (UnicastProcessor<TRight> up : lefts.values()) {
                    up.onNext(right);
                }
            }
            else if (mode == LEFT_CLOSE) {
                LeftRightEndSubscriber end = (LeftRightEndSubscriber)val;

                UnicastProcessor<TRight> up = lefts.remove(end.index);
                disposables.remove(end);
                if (up != null) {
                    up.onComplete();
                }
            }
            else {
                LeftRightEndSubscriber end = (LeftRightEndSubscriber)val;

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