// Performance measurements for long adders
// sestoft@itu.dk * 2026-10-13

// Based on pcpp/TestLongAdders.java * 2014, 2016

package exercises09;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.LongToDoubleFunction;

public class LongAdderPerformance {
  public static void main(String[] args) {
    SystemInfo();
    for (int threadCount=20; threadCount<=20; threadCount++) 
      timeAllLongAdders(threadCount);
  }

  private static void timeAllLongAdders(int threadCount) {
    int iterations = 10_000_000, perThread = iterations / threadCount;
    final String format = "%-25s %3d";
    Function<String, String> info = s -> String.format(Locale.US, format, s, threadCount);
    Mark7('u', info.apply("SyncLongAdder"), 
          i -> exerciseLongAdder(threadCount, perThread, new SyncLongAdder()));
    Mark7('u', info.apply("SyncLongArrayAdder"), 
             i -> exerciseLongAdder(threadCount, perThread, new SyncLongArrayAdder()));
    Mark7('u', info.apply("SyncLongArrayAdderPadded"), 
          i -> exerciseLongAdder(threadCount, perThread, new SyncLongArrayAdderPadded()));
    Mark7('u', info.apply("WrappedAtomicLong"), 
          i -> exerciseLongAdder(threadCount, perThread, new WrappedAtomicLong()));
    Mark7('u', info.apply("MyAtomicLong"), 
          i -> exerciseLongAdder(threadCount, perThread, new MyAtomicLong()));
    Mark7('u', info.apply("ThreadLocalAtomicLong"), 
          i -> exerciseLongAdder(threadCount, perThread, new ThreadLocalAtomicLong()));
    Mark7('u', info.apply("LongAdderArray"),
          i -> exerciseLongAdder(threadCount, perThread, new LongAdderArray()));
    Mark7('u', info.apply("LongAdderArrayPadded"),
          i -> exerciseLongAdder(threadCount, perThread, new LongAdderArrayPadded()));
    Mark7('u', info.apply("WrappedLongAdder"), 
          i -> exerciseLongAdder(threadCount, perThread, new WrappedLongAdder()));
    Mark7('u', info.apply("SyncLongArrayAdderLessPad"),
          i -> exerciseLongAdder(threadCount, perThread, new SyncLongArrayAdderLessPad()));
  }

  // Timing of a long adder
  private static double exerciseLongAdder(int threadCount, int perThread, ILongAdder longAdder) {
    Thread[] threads = new Thread[threadCount];
    for (int t=0; t<threadCount; t++) {
      threads[t] = Thread.ofPlatform().start(() -> {
        for (int i=0; i<perThread; i++) 
          longAdder.add(i);
      });
    }
    try {
      for (int t=0; t<threadCount; t++) 
        threads[t].join();
    } catch (InterruptedException exn) { }
    long expected = ((long)perThread * (perThread - 1) / 2) * threadCount;
    if (longAdder.longValue() != expected)
      System.out.println("Wrong sum " + longAdder.longValue() + ", expected " + expected);
    return longAdder.longValue();
  }

  // --- Benchmarking infrastructure ---

  private static class Timer {
    private long start, spent = 0;
    public Timer() { play(); }
    public double check() { return (System.nanoTime()-start+spent)/1e9; }
    public void pause() { spent += System.nanoTime()-start; }
    public void play() { start = System.nanoTime(); }
  }

  public static double Mark7(char tunit, String msg, LongToDoubleFunction f) {
    long n = 10, count = 1, totalCount = 0;
    double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
    do { 
      count *= 2;
      st = sst = 0.0;
      for (long j=0; j<n; j++) {
        Timer t = new Timer();
        for (long i=0; i<count; i++) 
          dummy += f.applyAsDouble(i);
        runningTime = t.check();
        double time = runningTime * scale(tunit) / count;
        st += time; 
        sst += time * time;
        totalCount += count;
      }
    } while (runningTime < 0.25);
    double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
    System.out.printf("%-25s %15.1f %cs %10.2f %10d%n", msg, mean, tunit, sdev, count);
    return dummy / totalCount;
  }

