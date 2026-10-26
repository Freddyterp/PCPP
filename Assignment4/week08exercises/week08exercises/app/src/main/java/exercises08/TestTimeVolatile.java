// PCPP 

// sestoft@itu.dk * 2026-10-15
package exercises08;

import java.util.function.LongToDoubleFunction;

public class TestTimeVolatile {
  public static void main(String[] args) {
    SystemInfo();
    Mark7('u', "Non-volatile", i -> {
      LongHolder lh = new LongHolder();
      lh.callAdd(i);
      return lh.value();
    });

    Mark7('u', "Volatile", i -> {
      LongHolder lh = new LongHolder();
      lh.callAddVolatile(i);
      return lh.valueVolatile();
    });
  }

  static class LongHolder {
    private long counter;
    private volatile long counterVolatile;

    public long value() { return counter; }
    public void add(long delta) { counter += delta; }
    private void callAdd(long i) {
      for (int j=0; j<1_000_000; j++)
        add(i+j);
    }

    public long valueVolatile() { return counterVolatile; }
    public void addVolatile(long delta) { counterVolatile += delta; }
    private void callAddVolatile(long i) {
      for (int j=0; j<1_000_000; j++)
        addVolatile(i+j);
    }
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
