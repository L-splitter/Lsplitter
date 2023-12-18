/**
 * Checks if the process is cancelled or there is an error.
 * If so, it clears the queue, cancels all and propagates the error.
 *
 * @param q the queue
 * @param a the observer
 * @return true if the process is cancelled or there is an error, false otherwise
 */
private boolean checkCancelledOrError(SpscLinkedArrayQueue<Object> q, Observer<? super R> a) {
    if (cancelled) {
        q.clear();
        return true;
    }
    Throwable ex = error.get();
    if (ex != null) {
        q.clear();
        cancelAll();
        errorAll(a);
        return true;
    }
    return false;
}
/**
 * Handles the case when the mode is LEFT_VALUE.
 *
 * @param q the queue
 * @param a the observer
 * @param val the value
 */
private void handleLeftValue(SpscLinkedArrayQueue<Object> q, Observer<? super R> a, Object val) {
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
    if (checkCancelledOrError(q, a)) {
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
/**
 * Handles the case when the mode is RIGHT_VALUE.
 *
 * @param q the queue
 * @param a the observer
 * @param val the value
 */
private void handleRightValue(SpscLinkedArrayQueue<Object> q, Observer<? super R> a, Object val) {
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
    if (checkCancelledOrError(q, a)) {
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
/**
 * Handles the case when the mode is LEFT_CLOSE or RIGHT_CLOSE.
 *
 * @param val the value
 */
private void handleClose(Object val) {
    LeftRightEndObserver end = (LeftRightEndObserver)val;
    if (end.isLeft) {
        lefts.remove(end.index);
    } else {
        rights.remove(end.index);
    }
    disposables.remove(end);
}
void drain() {
    if (getAndIncrement() != 0) {
        return;
    }
    int missed = 1;
    SpscLinkedArrayQueue<Object> q = queue;
    Observer<? super R> a = downstream;
    for (;;) {
        for (;;) {
            if (checkCancelledOrError(q, a)) {
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
                handleLeftValue(q, a, val);
            }
            else if (mode == RIGHT_VALUE) {
                handleRightValue(q, a, val);
            }
            else {
                handleClose(val);
            }
        }
        missed = addAndGet(-missed);
        if (missed == 0) {
            break;
        }
    }
}
