# Assignment 2 — Min-Heap (Pair 4, Student A)

Implements a **binary Min-Heap** supporting:
- `insert` (returns a **stable id/handle**)
- `extractMin`
- `peekMin`
- `decreaseKey(id, newKey)`
- `merge(MinHeap other)` (meld by re-inserting)
- Metrics collection (comparisons, array accesses, swaps/moves, elapsed time, mem bytes)
- CLI benchmark producing CSV

## Structure
```
assignment2-min-heap/
├── src/main/java/
│   ├── algorithms/MinHeap.java
│   ├── metrics/PerformanceTracker.java
│   └── cli/BenchmarkRunner.java
├── src/test/java/
│   └── algorithms/MinHeapTest.java
├── docs/
│   └── performance-plots/
├── README.md
└── pom.xml
```

## Build & Run
```bash
mvn test
mvn package
java -jar target/assignment2-min-heap-1.0.0.jar
```
Customize:
```bash
java -jar target/assignment2-min-heap-1.0.0.jar --sizes 100,1000,10000 --seed 42 --ops 100000 --dec-ratio 0.5 > heap_results.csv
```

## CSV Columns
```
seed,n,build_ns,ops,op_ns,comparisons,array_accesses,swaps,mem_bytes,extracts,decreases,inserts
```
