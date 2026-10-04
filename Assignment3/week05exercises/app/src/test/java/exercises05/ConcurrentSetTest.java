package exercises05;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
// TODO: Very likely you need to expand the list of imports

public class ConcurrentSetTest {

    // Variable with set under test
    private ConcurrentIntegerSet set;

    // TODO: Very likely you should add more variables here
    private static final int t_count = 16;

    // Uncomment the appropriate line below to choose the class to
    // test
    // Remember that @BeforeEach is executed before each test
    @BeforeEach
    public void initialize() {
        // init set
        // set = new ConcurrentIntegerSetBuggy();
        // set = new ConcurrentIntegerSetSync();
        set = new ConcurrentIntegerSetLibrary();
    }

    // TODO: Define your tests below
    // 5.1.1
    @RepeatedTest(value = 1000, failureThreshold = 1)
    public void concurrentAddSameElement() throws Exception {
        CyclicBarrier startBarrier = new CyclicBarrier(t_count);
        ExecutorService exec = Executors.newFixedThreadPool(t_count);
    List<Future<Boolean>> res = new ArrayList<>();

    try {
        for (int i = 0; i < t_count; i++) {
            res.add(exec.submit(() -> {
                startBarrier.await(10, TimeUnit.SECONDS);
                return set.add(42);
            }));
        }
        int successfulAdds = 0;

        // Wait for worker threads before checking res
        for(Future<Boolean> result : res) {
            if (result.get(10, TimeUnit.SECONDS)) {
                successfulAdds++;
            }
        }
        assertEquals(1, successfulAdds, "Exactl 1 add should return true");
        assertEquals(1, set.size(), "The set should contain exactly 1 element");
    } finally {
        exec.shutdownNow();
        exec.awaitTermination(10, TimeUnit.SECONDS);
    }
    }

    //5.1.2
    @RepeatedTest(value = 1000, failureThreshold = 1)
    public void concurrentRemoveDistinctElements() throws Exception {
        int elementsPerThread = 1000;
        int totalElements = t_count * elementsPerThread;
        
        for (int i = 0; i < totalElements; i++){
            set.add(i);
        }

        assertEquals(totalElements, set.size(), "Incorrect initial size");

        CyclicBarrier startBarrier = new CyclicBarrier(t_count);
        ExecutorService exec = Executors.newFixedThreadPool(t_count);
        List<Future<Integer>> res = new ArrayList<>();

    try {
        for (int i = 0; i < t_count; i++) {
            final int firstElement = i * elementsPerThread;

            res.add(exec.submit(() -> {
                startBarrier.await(10, TimeUnit.SECONDS);
                int removed = 0;
                for (int j = 0; j < elementsPerThread; j++) {
                    if (set.remove(firstElement + j)) {
                        removed++;
                    }
                }
                return removed;
            }));
        }
        int successfulRemoves = 0;

        // Wait for worker threads before checking res
        for(Future<Integer> result : res) {
            successfulRemoves += result.get(10, TimeUnit.SECONDS);
        }
        assertEquals(totalElements, successfulRemoves, "every distinct element should be removed succesfully");
        assertEquals(0, set.size(), "The set should be empty after removal");
    } finally {
        exec.shutdownNow();
        exec.awaitTermination(10, TimeUnit.SECONDS);
    }
    }
}

