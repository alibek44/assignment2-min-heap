package algorithms;

import metrics.PerformanceTracker;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Binary Min-Heap of integers with stable ids (handles) for decreaseKey.
 * Internals:
 *   - keys[]: heap keys
 *   - ids[]:  per-node stable id
 *   - posOfId[]: id -> current position in heap (or -1 if not present)
 *
 * API:
 *   - int insert(int key): returns id
 *   - int peekMin()
 *   - int extractMin()
 *   - void decreaseKey(int id, int newKey)
 *   - void merge(MinHeap other)   (meld via reinsertion)
 *   - int size()
 */
public class MinHeap {
    private int[] keys;
    private int[] ids;
    private int size;
    private int nextId;
    private int[] posOfId;
    private final PerformanceTracker t;

    public MinHeap(int capacity, PerformanceTracker tracker) {
        if (capacity < 1) capacity = 1;
        this.keys = new int[capacity];
        this.ids = new int[capacity];
        this.size = 0;
        this.nextId = 0;
        this.posOfId = new int[capacity * 2];
        Arrays.fill(this.posOfId, -1);
        this.t = tracker != null ? tracker : new PerformanceTracker();
        this.t.reset();
        t.incMem((long)(keys.length + ids.length + posOfId.length) * Integer.BYTES);
    }

    public int size() { return size; }

    private void ensureCapacity(int min) {
        if (min <= keys.length) return;
        int ncap = Math.max(min, keys.length * 2);
        keys = Arrays.copyOf(keys, ncap);
        ids  = Arrays.copyOf(ids,  ncap);
        t.incAccesses(ncap);
        t.incMem((long)(keys.length + ids.length + posOfId.length) * Integer.BYTES);
    }

    private void ensureIdCapacity(int id) {
        if (id < posOfId.length) return;
        int ncap = Math.max(id + 1, posOfId.length * 2);
        posOfId = Arrays.copyOf(posOfId, ncap);
        Arrays.fill(posOfId, -1); // safe default; callers will set needed entries
        t.incAccesses(ncap);
        t.incMem((long)(keys.length + ids.length + posOfId.length) * Integer.BYTES);
    }

    private void swap(int i, int j) {
        if (i == j) return;
        int tk = keys[i]; keys[i] = keys[j]; keys[j] = tk;
        int tid = ids[i]; ids[i] = ids[j]; ids[j] = tid;
        t.incAccesses(4);
        t.incSwaps(1);
        posOfId[ids[i]] = i;
        posOfId[ids[j]] = j;
    }

    private int parent(int i) { return (i - 1) >>> 1; }
    private int left(int i) { return (i << 1) + 1; }
    private int right(int i) { return (i << 1) + 2; }

    private void siftUp(int i) {
        while (i > 0) {
            int p = parent(i);
            t.incComparisons(1); t.incAccesses(2);
            if (keys[i] < keys[p]) {
                swap(i, p);
                i = p;
            } else break;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int l = left(i), r = right(i), smallest = i;
            if (l < size) {
                t.incComparisons(1); t.incAccesses(2);
                if (keys[l] < keys[smallest]) smallest = l;
            }
            if (r < size) {
                t.incComparisons(1); t.incAccesses(2);
                if (keys[r] < keys[smallest]) smallest = r;
            }
            if (smallest != i) {
                swap(i, smallest);
                i = smallest;
            } else break;
        }
    }

    /** Build-heap from given array; assigns ids 0..n-1 and sets nextId=n. */
    public static MinHeap heapify(int[] arr, PerformanceTracker tracker) {
        MinHeap h = new MinHeap(arr.length, tracker);
        h.size = arr.length;
        h.keys = Arrays.copyOf(arr, arr.length);
        h.ids = new int[arr.length];
        h.posOfId = new int[Math.max(arr.length * 2, 4)];
        Arrays.fill(h.posOfId, -1);
        for (int i = 0; i < arr.length; i++) {
            h.ids[i] = i;
            h.ensureIdCapacity(i);
            h.posOfId[i] = i;
        }
        h.nextId = arr.length;
        h.t.incAccesses(arr.length);
        h.t.start();
        for (int i = (h.size >>> 1) - 1; i >= 0; i--) h.siftDown(i);
        h.t.stop();
        return h;
    }

    /** Insert and return a stable id (handle). */
    public int insert(int key) {
        ensureCapacity(size + 1);
        int id = nextId++;
        ensureIdCapacity(id);
        keys[size] = key;
        ids[size] = id;
        posOfId[id] = size;
        t.incAccesses(2);
        size++;
        siftUp(size - 1);
        return id;
    }

    public int peekMin() {
        if (size == 0) throw new NoSuchElementException("empty heap");
        t.incAccesses(1);
        return keys[0];
    }

    public int extractMin() {
        if (size == 0) throw new NoSuchElementException("empty heap");
        int min = keys[0];
        t.incAccesses(1);
        swap(0, size - 1);
        posOfId[ids[size - 1]] = -1;
        size--;
        if (size > 0) siftDown(0);
        return min;
    }

    public void decreaseKey(int id, int newKey) {
        ensureIdCapacity(id);
        int pos = posOfId[id];
        if (pos < 0 || pos >= size) throw new IllegalArgumentException("id is not in heap: " + id);
        t.incAccesses(1);
        if (newKey > keys[pos]) throw new IllegalArgumentException("newKey must be <= current key");
        keys[pos] = newKey;
        siftUp(pos);
    }

    /** Merge other heap into this heap by reinserting keys from other. */
    public void merge(MinHeap other) {
        for (int i = 0; i < other.size; i++) {
            insert(other.keys[i]);
        }
    }
}
