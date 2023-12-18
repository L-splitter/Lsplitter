/**
 * This method checks if the process is cancelled or has an error.
 * If so, it clears the queue, cancels all processes and returns an error.
 * @param q the queue to be cleared
 * @param a the subscriber to be notified of the error
 * @return true if the process is cancelled or has an error, false otherwise
 */
private boolean checkCancelledOrError(SpscLinkedArrayQueue<Object> q, Subscriber<? super R> a) {
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
 * This method completes all UnicastProcessors in the lefts map,
 * clears the lefts and rights maps, disposes all disposables and completes the subscriber.
 * @param a the subscriber to be completed
 */
private void completeAll(Subscriber<? super R> a) {
    for (UnicastProcessor<?> up : lefts.values()) {
        up.onComplete();
    }
    lefts.clear();
    rights.clear();
    disposables.dispose();
    a.onComplete();
}
/**
 * This method handles the LEFT_VALUE mode.
 * It creates a new UnicastProcessor, adds it to the lefts map, subscribes a new LeftRightEndSubscriber to a new Publisher,
 * applies the resultSelector to the left value and the UnicastProcessor, and sends the result to the subscriber.
 * It also sends all right values to the UnicastProcessor.
 * @param q the queue to be cleared in case of an error
 * @param a the subscriber to be notified of the error or the result
 * @param val the left value
 * @return true if the process is successful, false otherwise
 */
private boolean handleLeftValue(SpscLinkedArrayQueue<Object> q, Subscriber<? super R> a, Object val) {
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
        return false;
    }
    LeftRightEndSubscriber end = new LeftRightEndSubscriber(this, true, idx);
    disposables.add(end);
    p.subscribe(end);
    if (checkCancelledOrError(q, a)) {
        return false;
    }
    R w;
    try {
        w = Objects.requireNonNull(resultSelector.apply(left, up), "The resultSelector returned a null value");
    } catch (Throwable exc) {
        fail(exc, a, q);
        return false;
    }
    if (requested.get() != 0L) {
        a.onNext(w);
        BackpressureHelper.produced(requested, 1);
    } else {
        fail(MissingBackpressureException.createDefault(), a, q);
        return false;
    }
    for (TRight right : rights.values()) {
        up.onNext(right);
    }
    return true;
}
// Similar methods for handleRightValue, handleLeftClose and handleRightClose
void drain() {
    if (getAndIncrement() != 0) {
        return;
    }
    int missed = 1;
    SpscLinkedArrayQueue<Object> q = queue;
    Subscriber<? super R> a = downstream;
    for (;;) {
        for (;;) {
            if (checkCancelledOrError(q, a)) {
                return;
            }
            boolean d = active.get() == 0;
            Integer mode = (Integer)q.poll();
            boolean empty = mode == null;
            if (d && empty) {
                completeAll(a);
                return;
            }
            if (empty) {
                break;
            }
            Object val = q.poll();
            if (mode == LEFT_VALUE) {
                if (!handleLeftValue(q, a, val)) {
                    return;
                }
            }
            // Similar calls for RIGHT_VALUE, LEFT_CLOSE and RIGHT_CLOSE
        }
        missed = addAndGet(-missed);
        if (missed == 0) {
            break;
        }
    }
}
