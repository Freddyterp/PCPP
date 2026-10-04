// For week 6
// raup@itu.dk * 2026-09-23

package exercises06;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestHistograms {
    // The imports above are just for convenience, feel free add or remove imports

    @Test
    public void parallelHistogramMatchesSequential() throws Exception {
        final int span = 30;
        final int numberCount = 5000;

        Histogram expected = new Histogram1(span);
        Histogram actual = new CasHistogram(span);

        // Compute reference res sequentially
        for (int i = 0; i < numberCount; i++) {
            expected.increment(countFactors(i));
        }

        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<Void>> res = new ArrayList<>();

        // Create separate virtual thread for every task submitted
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            try {
                for (int j = 0; j < numberCount; j++) {
                    final int num = j;

                    res.add(exec.submit(() -> {
                        startGate.await();
                        actual.increment(countFactors(num));
                        return null;
                    }));
                }
                // Allow workers to behin after tasks have been submitted
                startGate.countDown();

                // Wait for all workers and propagate worker exceptions
                for (Future<Void> result : res) {
                    result.get(30, TimeUnit.SECONDS);
                }

                assertEquals(expected.getSpan(), actual.getSpan());

                for (int bin = 0; bin < span; bin++) {
                    assertEquals(
                        expected.getCount(bin), actual.getCount(bin), "Incorrect count in bin " + bin
                    );
                }
            } finally {
                startGate.countDown();
                exec.shutdownNow();
            }
        }
    }




    // Function to count the number of prime factors of a number `p`
    private static int countFactors(int p) {
        if (p < 2) return 0;
        int factorCount = 1, k = 2;
        while (p >= k * k) {
            if (p % k == 0) {
                factorCount++;
                p= p/k;
            } else
                k= k+1;
        }
        return factorCount;
    }

}
