/**
 * This method is used to initialize the hash code.
 *
 * @param hc the hash code array
 * @return the hash code
 */
private int initializeHashCode(int[] hc) {
    int h;
    if (hc == null) {
        threadHashCode.set(hc = new int[1]); // Initialize randomly
        int r = rng.nextInt(); // Avoid zero to allow xorShift rehash
        h = hc[0] = (r == 0) ? 1 : r;
    } else {
        h = hc[0];
    }
    return h;
}
/**
 * This method is used to create a new cell and attach it to the cells array.
 *
 * @param x the value to be stored in the cell
 * @param h the hash code
 * @return true if a new cell was created, false otherwise
 */
private boolean tryAttachNewCell(long x, int h) {
    if (busy == 0) { // Try to attach new Cell
        Cell r = new Cell(x); // Optimistically create
        if (busy == 0 && casBusy()) {
            boolean created = false;
            try { // Recheck under lock
                Cell[] rs;
                int m, j;
                if ((rs = cells) != null && (m = rs.length) > 0 && rs[j = (m - 1) & h] == null) {
                    rs[j] = r;
                    created = true;
                }
            } finally {
                busy = 0;
            }
            return created;
        }
    }
    return false;
}
/**
 * This method is used to expand the cells array.
 *
 * @param as the current cells array
 * @param n the length of the cells array
 * @return true if the cells array was expanded, false otherwise
 */
private boolean tryExpandCells(Cell[] as, int n) {
    if (busy == 0 && casBusy()) {
        try {
            if (cells == as) { // Expand table unless stale
                Cell[] rs = new Cell[n << 1];
                for (int i = 0; i < n; ++i) rs[i] = as[i];
                cells = rs;
            }
        } finally {
            busy = 0;
        }
        return true;
    }
    return false;
}
/**
 * This method is used to initialize the cells array.
 *
 * @param x the value to be stored in the cell
 * @param h the hash code
 * @return true if the cells array was initialized, false otherwise
 */
private boolean tryInitializeCells(long x, int h) {
    if (busy == 0 && cells == null && casBusy()) {
        boolean init = false;
        try { // Initialize table
            if (cells == null) {
                Cell[] rs = new Cell[2];
                rs[h & 1] = new Cell(x);
                cells = rs;
                init = true;
            }
        } finally {
            busy = 0;
        }
        return init;
    }
    return false;
}
final void retryUpdate(long x, @CheckForNull int[] hc, boolean wasUncontended) {
    int h = initializeHashCode(hc);
    boolean collide = false; // True if last slot nonempty
    for (; ; ) {
        Cell[] as;
        Cell a;
        int n;
        long v;
        if ((as = cells) != null && (n = as.length) > 0) {
            if ((a = as[(n - 1) & h]) == null) {
                if (tryAttachNewCell(x, h)) break;
                continue; // Slot is now non-empty
            } else if (!wasUncontended) // CAS already known to fail
                wasUncontended = true; // Continue after rehash
            else if (a.cas(v = a.value, fn(v, x))) break;
            else if (n >= NCPU || cells != as) collide = false; // At max size or stale
            else if (!collide) collide = true;
            else if (tryExpandCells(as, n)) {
                collide = false;
                continue; // Retry with expanded table
            }
            h = rehash(h);
            hc[0] = h; // Record index for next time
        } else if (tryInitializeCells(x, h)) break;
        else if (casBase(v = base, fn(v, x))) break; // Fall back on using base
    }
}
/**
 * This method is used to rehash the hash code.
 *
 * @param h the current hash code
 * @return the new hash code
 */
private int rehash(int h) {
    h ^= h << 13; // Rehash
    h ^= h >>> 17;
    h ^= h << 5;
    return h;
}
