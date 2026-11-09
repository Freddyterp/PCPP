// PCPP 

// Counting primes, using the executor framework and tasks (futures).
// sestoft@itu.dk * 2014-09-21, 2026-10-14

package exercises09;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongToDoubleFunction;

public class TestCountPrimesTasks {
  private static final ExecutorService executor = Executors.newWorkStealingPool();
  
  public static void main(String[] args) {
    SystemInfo();
    final int range = 1_000_000;
    // Mark7('u', "countSequential", i -> countSequential(range));
    for (int c = 1; c <= 2048; c = c<8 ? c+1 : c*2) {
      final int taskCount = c;
      Mark7('u', String.format("countParallelTask  %6d", taskCount), 
	    i -> countParallelTask(range, taskCount));
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

  // General parallel solution, using multiple (Runnable) tasks
  private static long countParallelTask(int range, int taskCount) {
    final int perTask = range / taskCount;
    final LongAdder lc = new LongAdder();
    List<Future<?>> futures = new ArrayList<Future<?>>();
    for (int t=0; t<taskCount; t++) {
      final int from = perTask * t, 
        to = (t+1 == taskCount) ? range : perTask * (t+1); 
      futures.add(executor.submit(() -> { 
          for (int i=from; i<to; i++)
            if (isPrime(i))
              lc.increment();
      }));
    }
    try {
      for (Future<?> fut : futures)
        fut.get();
    } catch (InterruptedException exn) { 
      System.out.println("Interrupted: " + exn);
    } catch (ExecutionException exn) { 
      throw new RuntimeException(exn.getCause()); 
    }
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
