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
            if (cancelled) {
                Arrays.fill(lists, null);
                return;
            }

            Throwable ex = error.get();
            if (ex != null) {
                cancelAll();
                Arrays.fill(lists, null);
                a.onError(ex);
                return;
            }

            T min = null;
            int minIndex = -1;

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

            if (min == null) {
                Arrays.fill(lists, null);
                a.onComplete();
                return;
            }

            a.onNext(min);

            indexes[minIndex]++;

            e++;
        }

        if (cancelled) {
            Arrays.fill(lists, null);
            return;
        }

        Throwable ex = error.get();
        if (ex != null) {
            cancelAll();
            Arrays.fill(lists, null);
            a.onError(ex);
            return;
        }

        boolean empty = true;

        for (int i = 0; i < n; i++) {
            if (indexes[i] != lists[i].size()) {
                empty = false;
                break;
            }
        }

        if (empty) {
            Arrays.fill(lists, null);
            a.onComplete();
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