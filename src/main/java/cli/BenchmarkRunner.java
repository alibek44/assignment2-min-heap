package cli;

import algorithms.MinHeap;
import metrics.PerformanceTracker;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BenchmarkRunner {

    record Config(int[] sizes, long seed, int ops, double decRatio) {}

    public static void main(String[] args) {
        Config cfg = parseArgs(args);
        SecureRandom rnd = new SecureRandom();
        rnd.setSeed(cfg.seed);

        System.out.println("seed,n,build_ns,ops,op_ns,comparisons,array_accesses,swaps,mem_bytes,extracts,decreases,inserts");

        for (int n : cfg.sizes) {
            int[] initial = new int[n];
            for (int i = 0; i < n; i++) initial[i] = rnd.nextInt();

            PerformanceTracker buildTracker = new PerformanceTracker();
            MinHeap heap = MinHeap.heapify(initial, buildTracker);
            long buildNs = buildTracker.getElapsedNs();

            // Track ids that have been issued (may be inactive after extract)
            List<Integer> ids = new ArrayList<>(n);
            for (int i = 0; i < n; i++) ids.add(i);

            PerformanceTracker opsTracker = new PerformanceTracker();
            opsTracker.start();

            int performedDec = 0, performedExt = 0, performedIns = 0;

            for (int i = 0; i < cfg.ops; i++) {
                double u = rnd.nextDouble();

                // Prefer decreaseKey with probability decRatio, but only if heap not empty
                if (u < cfg.decRatio && heap.size() > 0) {
                    // Try a few random ids; skip if invalid
                    int tries = 3;
                    while (tries-- > 0 && heap.size() > 0) {
                        int pick = Math.abs(rnd.nextInt()) % Math.max(ids.size(), 1);
                        int id = ids.get(pick);
                        int newKey = Math.abs(rnd.nextInt()) % 100;
                        try {
                            heap.decreaseKey(id, newKey);
                            performedDec++;
                            break;
                        } catch (IllegalArgumentException ignored) {
                            // id not active; try another
                        }
                    }
                    continue;
                }

                // Otherwise half extract, half insert — but never extract if empty
                boolean doExtract = rnd.nextBoolean();
                if (doExtract && heap.size() > 0) {
                    heap.extractMin();
                    performedExt++;
                } else {
                    int newId = heap.insert(rnd.nextInt());
                    ids.add(newId);
                    performedIns++;
                }
            }

            opsTracker.stop();
            opsTracker.add(buildTracker);

            System.out.printf(Locale.US,
                    "%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",
                    cfg.seed, n, buildNs, cfg.ops, opsTracker.getElapsedNs(),
                    opsTracker.getComparisons(), opsTracker.getArrayAccesses(), opsTracker.getSwaps(),
                    Math.max(0, opsTracker.getMemBytes()),
                    performedExt, performedDec, performedIns
            );
        }
    }

    private static Config parseArgs(String[] args) {
        String sizes = "100,1000,10000";
        long seed = 42L;
        int ops = 10000;
        double dec = 0.5;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--sizes" -> sizes = args[++i];
                case "--seed" -> seed = Long.parseLong(args[++i]);
                case "--ops" -> ops = Integer.parseInt(args[++i]);
                case "--dec-ratio" -> dec = Double.parseDouble(args[++i]);
            }
        }
        String[] parts = sizes.split(",");
        int[] sz = new int[parts.length];
        for (int i = 0; i < parts.length; i++) sz[i] = Integer.parseInt(parts[i].trim());
        return new Config(sz, seed, ops, dec);
    }
}
