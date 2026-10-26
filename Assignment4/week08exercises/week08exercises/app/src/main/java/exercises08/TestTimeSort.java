// Benchmarks on quicksort, Arrays.sort, Arrays.parallelSort
// sestoft@itu.dk * 2026-10-18

// Compile and run with
//   javac TestTimeSort.java
//   java TestTimeSort
// or, to check sortedness of the results:
//   java -enableassertions TestTimeSort
package exercises08;
 
import java.util.Arrays;
import java.util.function.LongToDoubleFunction;

class TestTimeSort {
  public static void main(String[] args) {
    SystemInfo();
    final int[] intArray = Quicksort.fillIntArray(100_000);
    Mark8Setup('u', "quicksort", "", 
               () -> Quicksort.shuffle(intArray),
               i -> {
                 Quicksort.quicksort(intArray);
                 assert Quicksort.isSorted(intArray);
                 return intArray[37];
               });

    Mark8Setup('u', "Array.sort", "", 
      ()-> Quicksort.shuffle(intArray),
      i -> {
        Arrays.sort(intArray);
        return intArray[37];
      }
    );

    Mark8Setup('u', "Arrays.parallelSort", "",
    () -> Quicksort.shuffle(intArray),
    i -> {
        Arrays.parallelSort(intArray);
        return intArray[37];
    });

    Mark8Setup('u', "Parallel quicksort", "",
    () -> Quicksort.shuffle(intArray),
    i -> {
        Quicksort.quicksort(intArray);
        return intArray[37];
    });
  }

  // ========== Infrastructure code ==========

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

  private static double scale(char tunit) {
    return tunit=='n' ? 1e9 : tunit=='u' ? 1e6 : tunit=='m' ? 1e3 : 0;
  }

  public static double Mark8Setup(char tunit, String msg, String info, Runnable setup, LongToDoubleFunction f) {
    long n = 10, count = 1, totalCount = 0;
    double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
    do { 
      count *= 2;
      st = sst = 0.0;
      for (long j=0; j<n; j++) {
        Timer t = new Timer();
        for (long i=0; i<count; i++) {
          t.pause();
          setup.run();
          t.play();
          dummy += f.applyAsDouble(i);
        }
        runningTime = t.check();
        double time = runningTime * scale(tunit) / count;
        st += time; 
        sst += time * time;
        totalCount += count;
      }
    } while (runningTime < 0.25);
    double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
    System.out.printf("%-25s %s%15.1f %cs %10.2f %10d%n", msg, info, mean, tunit, sdev, count);
    return dummy / totalCount;
  }

  // Example code for microbenchmark note, quicksort only
  // sestoft@itu.dk * 2013-08-01, 2026-10-17
  
  static class Quicksort {
    public static void swap(int[] arr, int s, int t) {
      int tmp = arr[s];  arr[s] = arr[t];  arr[t] = tmp;
    }
    
    // Sequential quicksort: sort arr[a..b]
    private static void qsort(int[] arr, int a, int b) {
      if (a < b) {
        int i = a, j = b;
        int x = arr[(i + j) / 2];

        do {
            while (arr[i] < x) i++;
            while (arr[j] > x) j--;

            if (i <= j) {
                swap(arr, i, j);
                i++;
                j--;
            }
        } while (i <= j);

        if (j - a > 50) {
            final int left = a, right = j;

            Thread t = Thread.ofVirtual().start(() ->
                qsort(arr, left, right));

            qsort(arr, i, b);

            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        } else {
            qsort(arr, a, j);
            qsort(arr, i, b);
        }
      }
    }
    
    public static void quicksort(int[] arr) {
      qsort(arr, 0, arr.length-1);
      assert isSorted(arr);
    }
    
    public static int[] fillIntArray(int n) {
      int [] arr = new int[n];
      for (int i = 0; i < n; i++)
        arr[i] = i;
      return arr;
    }
    
    public static boolean isSorted(int[] arr) {
      for (int i=1; i<arr.length; i++)
        if (arr[i-1] > arr[i])
          return false;
      return true;
    }
    
    private static final java.util.Random rnd = new java.util.Random();
    
    public static void shuffle(int[] arr) {
      for (int i = arr.length-1; i > 0; i--)
        swap(arr, i, rnd.nextInt(i+1));
    }
    
    public static void random(int[] arr) {
      for (int i = arr.length-1; i > 0; i--)
        arr[i] = rnd.nextInt(i+1);
    }
  }
}
