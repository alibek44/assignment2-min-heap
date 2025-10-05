package algorithms;

import metrics.PerformanceTracker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinHeapTest {

    @Test
    void testHeapifyAndExtract() {
        int[] data = {5, 3, 8, 1, 2, 9};
        MinHeap h = MinHeap.heapify(data, new PerformanceTracker());
        assertEquals(6, h.size());
        assertEquals(1, h.extractMin());
        assertEquals(2, h.extractMin());
        assertEquals(3, h.extractMin());
        assertEquals(3, h.size());
    }

    @Test
    void testInsertAndPeek() {
        MinHeap h = new MinHeap(4, new PerformanceTracker());
        int id1 = h.insert(7);
        int id2 = h.insert(4);
        int id3 = h.insert(10);
        assertEquals(4, h.peekMin());
        assertEquals(3, h.size());
        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
    }

    @Test
    void testDecreaseKey() {
        MinHeap h = new MinHeap(4, new PerformanceTracker());
        int a = h.insert(20);
        int b = h.insert(15);
        int c = h.insert(30);
        assertEquals(15, h.peekMin());
        h.decreaseKey(a, 5);
        assertEquals(5, h.peekMin());
        assertEquals(3, h.size());
    }

    @Test
    void testMerge() {
        MinHeap h1 = new MinHeap(4, new PerformanceTracker());
        h1.insert(10); h1.insert(40); h1.insert(30);

        MinHeap h2 = new MinHeap(4, new PerformanceTracker());
        h2.insert(5); h2.insert(20);

        h1.merge(h2);
        assertEquals(5, h1.extractMin());
        assertEquals(10, h1.extractMin());
        assertEquals(20, h1.extractMin());
        assertEquals(2, h1.size());
    }

    @Test
    void testExtractFromEmptyThrows() {
        MinHeap h = new MinHeap(1, new PerformanceTracker());
        assertThrows(java.util.NoSuchElementException.class, h::extractMin);
    }

    @Test
    void testDecreaseKeyInvalidIdThrows() {
        MinHeap h = new MinHeap(2, new PerformanceTracker());
        h.insert(3);
        assertThrows(IllegalArgumentException.class, () -> h.decreaseKey(999, 1));
    }
}
