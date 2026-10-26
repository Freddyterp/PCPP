// PCPP 

// sestoft@itu.dk * 2014-09-10, 2026-10-14

// Microbenchmarks for small object creation, Thread creation
// (platform and virtual), thread start, thread execution and join,
// taking an uncontended lock.
package exercises08;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongToDoubleFunction;

public class TestTimeThreads {
  public static void main(String[] args) {
    SystemInfo();
    final Point myPoint = new Point(42, 39);
    Mark7('n', "hashCode()", i -> myPoint.hashCode());
    Mark7('n', "Point creation", 
          i -> {
            Point p = new Point((int)i, (int)i);
            return p.hashCode();
          });
    Mark7('n', "Thread's work", 
          i -> {
	    final AtomicInteger ai = new AtomicInteger(); 
            for (int j=0; j<1000; j++)
              ai.getAndIncrement();
            return ai.doubleValue();
          });
    Mark7('n', "Thread pltf cre", 
          i -> {
	    final AtomicInteger ai = new AtomicInteger(); 
            Thread t = Thread.ofPlatform().unstarted(() -> {
                for (int j=0; j<1000; j++)
                  ai.getAndIncrement();
              });
            return t.hashCode();
          });
    Mark7('n', "Thread pltf cre strt", 
          i -> {
	    final AtomicInteger ai = new AtomicInteger(); 
            Thread t = Thread.ofPlatform().start(() -> {
              for (int j=0; j<1000; j++)
                ai.getAndIncrement();
            });
            return t.hashCode();
          });
    Mark7('n', "Thread pltf cre strt join", 
          i -> {
	    final AtomicInteger ai = new AtomicInteger(); 
            Thread t = Thread.ofPlatform().start(() -> {
              for (int j=0; j<1000; j++)
                ai.getAndIncrement();
              });
            try { t.join(); } 
            catch (InterruptedException exn) { }
            return t.hashCode();
          });
    Mark7('n', "Thread virt cre", 
          i -> {
	    final AtomicInteger ai = new AtomicInteger(); 
            Thread t = Thread.ofVirtual().unstarted(() -> {
                for (int j=0; j<1000; j++)
                  ai.getAndIncrement();
              });
            return t.hashCode();
          });
    // The measurement below does not make much sense, so we exclude
    // it.  The problem is that creating and starting a virtual thread
    // is fast, so timing this creates huge number of them, but not
    // join()ing them seems to cause them to just hang there and not
    // be garbage collected.  This causes a space leak (huge and
    // pointless memory usage), which does NOT appear for platform
    // threads.  Maybe the non-joined virtual threads are worse
    // because they get suspended and their stacks are copied to heap,
    // where they languish.  Garbage collection of platform threads
    // may be more effective just because they hold on to a sparse
    // resource: operating system threads.
    // Mark6('n', "Thread virt cre strt", 
    //       i -> {
    //         Thread t = tbVirtual.start(() -> {
    //           for (int j=0; j<1000; j++)
    //             ai.getAndIncrement();
    //         });
    //         return t.hashCode();
    //       });
    Mark7('n', "Thread virt cre strt join", 
          i -> {
	    final AtomicInteger ai = new AtomicInteger(); 
            Thread t = Thread.ofVirtual().start(() -> {
              for (int j=0; j<1000; j++)
                ai.getAndIncrement();
              });
            try { t.join(); } 
            catch (InterruptedException exn) { }
            return t.hashCode();
          });
    final Object obj = new Object();
    Mark7('n', "Uncontended lock", 
          i -> {
            synchronized (obj) {
              return i;
            }
          });
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

/**
 * Immutable Point class used by DelegatingVehicleTracker
 * @author Brian Goetz and Tim Peierls
 */
class Point {
  public final int x, y;  
  public Point(int x, int y) {
    this.x = x; this.y = y;
  }
}
