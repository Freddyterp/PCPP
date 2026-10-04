package exercises05;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SemaphoreTest {
    @Test
    public void capacityMustNotBeExceeded() throws Exception {
        int capacity = 1;
        SemaphoreImp semaphore = new SemaphoreImp(capacity);

        AtomicInteger inside = new AtomicInteger(0);
        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch allowFirstToExit = new CountDownLatch(1);
        ExecutorService exec = Executors.newFixedThreadPool(2);

        // Trigger counterexample before starting workers
        semaphore.release();

        try {
            Future<Void> first = exec.submit(() -> {
                semaphore.acquire();
                inside.incrementAndGet();

                try {
                    firstEntered.countDown();
                    allowFirstToExit.await();
                } finally {
                    inside.decrementAndGet();
                    semaphore.release();
                }
                return null;
            });
            assertTrue(firstEntered.await(10, TimeUnit.SECONDS), "First worker did not enter in time");

            Future<Integer> second = exec.submit(() -> {
                semaphore.acquire();

                try {
                    return inside.incrementAndGet();
                } finally {
                    inside.decrementAndGet();
                    semaphore.release();
                }
            });

            int observedInside = second.get(10, TimeUnit.SECONDS);

            // First worker is still inside at this point
            assertTrue(observedInside <= capacity, "Capacity exceeded: " + observedInside + " threads entered with capacity " + capacity);
            
            allowFirstToExit.countDown();
            first.get(10, TimeUnit.SECONDS);
        } finally {
            allowFirstToExit.countDown();
            exec.shutdownNow();
            exec.awaitTermination(10, TimeUnit.SECONDS);
        }
    }
}