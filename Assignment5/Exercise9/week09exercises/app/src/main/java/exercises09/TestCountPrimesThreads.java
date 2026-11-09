// PCPP 

// Counting primes, using multiple threads (platform or virtual) for better performance.
// sestoft@itu.dk * 2014-08-31, 2026-10-14

package exercises09;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongToDoubleFunction;

public class TestCountPrimesThreads {
  public static void main(String[] args) {
    SystemInfo();
    final int range = 1_000_000;
    Mark7('u', String.format("countSequential"), i ->  countSequential(range));
    for (int c = 1; c <= 2048; c = c<8 ? c+1 : c*2) {
      final int threadCount = c;
      /*Mark7('u', String.format("countParallelPlatf %6d", threadCount), 
            i -> countParallel(range, Thread.ofPlatform(), threadCount));*/
      Mark7('u', String.format("countParallelVirtu %6d", threadCount), 
            i -> countParallel(range, Thread.ofVirtual(), threadCount));
    }
  }

  private static boolean isPrime(int n) {
    int k = 2;
    while (k * k <= n && n % k != 0)
      k++;
    return n >= 2 && k * k > n;
  }

  // Sequential solution
  private static long countSequential(int range) {
    long count = 0;
    final int from = 0, to = range;
    for (int i=from; i<to; i++)
      if (isPrime(i)) 
        count++;
    return count;
  }

  // General parallel solution, using multiple threads (platform or virtual)
  private static long countParallel(int range, Thread.Builder tb, int threadCount) {
    final int perThread = range / threadCount;
    final LongAdder lc = new LongAdder();
    Thread[] threads = new Thread[threadCount];
    for (int t=0; t<threadCount; t++) {
      final int from = perThread * t, to = (t+1==threadCount) ? range : perThread * (t+1); 
      threads[t] = tb.start(() -> {
	  for (int i=from; i<to; i++)
	    if (isPrime(i))
	      lc.increment();
	});
    }
    try {
      for (int t=0; t<threadCount; t++) 
        threads[t].join();
    } catch (InterruptedException exn) { }
    return lc.sum();
  }

  // --- Benchmarking infrastructure ---

  private static double scale(char tunit) {
    return tunit=='n' ? 1e9 : tunit=='u' ? 1e6 : tunit=='m' ? 1e3 : 0;
  }

  public static double Mark6(char tunit, String msg, LongToDoubleFunction f) {
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
      double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
      System.out.printf("%-25s %15.1f %cs %10.2f %10d%n", msg, mean, tunit, sdev, count);
    } while (runningTime < 0.25);
    return dummy / totalCount;
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
