package cli;

import algorithms.MinHeap;
import metrics.PerformanceTracker;

import java.io.File;
import java.io.PrintWriter;
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

        String fileName = "results.csv";

        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            writer.println("seed,n,build_ns,ops,op_ns,comparisons,array_accesses,swaps,mem_bytes,extracts,decreases,inserts");

            for (int n : cfg.sizes) {
                int[] initial = new int[n];
                for (int i = 0; i < n; i++) initial[i] = rnd.nextInt();

                PerformanceTracker buildTracker = new PerformanceTracker();
                MinHeap heap = MinHeap.heapify(initial, buildTracker);
                long buildNs = buildTracker.getElapsedNs();

                List<Integer> ids = new ArrayList<>(n);
                for (int i = 0; i < n; i++) ids.add(i);

                PerformanceTracker opsTracker = new PerformanceTracker();
                opsTracker.start();

                int performedDec = 0, performedExt = 0, performedIns = 0;

                for (int i = 0; i < cfg.ops; i++) {
                    double u = rnd.nextDouble();

                    // decreaseKey operation
                    if (u < cfg.decRatio && heap.size() > 0) {
                        int id = ids.get(Math.abs(rnd.nextInt()) % Math.max(ids.size(), 1));
                        int newKey = Math.abs(rnd.nextInt()) % 100;
                        try {
                            heap.decreaseKey(id, newKey);
                            performedDec++;
                        } catch (IllegalArgumentException ignored) {}
                    }

                    // extractMin or insert
                    else if (rnd.nextBoolean() && heap.size() > 0) {
                        heap.extractMin();
                        performedExt++;
                    } else {
                        int id = heap.insert(rnd.nextInt());
                        ids.add(id);
                        performedIns++;
                    }
                }

                opsTracker.stop();
                opsTracker.add(buildTracker);

                writer.printf(Locale.US,
                        "%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n",
                        cfg.seed, n, buildNs, cfg.ops, opsTracker.getElapsedNs(),
                        opsTracker.getComparisons(), opsTracker.getArrayAccesses(), opsTracker.getSwaps(),
                        Math.max(0, opsTracker.getMemBytes()), performedExt, performedDec, performedIns
                );

                System.out.printf("✅ Completed n=%d → extracts=%d, decreases=%d, inserts=%d%n",
                        n, performedExt, performedDec, performedIns);
            }

            System.out.println("📁 Results saved automatically to " + fileName);

        } catch (Exception e) {
            System.err.println("❌ Error writing CSV: " + e.getMessage());
            e.printStackTrace();
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