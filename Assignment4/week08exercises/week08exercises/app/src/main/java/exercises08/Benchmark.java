// Simple microbenchmark setups for Java
// sestoft@itu.dk * 2013, 2015, 2026-08-19

// Compile and run with
//   javac Benchmark.java
//   java Benchmark > benchmark-out-yyyymmdd.txt

package exercises08;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.function.LongToDoubleFunction;

import exercises08.Timer;
import exercises08.SearchAndSort;

class Benchmark {
  public static void main(String[] args) {
    //SystemInfo();
    //Mark0();
    //Mark1();
    //Mark2();
    //Mark3();
    //Mark4();
    //Mark5();
    //Mark6('n', "multiply", i -> multiply(i));
    //Mark7('n', "multiply", i -> multiply(i));
    MathFunctionBenchmarks();
    // final java.util.Random rnd = new java.util.Random();
    // final int n = 1638400;
    // Mark8('n', "random_index", "", i -> rnd.nextInt(n));
    // SearchBenchmarks();
    // SearchScalabilityBenchmarks1();
    // SearchScalabilityBenchmarks2();
    // GetPseudorandomItems();
    // SortingBenchmarks();
    // SortingScalabilityBenchmarks();
    // MemoryLatency.MemoryLatencies();
    // BubbleSortBenchmarks();
    // BoxingBenchmark(); // For Java run-time data note
    // SimdExamples();
    // InstructionLevelParallelism();
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

  public static void Mark0() {         // USELESS
    Timer t = new Timer();
    double dummy = multiply(10);
    double time = t.check() * 1e9;
    System.out.printf("%6.1f ns%n", time);
  }

  public static void Mark1() {         // NEARLY USELESS
    Timer t = new Timer();
    long count = 100_000_000;
    for (long i=0; i<count; i++) {
      double dummy = multiply(i);
    }
    double time = t.check() * 1e9 / count;
    System.out.printf("%6.1f ns%n", time);
  }

  public static double Mark2() {
    Timer t = new Timer();
    long count = 100_000_000;
    double dummy = 0.0;
    for (long i=0; i<count; i++) 
      dummy += multiply(i);
    double time = t.check() * 1e9 / count;
    System.out.printf("%6.1f ns%n", time);
    return dummy;
  }

  public static double Mark3() {
    long n = 10;
    long count = 100_000_000;
    double dummy = 0.0;
    for (long j=0; j<n; j++) {
      Timer t = new Timer();
      for (long i=0; i<count; i++) 
        dummy += multiply(i);
      double time = t.check() * 1e9 / count;
      System.out.printf("%6.1f ns%n", time);
    }
    return dummy / n;
  }

  public static double Mark4() {
    long n = 10;
    long count = 100_000_000;
    double dummy = 0.0;
    double st = 0.0, sst = 0.0;
    for (long j=0; j<n; j++) {
      Timer t = new Timer();
      for (long i=0; i<count; i++) 
        dummy += multiply(i);
      double time = t.check() * 1e9 / count;
      st += time; 
      sst += time * time;
    }
    double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
    System.out.printf("%6.1f ns +/- %6.3f%n", mean, sdev);
    return dummy / n;
  }

  public static double Mark5() {
    long n = 10, count = 1, totalCount = 0;
    double dummy = 0.0, runningTime = 0.0, st = 0.0, sst = 0.0;
    do {
      count *= 2;
      st = sst = 0.0;
      for (long j=0; j<n; j++) {
        Timer t = new Timer();
        for (long i=0; i<count; i++) 
          dummy += multiply(i);
        runningTime = t.check();
        double time = runningTime * 1e9 / count;
        st += time; 
        sst += time * time;
        totalCount += count;
      }
      double mean = st/n, sdev = Math.sqrt((sst - mean*mean*n)/(n-1));
      System.out.printf("%6.1f ns +/- %8.2f %10d%n", mean, sdev, count);
    } while (runningTime < 0.25);
    return dummy / totalCount;
  }

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

  public static double Mark8(char tunit, String msg, String info, LongToDoubleFunction f) {
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
    System.out.printf("%-25s %s%15.1f %cs %10.2f %10d%n", msg, info, mean, tunit, sdev, count);
    return dummy / totalCount;
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
  
  // ========== Example functions and benchmarks ==========

  private static double multiply(long i) {
    double x = 1.1 * (double)(i & 0xFF);
     return x * x * x * x * x * x * x * x * x * x 
          * x * x * x * x * x * x * x * x * x * x;
  }

  private static void MathFunctionBenchmarks() {
    Mark7('n', "pow",  i -> Math.pow(10.0, 0.1 * (i & 0xFF)));
    Mark7('n', "exp",  i -> Math.exp(0.1 * (i & 0xFF)));
    Mark7('n', "log",  i -> Math.log(0.1 + 0.1 * (i & 0xFF)));
    Mark7('n', "sin",  i -> Math.sin(0.1 * (i & 0xFF)));
    Mark7('n', "cos",  i -> Math.cos(0.1 * (i & 0xFF)));
    Mark7('n', "tan",  i -> Math.tan(0.1 * (i & 0xFF)));
    Mark7('n', "asin", i -> Math.asin(1.0/256.0 * (i & 0xFF)));
    Mark7('n', "acos", i -> Math.acos(1.0/256.0 * (i & 0xFF)));
    Mark7('n', "atan", i -> Math.atan(1.0/256.0 * (i & 0xFF)));
    Mark7('n', "sqrt", i -> Math.sqrt(i));
    Mark7('n', "random", i -> Math.random());
    Mark7('n', "sqrt(random)", i -> Math.sqrt(Math.random()));
  }

  private static void SearchBenchmarks() {
    final int[] intArray = SearchAndSort.fillIntArray(10_000);  // sorted [0,1,...]
    final int successItem = 4900, failureItem = 14000;
    Mark7('n', "linear_search_success", 
          i -> SearchAndSort.linearSearch(successItem, intArray));
    Mark7('n', "linear_search_failure", 
          i -> SearchAndSort.linearSearch(failureItem, intArray));
    Mark7('n', "binary_search_success", 
          i -> SearchAndSort.binarySearch(successItem, intArray));
    Mark7('n', "binary_search_failure", 
          i -> SearchAndSort.binarySearch(failureItem, intArray));
  }

  private static void SearchScalabilityBenchmarks1() {
    for (int size = 100; size <= 10_000_000; size *= 2) {
      final int[] intArray = SearchAndSort.fillIntArray(size);  // sorted [0,1,...]
      final int successItem = (int)(0.49 * size);
      Mark8('n', "binary_search_success",
            String.format("%8d", size),
            i -> SearchAndSort.binarySearch(successItem, intArray)); 
    }
  }

  private static void SearchScalabilityBenchmarks2() {
    for (int size = 100; size <= 10_000_000; size *= 2) {
      final int[] intArray = SearchAndSort.fillIntArray(size);  // sorted [0,1,...]
      final int[] items = SearchAndSort.fillIntArray(size); 
      final int n = size;
      SearchAndSort.shuffle(items);
      Mark8('n', "binary_search_success",
            String.format("%8d", size),
            i -> SearchAndSort.binarySearch(items[(int)(i % n)], intArray));
    }
  }

  private static void GetPseudorandomItems() {
    for (int size = 100; size <= 10_000_000; size *= 2) {
      final int[] items = SearchAndSort.fillIntArray(size); 
      final int n = size;
      SearchAndSort.shuffle(items);
      Mark8('n', "get_pseudorandom_items", 
            String.format("%8d", size),
            i -> items[(int)(i % n)]);
    }
  }

  private static void SortingBenchmarks() {
    final int[] intArray = SearchAndSort.fillIntArray(10_000);
    Mark7('u', "shuffle int", 
          i -> { SearchAndSort.shuffle(intArray); return 0.0; });
    Mark8Setup('u', "shuffle", "", 
               () -> { },
               i -> { SearchAndSort.shuffle(intArray); return 0.0; });
    Mark8Setup('u', "selection_sort", "", 
               () -> SearchAndSort.shuffle(intArray),
               i -> { SearchAndSort.selsort(intArray); return 0.0; });
    Mark8Setup('u', "quicksort", "", 
               () -> SearchAndSort.shuffle(intArray),
               i -> { SearchAndSort.quicksort(intArray); return 0.0; });
    Mark8Setup('u', "heapsort", "", 
               () -> SearchAndSort.shuffle(intArray),
               i -> { SearchAndSort.heapsort(intArray); return 0.0; });
  }

  private static void SortingScalabilityBenchmarks() {
    for (int size = 100; size <= 50_000; size *= 2) {
      final int[] intArray = SearchAndSort.fillIntArray(size);
      Mark8Setup('u', "selection_sort", 
                 String.format("%8d", size),
                 () -> SearchAndSort.shuffle(intArray),
                 i -> { SearchAndSort.selsort(intArray); return 0.0; });
    }
    System.out.printf("%n%n"); // data set divider
    for (int size = 100; size <= 2_000_000; size *= 2) {
      final int[] intArray = SearchAndSort.fillIntArray(size);
      Mark8Setup('u', "quicksort", 
                 String.format("%8d", size),
                 () -> SearchAndSort.shuffle(intArray),
                 i -> { SearchAndSort.quicksort(intArray); return 0.0; });
    }
    System.out.printf("%n%n"); // data set divider
    for (int size = 100; size <= 2_000_000; size *= 2) {
      final int[] intArray = SearchAndSort.fillIntArray(size);
      Mark8Setup('u', "heapsort", 
                 String.format("%8d", size),
                 () -> SearchAndSort.shuffle(intArray),
                 i -> { SearchAndSort.heapsort(intArray); return 0.0; });
    }
  }

  // Bubble sort benchmarks
  private static void BubbleSortBenchmarks() {
    final int[] intArray = SearchAndSort.fillIntArray(10_000);
    // Bubblesort swap on ordered, reversed, random, shuffled or 121212... data
    Mark8Setup('u', "bbs swap ordered", "", 
               () -> { for (int i=0; i < intArray.length; i++) intArray[i] = i; },
               i -> { SearchAndSort.bubbleswap(intArray); return intArray[0]; });
    Mark8Setup('u', "bbs swap reverse", "", 
               () -> { for (int i=0; i < intArray.length; i++) intArray[i] = intArray.length - i; },
               i -> { SearchAndSort.bubbleswap(intArray); return intArray[0]; });
    Mark8Setup('u', "bbs swap random", "", 
               () -> SearchAndSort.random(intArray),
               i -> { SearchAndSort.bubbleswap(intArray); return intArray[0]; });
    Mark8Setup('u', "bbs swap shuffle", "", 
               () -> SearchAndSort.shuffle(intArray),
               i -> { SearchAndSort.bubbleswap(intArray); return intArray[0]; });
    Mark8Setup('u', "bbs swap 212121", "", 
               () -> { for (int i=0; i<intArray.length; i++) intArray[i] = 2 - (i%2); },
               i -> { SearchAndSort.bubbleswap(intArray); return intArray[0]; });
    // Bubblesort swap on ordered, reversed, random, shuffled or 121212... data
    Mark8Setup('u', "bbs move ordered", "", 
               () -> { for (int i=0; i < intArray.length; i++) intArray[i] = i; },
               i -> { SearchAndSort.bubblemove(intArray); return intArray[0]; });
    Mark8Setup('u', "bbs move reverse", "", 
               () -> { for (int i=0; i < intArray.length; i++) intArray[i] = intArray.length - i; },
               i -> { SearchAndSort.bubblemove(intArray); return intArray[0]; });
    Mark8Setup('u', "bbs move random", "", 
               () -> SearchAndSort.random(intArray),
               i -> { SearchAndSort.bubblemove(intArray); return intArray[0]; });
    Mark8Setup('u', "bbs move shuffle", "", 
               () -> SearchAndSort.shuffle(intArray),
               i -> { SearchAndSort.bubblemove(intArray); return intArray[0]; });
    Mark8Setup('u', "bbs move 212121", "", 
               () -> { for (int i=0; i<intArray.length; i++) intArray[i] = 2 - (i%2); },
               i -> { SearchAndSort.bubblemove(intArray); return intArray[0]; });
  }

  static class MemoryLatency {
    private static void MemoryLatencies() {
      final int K = 1 << 10, KB = K / 4, MB = K * KB, maxSize = 512 * MB;
      System.out.printf("Memory latency: %d identical operations k = arr[k]%n", maxSize);
      final int[] arr = new int[maxSize];
      final double factor = Math.sqrt(2.0);
      for (int size = 8*KB; size <= maxSize+1; size = (int)(Math.round(size * factor))) {
	fillCyclic(arr, size);
	int cycleSize = coverage(arr);
	if (cycleSize != size)
	  throw new RuntimeException("Cycle too short: " + cycleSize + " < " + size);
	Mark7('u', String.format("memory cover %11d B", size*4), (long i) -> jump(arr));
      }
    }

    // The design of the fillCyclic and jump methods below is explained
    // in "Microbenchmarks in Java, C# and C".
    
    private static void fillCyclic(int[] arr, int S) {
      ArrayList<Integer> indexes = new ArrayList<>();
      for (int k=0; k<S; k++) 
	indexes.add(k);
      Collections.shuffle(indexes);    
      // Make indexes[0] == 0.
      int shift = S - indexes.get(0);
      for (int k=0; k<S; k++) 
	indexes.set(k, (indexes.get(k) + shift) % S);
      // Make arr[0] = indexes[1], arr[arr[0]] = indexes[2], 
      // ..., arr^k[0] = indexes[k], ..., arr^S[0] = 0;
      // so the cyclic chain starting at arr[0] will have length S.
      int i = 0;
      for (int k=0; k<S; k++) 
	i = arr[i] = indexes.get((k+1) % S);
    }
    
    // The length of the index chain starting at arr[0]; for debugging only
    private static int coverage(int[] arr) {
      final BitSet indexes = new BitSet(arr.length);
      int k = 0;
      do {
	indexes.set(k);
	k = arr[k];
      } while (!indexes.get(k));
      return indexes.cardinality();
    }
    
    private static double jump(int[] arr) { 
      int k = 0;
      for (int j=0; j<arr.length; j++) 
	k = arr[k];
      return k;
    }
  }
  
  // For Java Run-Time Data note 2022
  private static void BoxingBenchmark() {
    Mark7('n', "no boxing", i ->
          { int sumi = 0;
            for (int j=1000; j<100000; j++)
              sumi += j;
            return sumi;
          });
    System.out.println(Mark7('n', "boxing", i -> 
          { Integer sumo = 0;
            for (Integer j=1000; j<100000; j++)
              sumo += j;
            return sumo;
          }));
  }

  // Demonstrate vectorisation with SIMD instructions 2025

  // On the Apple Aarch M M3pro addArrays performs 1m additions of
  // array elements in 0.1 ms, so 10bn additions per second.
  // Inspection of the code shows that it uses Neon instructions in
  // combination with loop unrolling.  
  
  static void SimdExamples() {
    final int size = 1_000_000;
    int[] a = SearchAndSort.fillIntArray(size), b = SearchAndSort.fillIntArray(size), c = new int[size];
    Mark6('n', "addIntArrays", i -> addIntArrays(a, b, c));
    double[] aa = Arrays.stream(a).mapToDouble(i -> (double)i).toArray(),
      bb = Arrays.stream(b).mapToDouble(i -> (double)i).toArray(),
      cc = new double[size];
    Mark6('n', "addDoubleArrays", i -> addDoubleArrays(aa, bb, cc));
    Mark6('n', "innerProduct", i -> innerProduct(aa, bb));
  }
  
  static double addIntArrays(int[] A, int[] B, int[] C) {
    for (int i=0; i<A.length; i++)
      C[i] = A[i] + B[i];
    return C[0];
  }

  static double addDoubleArrays(double[] A, double[] B, double[] C) {
    for (int i=0; i<A.length; i++)
      C[i] = A[i] + B[i];
    return C[0];
  }

  static double innerProduct(double[] A, double[] B) {
    double sum1 = 0.0, sum2 = 0.0, sum3 = 0.0, sum4 = 0.0;
    for (int i=0; i<A.length; i += 4) {
      sum1 = sum1 + A[i] * B[i];
      sum2 = sum2 + A[i+1] * B[i+1];
      sum3 = sum3 + A[i+2] * B[i+2];
      sum4 = sum4 + A[i+3] * B[i+3];
    }
    return sum1 + sum2;
  }

  
  
  // Demonstrate instruction-level parallelism 2025

  static void InstructionLevelParallelism() {
    Mark7('n', "loop1", i -> loop1(1_000_000));
    Mark7('n', "loop2", i -> loop2(1_000_000));
    Mark7('n', "loop3", i -> loop3(1_000_000));
    Mark7('n', "loop4", i -> loop4(1_000_000));
  }
  
  static double loop1(long n) {
    long sum1 = 0;
    for (long i=0; i<n; i++) {
      sum1 = sum1 + i;
    }
    return sum1;
  }

  // The C2-compiled loop1 code is straightforward:
  // 7658:   add        x11, x0, w14, sxtw
  // 765c:   add        w14, w14, #0x1
  // 7660:   add        x15, x15, x11               ;*ladd ; - Benchmark::loop1@15 (line 250)
  // 7664:   cmp        w14, w12
  // 7668:   b.lt 7658  // b.tstop

  static double loop2(long n) {
    long sum1 = 0, sum2 = 0;
    for (long i=0; i<n; i++) {
      sum1 = sum1 + i;
      sum2 = sum2 + sum1;
    }
    return sum2;
  }

  // The C2-compiled loop2 code is straightforward:
  // 7970:   add        x20, x20, x21               ;*ladd ; - Benchmark::loop2@18 (line 258)
  // 7974:   ldr        x10, [x28, #1104]
  // 7978:   add        x21, x21, #0x1              ;*ladd ; - Benchmark::loop2@29 (line 257)
  // 797c:   add        x22, x22, x20               ; ImmutableOopMap {} ; Benchmark::loop2@32 (line 257)
  // 7980:   ldr        wzr, [x10]                  ;*lload ; - Benchmark::loop2@8 (line 257)
  // 7984:   cmp        x21, x19
  // 7988:   b.lt       7970                        ; // b.tstop;*ifge {reexecute=0 rethrow=0 return_oop=

  static double loop3(long n) {
    long sum1 = 0, sum2 = 0, sum3 = 0;
    for (long i=0; i<n; i++) {
      sum1 = sum1 + i;
      sum2 = sum2 + sum1;
      sum3 = sum3 + sum2;
    }
    return sum3;
  }

  // The C2-compiled loop3 code is straightforward:
  // 8d00:   add        x22, x22, x20               ;*ladd ; - Benchmark::loop3@21 (line 267)
  // 8d04:   add        x23, x23, x22               ;*ladd ; - Benchmark::loop3@26 (line 268)
  // 8d08:   ldr        x10, [x28, #1104]
  // 8d0c:   add        x20, x20, #0x1              ;*ladd ; - Benchmark::loop3@39 (line 266)
  // 8d10:   add        x21, x21, x23               ; ImmutableOopMap {}
  // 8d14:   ldr        wzr, [x10]                  ;*lload
  // 8d18:   cmp        x20, x19
  // 8d1c:   b.lt       e8d00               ; 
  
  static double loop4(long n) {
    long sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0;
    for (long i=0; i<n; i++) {
      sum1 = sum1 + i;
      sum2 = sum2 + sum1;
      sum3 = sum3 + sum2;
      sum4 = sum4 + sum3;
    }
    return sum4;
  }

  // The C2-compiled loop4 code is straightforward:
  // ae68:   add        x11, x3, w14, sxtw
  // ae6c:   add        x6, x6, x11                 ;*ladd ; - Benchmark::loop4@24 (line 277)
  // ae70:   add        x5, x5, x6                  ;*ladd ; - Benchmark::loop4@29 (line 278)
  // ae74:   add        x0, x0, x5                  ;*ladd ; - Benchmark::loop4@36 (line 279)
  // ae78:   add        w14, w14, #0x1
  // ae7c:   add        x4, x4, x0                  ;*ladd ; - Benchmark::loop4@43 (line 280)
  // ae80:   cmp        w14, w12
  // ae84:   b.lt       ae68  // b.tstop

  // In most cases, there is some loop-unrolled code also, but no use of vector instructions. 
}