  private static double scale(char tunit) {
    return tunit=='n' ? 1e9 : tunit=='u' ? 1e6 : tunit=='m' ? 1e3 : 0;
  }

  public static void SystemInfo() {
    System.out.printf("# OS:   %s; %s; %s%n", 
                      System.getProperty("os.name"), 
                      System.getProperty("os.version"), 
                      System.getProperty("os.arch"));
    System.out.printf("# JVM:  %s; %s%n", 
                      System.getProperty("java.vendor"), 
                      System.getProperty("java.version"));
    // The processor identifier works only on MS Windows:
    System.out.printf("# CPU:  %s; %d \"cores\"%n", 
                      System.getenv("PROCESSOR_IDENTIFIER"),
                      Runtime.getRuntime().availableProcessors());
    java.util.Date now = new java.util.Date();
    System.out.printf("# Date: %s%n", 
      new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(now));
  }
}

// ----------------------------------------------------------------------

// Common interface for long adders

interface ILongAdder {
  void add(long delta);
  long longValue();
}

// ----------------------------------------------------------------------

// A long adder that consists of a single private long field and
// lock-taking methods, following the Java "monitor pattern".

class SyncLongAdder implements ILongAdder {
  private long count = 0;
  private final Object lock = new Object();
  public void add(long delta) {
    synchronized (lock) {
      count += delta;
    }
  }
  public long longValue() {
    synchronized (lock) {
      return count;
    }
  }
}

// ----------------------------------------------------------------------

// A striped long adder that consists of multiple (typically 32 or 64)
// longs stored next to each other in an array, and a lock for each of
// these longs.

class SyncLongArrayAdder implements ILongAdder {
  private final static int logS = 6, stripeCount = 1 << logS, stripeMask = stripeCount - 1;
  private final long[] counts = new long[stripeCount];
  private final Object[] locks = new Object[stripeCount];

  public SyncLongArrayAdder() {
    for (int s=0; s<stripeCount; s++)
      locks[s] = new Object();
  }

  public void add(long delta) {
    final int s = (int)Thread.currentThread().threadId() & stripeMask; 
    synchronized (locks[s]) {
      counts[s] += delta;
    }
  }

  public long longValue() { 
    long result = 0;
    for (int s=0; s<stripeCount; s++)
      synchronized (locks[s]) {
        result += counts[s];
      }
    return result;
  }
}

// ----------------------------------------------------------------------

// A striped long adder that consists of multiple (typically 32 or 64)
// longs stored next to each other in an array, and a lock object for
// each of these longs, with padding between the longs (and locks) to
// avoid false sharing.

class SyncLongArrayAdderPadded implements ILongAdder {
  private final static int logS = 6, stripeCount = 1 << logS, stripeMask = stripeCount - 1, padding = 16;
  private final long[] counts = new long[stripeCount * padding];
  private final Object[] locks = new Object[stripeCount * padding];

  public SyncLongArrayAdderPadded() {
    for (int s=0; s<stripeCount * padding; s++)
      locks[s] = new Object();
  }

  public void add(long delta) {
    final int s = (int)Thread.currentThread().threadId() & stripeMask, p = s * padding;
    synchronized (locks[p]) {
      counts[p] += delta;
    }
  }

  public long longValue() { 
    long result = 0;
    for (int s=0; s<stripeCount; s++)
      synchronized (locks[s * padding]) {
        result += counts[s * padding];
      }
    return result;
  }
}

// ----------------------------------------------------------------------

// A long adder that is just a wrapped Java AtomicLong

class WrappedAtomicLong implements ILongAdder {
  private final AtomicLong atomicLong = new AtomicLong();

  public void add(long delta) {
    atomicLong.addAndGet(delta);
  }

  public long longValue() { 
    return atomicLong.get();
  }
}

// ----------------------------------------------------------------------

// A long adder that has a Java AtomicLong per thread.  We create a
// set of all the AtomicLongs belonging to the threads and add up the
// fragments by going through the set on the main thread.  The
// construction might actually work regardless of which thread calls
// the longValue method and when.

