package exercises06;

import java.util.concurrent.atomic.AtomicInteger;

class CasHistogram implements Histogram {
    private final AtomicInteger[] counts;

    public CasHistogram(int span) {
        counts = new AtomicInteger[span];

        for (int i = 0; i < span; i++) {
            counts[i] = new AtomicInteger(0);
        }
    }

    public void increment(int bin) {
        AtomicInteger count = counts[bin];
        int oldVal;

        do { 
            oldVal = count.get();
        } while (!count.compareAndSet(oldVal, oldVal + 1));
    }

    public int getCount(int bin) {
        return counts[bin].get();
    }

    public int getSpan() {
        return counts.length;
    }

    public int getAndClear(int bin) {
        AtomicInteger count = counts[bin];
        int oldVal;

        do {
            oldVal = count.get();
        } while (!count.compareAndSet(oldVal, 0));

        return oldVal;
    }
}