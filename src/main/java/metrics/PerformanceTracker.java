package metrics;

public class PerformanceTracker {
    private long comparisons;
    private long arrayAccesses;
    private long swaps;
    private long memBytes;
    private long startNs;
    private long elapsedNs;

    public void reset() {
        comparisons = 0; arrayAccesses = 0; swaps = 0; memBytes = 0; startNs = 0; elapsedNs = 0;
    }
    public void start() { startNs = System.nanoTime(); }
    public void stop() { elapsedNs = System.nanoTime() - startNs; }

    public void incComparisons(long d) { comparisons += d; }
    public void incAccesses(long d) { arrayAccesses += d; }
    public void incSwaps(long d) { swaps += d; }
    public void incMem(long d) { memBytes += d; }

    public long getComparisons() { return comparisons; }
    public long getArrayAccesses() { return arrayAccesses; }
    public long getSwaps() { return swaps; }
    public long getMemBytes() { return memBytes; }
    public long getElapsedNs() { return elapsedNs; }
    public void add(PerformanceTracker o) {
        this.comparisons += o.comparisons;
        this.arrayAccesses += o.arrayAccesses;
        this.swaps += o.swaps;
        this.memBytes = Math.max(this.memBytes, o.memBytes);
        this.elapsedNs += o.elapsedNs;
    }
}
