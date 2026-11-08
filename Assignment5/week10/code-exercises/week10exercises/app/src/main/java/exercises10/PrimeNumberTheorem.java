// PCPP: exercises 10.3.1-10.3.3.
package exercises10;

import java.util.Arrays;
import java.util.Locale;

public class PrimeNumberTheorem {
  public static void main(String[] args) {
    final int N = 10_000_001;
    int[] a = new int[N];

    // 10.3.1: mark each prime with 1 and each non-prime with 0.
    Arrays.parallelSetAll(a, i -> isPrime(i) ? 1 : 0);

    // 10.3.2: a[i] now counts primes less than or equal to i.
    Arrays.parallelPrefix(a, Integer::sum);
    System.out.println("Primes up to 10,000,000: " + a[10_000_000]);

    // 10.3.3: N-1 is the last valid index, so include 10,000,000, not N.
    System.out.println("n\tpi(n)\tpi(n) / (n / ln(n))");
    for (int step = 1; step <= 10; step++) {
      int i = (N - 1) / 10 * step;
      double ratio = a[i] / (i / Math.log(i));
      System.out.printf(Locale.ROOT, "%d\t%d\t%.9f%n", i, a[i], ratio);
    }

    // Over this range the ratio decreases toward 1, but slowly.
    // The prime number theorem states convergence as n tends to infinity;
    // it does not promise a decrease at every individual integer.
  }

  public static boolean isPrime(int n) {
    if (n < 2) {
      return false;
    }
    for (int divisor = 2; (long) divisor * divisor <= n; divisor++) {
      if (n % divisor == 0) {
        return false;
      }
    }
    return true;
  }
}