class ThreadLocalAtomicLong implements ILongAdder {
  private final ConcurrentHashMap<AtomicLong,Boolean> allAtomicLongs =
    new ConcurrentHashMap<>();
  private final ThreadLocal<AtomicLong> atomicLongHolder =
    ThreadLocal.withInitial(() -> {
        AtomicLong al = new AtomicLong();
        allAtomicLongs.put(al, Boolean.TRUE);
        return al;
      });

  public void add(long delta) {
    atomicLongHolder.get().addAndGet(delta);
  }

  public long longValue() {
    return allAtomicLongs.keySet().stream().mapToLong(al -> al.get()).sum();
  }
}

// ----------------------------------------------------------------------

// A long adder implementation based on AtomicLong and using CAS

class MyAtomicLong implements ILongAdder {
  private final AtomicLong atomicLong = new AtomicLong();

  public void add(long delta) {
    long oldValue, newValue;
    do {
      oldValue = atomicLong.get();
      newValue = oldValue + delta;
    } while (!atomicLong.compareAndSet(oldValue, newValue));
  }

  public long longValue() { 
    return atomicLong.get();
  }
}

// ----------------------------------------------------------------------

// An atomic long that is composed of 32 or 64 AtomicLongs stored next
// to each other in an array.

class LongAdderArray implements ILongAdder {
  private final static int logS = 6, stripeCount = 1 << logS, stripeMask = stripeCount - 1;
  private final AtomicLongArray counters = new AtomicLongArray(stripeCount);

  public void add(long delta) {
    counters.addAndGet((int)Thread.currentThread().threadId() & stripeMask, delta);
  }

  public long longValue() {
    long result = 0;
    for (int s=0; s<stripeCount; s++)
      result += counters.get(s);
    return result;
  }
}

// ----------------------------------------------------------------------

// An atomic long that is composed of 32 or 64 AtomicLongs stored in
// an array, with padding added in an attempt to avoid false cacheline
// sharing and hence M <-> I cache invalidation.  Inspired by the
// innards of Java 8's LongAdder.

class LongAdderArrayPadded implements ILongAdder {
  private final static int logS = 6, stripeCount = 1 << logS, stripeMask = stripeCount - 1, padding = 16;
  private final AtomicLongArray counters = new AtomicLongArray(stripeCount * padding);

  public void add(long delta) {
    counters.addAndGet(((int)Thread.currentThread().threadId() & stripeMask) * padding, delta);
  }

  public long longValue() {
    long result = 0;
    for (int s=0; s<stripeCount; s++)
      result += counters.get(s * padding);
    return result;
  }
}

// ----------------------------------------------------------------------

// A long adder that is a wrapped Java LongAdder

class WrappedLongAdder implements ILongAdder {
  private final LongAdder longAdder = new LongAdder();
  public void add(long delta) {
    longAdder.add(delta);
  }
  public long longValue() { 
    return longAdder.longValue();
  }
}


// ----------------------------------------------------------------------


class SyncLongArrayAdderLessPad implements ILongAdder {
  private final static int logS = 6;
  private final static int stripeCount = 1 << logS;
  private final static int stripeMask = stripeCount - 1;
  private final static int padding = 16;

  private final long[] counts = new long[stripeCount * padding];
  private final Object[] locks = new Object[stripeCount * padding];

  public SyncLongArrayAdderLessPad() {
    for (int s = 0; s < stripeCount * padding; s++) {
      if (s % padding == 0) {
        locks[s] = new Object();
      }
    }
  }

  public void add(long delta) {
    final int s =
      (int) Thread.currentThread().threadId() & stripeMask;
    final int p = s * padding;

    synchronized (locks[p]) {
      counts[p] += delta;
    }
  }

  public long longValue() {
    long result = 0;

    for (int s = 0; s < stripeCount; s++) {
      synchronized (locks[s * padding]) {
        result += counts[s * padding];
      }
    }

    return result;
  }
}
