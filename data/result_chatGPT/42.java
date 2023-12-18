/**
 * This method is used to drain the data.
 */
void drain() {
    if (getAndIncrement() != 0) {
        return;
    }
    int missed = 1;
    Subscriber<? super T> a = downstream;
    List<T>[] lists = this.lists;
    int[] indexes = this.indexes;
    int n = indexes.length;
    for (;;) {
        long r = requested.get();
        long e = 0L;
        while (e != r) {
            if (checkCancelled(lists)) {
                return;
            }
            Throwable ex = error.get();
            if (ex != null) {
                handleException(a, lists, ex);
                return;
            }
            T min = null;
            int minIndex = -1;
            minIndex = findMin(lists, indexes, n, min, minIndex);
            if (min == null) {
                complete(a, lists);
                return;
            }
            a.onNext(min);
            indexes[minIndex]++;
            e++;
        }
        if (checkCancelled(lists)) {
            return;
        }
        Throwable ex = error.get();
        if (ex != null) {
            handleException(a, lists, ex);
            return;
        }
        if (checkEmpty(lists, indexes, n)) {
            complete(a, lists);
            return;
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
 * This method is used to check if the process is cancelled.
 * @param lists The lists to be checked.
 * @return True if cancelled, false otherwise.
 */
private boolean checkCancelled(List<T>[] lists) {
    if (cancelled) {
        Arrays.fill(lists, null);
        return true;
    }
    return false;
}
/**
 * This method is used to handle exceptions.
 * @param a The subscriber.
 * @param lists The lists to be filled.
 * @param ex The exception to be handled.
 */
private void handleException(Subscriber<? super T> a, List<T>[] lists, Throwable ex) {
    cancelAll();
    Arrays.fill(lists, null);
    a.onError(ex);
}
/**
 * This method is used to find the minimum value.
 * @param lists The lists to be checked.
 * @param indexes The indexes to be checked.
 * @param n The length of the indexes.
 * @param min The minimum value.
 * @param minIndex The index of the minimum value.
 * @return The index of the minimum value.
 */
private int findMin(List<T>[] lists, int[] indexes, int n, T min, int minIndex) {
    for (int i = 0; i < n; i++) {
        List<T> list = lists[i];
        int index = indexes[i];
        if (list.size() != index) {
            if (min == null) {
                min = list.get(index);
                minIndex = i;
            } else {
                T b = list.get(index);
                boolean smaller;
                try {
                    smaller = comparator.compare(min, b) > 0;
                } catch (Throwable exc) {
                    Exceptions.throwIfFatal(exc);
                    cancelAll();
                    Arrays.fill(lists, null);
                    if (!error.compareAndSet(null, exc)) {
                        RxJavaPlugins.onError(exc);
                    }
                    a.onError(error.get());
                    return;
                }
                if (smaller) {
                    min = b;
                    minIndex = i;
                }
            }
        }
    }
    return minIndex;
}
/**
 * This method is used to complete the process.
 * @param a The subscriber.
 * @param lists The lists to be filled.
 */
private void complete(Subscriber<? super T> a, List<T>[] lists) {
    Arrays.fill(lists, null);
    a.onComplete();
}
/**
 * This method is used to check if the lists are empty.
 * @param lists The lists to be checked.
 * @param indexes The indexes to be checked.
 * @param n The length of the indexes.
 * @return True if empty, false otherwise.
 */
private boolean checkEmpty(List<T>[] lists, int[] indexes, int n) {
    boolean empty = true;
    for (int i = 0; i < n; i++) {
        if (indexes[i] != lists[i].size()) {
            empty = false;
            break;
        }
    }
    return empty;
}
