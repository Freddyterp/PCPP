// Various threadsafe histogram implementations and a workload
// sestoft@itu.dk * 2014-09-10, 2026-10-21

package exercises09;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.LongAdder;

class HistogramPerformance {
  public static void main(String[] args) {
    exerciseHistogram(new SynchronizedHistogram(30));
    exerciseHistogram(new CasHistogram(30));
    exerciseHistogram(new AtomicHistogram(30));
    exerciseHistogram(new StripedHistogram(30));
    exerciseHistogram(new LongAdderHistogram(30));
  }

  public static void exerciseHistogram(Histogram histogram) {
    final int range = 50_000_000;
    final int threadCount = 64, perThread = range / threadCount;
    final Thread[] threads = new Thread[threadCount];
    Timer timer = new Timer();
    for (int t=0; t<threadCount; t++) {
      final int from = perThread * t, 
	          to = (t+1 == threadCount) ? range : perThread * (t+1); 
      threads[t] = Thread.ofVirtual().start(() -> {
	  for (int p=from; p<to; p++)
	    histogram.increment(countFactors(p));
	});
    }
    try {
      for (int t=0; t<threadCount; t++) 
	threads[t].join();
    } catch (InterruptedException exn) {
      System.out.println("Some thread was interrupted");
    }
    System.out.printf("%-30s %10.1f ms%n", histogram.getClass(), timer.check() * 1e3);
    // dump(histogram);
  }
  
  public static void dump(Histogram histogram) {
    int totalCount = 0;
    for (int bin=0; bin<histogram.getSpan(); bin++) {
      System.out.printf("%4d: %9d%n", bin, histogram.getCount(bin));
      totalCount += histogram.getCount(bin);
    }
    System.out.printf("      %9d%n", totalCount);
  }

  public static int countFactors(int p) {
    if (p < 2) 
      return 0;
    int factorCount = 1, k = 2;
    while (p >= k * k) {
      if (p % k == 0) {
	factorCount++;
	p /= k;
      } else 
	k++;
    }
    return factorCount;
  }
}

class Histogram1 implements Histogram {
  private int[] counts;
  public Histogram1(int span) {
    this.counts = new int[span];
  }
  public void increment(int bin) {
    counts[bin] = counts[bin] + 1;
  }
  public int getCount(int bin) {
    return counts[bin];
  }
  public int getSpan() {
    return counts.length;
  }
  public int getAndClear(int bin) {
    int result = counts[bin];
    counts[bin] = 0;
    return result;
  }
}

// Classes to implement:

class SynchronizedHistogram implements Histogram {
  private final int[] counts;


  public SynchronizedHistogram(int span) {
    counts = new int[span];
  }

  public synchronized void increment(int bin) {
    counts[bin]++;
  }

  public synchronized int getCount(int bin) {
    return counts[bin];
  }

  public int getSpan() {
    return counts.length;
  }

  public synchronized int getAndClear(int bin) {
    int count = counts[bin];
    counts[bin] = 0;
    return count;
  }
}
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

class AtomicHistogram implements Histogram {
  private final AtomicIntegerArray counts;

  public AtomicHistogram(int span) {
    counts = new AtomicIntegerArray(span);
  }

  public void increment(int bin) {
    counts.getAndIncrement(bin);
  }

  public int getCount(int bin) {
    return counts.get(bin);
  }

  public int getSpan() {
    return counts.length();
  }

  public int getAndClear(int bin) {
    return counts.getAndSet(bin, 0);
  }
}

class StripedHistogram implements Histogram {
  private final int stripeCount = 16;
  private final AtomicIntegerArray[] counts;

  public StripedHistogram(int span) {
    counts = new AtomicIntegerArray[stripeCount];

    for (int i = 0; i < stripeCount; i++) {
      counts[i] = new AtomicIntegerArray(span);
    }
  }

  public void increment(int bin) {
    int strp = (int) (Thread.currentThread().threadId() % stripeCount);
    counts[strp].getAndIncrement(bin);
  }

  public int getCount(int bin) {
    int total = 0;

    for (int i = 0; i < stripeCount; i++) {
      total += counts[i].get(bin);
    }

    return total;
  }

  public int getSpan() {
    return counts[0].length();
  }

  public int getAndClear(int bin) {
    int total = 0;

    for (int i = 0; i < stripeCount; i++) {
      total += counts[i].getAndSet(bin, 0);
    }

    return total;
  }
}

class LongAdderHistogram implements Histogram {
  private final LongAdder[] counts;

  public LongAdderHistogram(int span) {
    counts = new LongAdder[span];

    for (int i = 0; i < span; i++) {
      counts[i] = new LongAdder();
    }
  }

  public void increment(int bin) {
    counts[bin].increment();
  }

  public int getCount(int bin) {
    return (int) counts[bin].sum();
  }

  public int getSpan() {
    return counts.length;
  }

  public int getAndClear(int bin) {
    return (int) counts[bin].sumThenReset();
  }
}
