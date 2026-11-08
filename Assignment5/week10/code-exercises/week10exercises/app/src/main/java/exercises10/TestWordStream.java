// PCPP: exercises 10.1.1-10.1.14.
// sestoft@itu.dk * 2015-09-08, 2026-10-26
package exercises10;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestWordStream {
  public static void main(String[] args) throws IOException {
    // Find the dictionary from the IDE or Gradle launch directory.
    // An explicit first argument always takes precedence.
    String filename = args.length == 0 ? defaultWordFile() : args[0];

    // 10.1.1: count all words in the file.
    try (Stream<String> words = readWords(filename)) {
      System.out.println("10.1.1 - Number of words: " + words.count());
    }

    // 10.1.2: print the first 20 words.
    System.out.println("10.1.2 - First 20 words:");
    try (Stream<String> words = readWords(filename)) {
      words.limit(20).forEach(System.out::println);
    }

    // 10.1.3: print words with at least 22 letters.
    System.out.println("10.1.3 - Words with at least 22 letters:");
    try (Stream<String> words = readWords(filename)) {
      words.filter(word -> word.length() >= 22)
          .forEach(System.out::println);
    }

    // 10.1.4: convert to lowercase and print the first 20 words.
    System.out.println("10.1.4 - First 20 lowercase words:");
    try (Stream<String> words = readWords(filename)) {
      words.map(word -> word.toLowerCase(Locale.ROOT))
          .limit(20)
          .forEach(System.out::println);
    }

    // 10.1.5: discard one-letter words and words containing non-a-z letters.
    // The supplied dictionary gives 74,533 here. The PDF's 72,097 is a typo:
    // 72,097 is the distinct, at-least-four-letter count used in exercise 10.2.
    try (Stream<String> words = readWords(filename)) {
      long count = words.map(word -> word.toLowerCase(Locale.ROOT))
          .filter(word -> word.length() > 1)
          .filter(TestWordStream::onlyaz)
          .count();
      System.out.println("10.1.5 - Filtered words: " + count);
    }

    // 10.1.6: apply the same rules and remove duplicates.
    List<String> cleanWords;
    try (Stream<String> words = readWords(filename)) {
      cleanWords = words.map(word -> word.toLowerCase(Locale.ROOT))
          .filter(word -> word.length() > 1)
          .filter(TestWordStream::onlyaz)
          .distinct()
          .collect(Collectors.toList());
    }
    System.out.println("10.1.6 - Distinct clean words: " + cleanWords.size());

    // 10.1.7: print the palindromes.
    System.out.println("10.1.7 - Palindromes:");
    cleanWords.stream()
        .filter(TestWordStream::isPalindrome)
        .forEach(System.out::println);

    // 10.1.8: group words by length and count the groups.
    Map<Integer, List<String>> lengthGroups = cleanWords.stream()
        .collect(Collectors.groupingBy(String::length));
    System.out.println("10.1.8 - Length groups: " + lengthGroups.size());

    // 10.1.9: print the group containing the 19-letter words.
    System.out.println("10.1.9 - Words of length 19:");
    System.out.println(lengthGroups.getOrDefault(19, List.of()));

    // 10.1.10: print letter counts for the first 20 clean words.
    System.out.println("10.1.10 - Letter counts:");
    cleanWords.stream()
        .map(TestWordStream::counts)
        .limit(20)
        .forEach(System.out::println);

    // 10.1.11: words with the same letter counts belong to the same group.
    // Measure collection only; printing is outside the timed section.
    Timer sequentialTimer = new Timer();
    Map<Map<Character, Long>, List<String>> anagram1 = cleanWords.stream()
        .collect(Collectors.groupingBy(TestWordStream::counts));
    double sequentialTime = sequentialTimer.check();

    System.out.println("10.1.11 - All anagram groups:");
    anagram1.entrySet().stream().forEach(System.out::println);

    System.out.println("10.1.11 - Non-trivial anagram groups:");
    anagram1.entrySet().stream()
        .filter(entry -> entry.getValue().size() > 1)
        .forEach(System.out::println);

    long nonTrivialGroups = anagram1.values().stream()
        .filter(group -> group.size() > 1)
        .count();
    System.out.println("Non-trivial groups: " + nonTrivialGroups);

    // 10.1.12: collect the same groups using a parallel stream.
    Timer parallelTimer = new Timer();
    Map<Map<Character, Long>, List<String>> anagramParallel = cleanWords.parallelStream()
        .collect(Collectors.groupingBy(TestWordStream::counts));
    double parallelTime = parallelTimer.check();

    System.out.println("10.1.12 - Parallel groups: " + anagramParallel.size());
    System.out.printf(Locale.ROOT,
        "Sequential: %.3f s; parallel: %.3f s; speedup: %.2fx%n",
        sequentialTime, parallelTime, sequentialTime / parallelTime);

    // 10.1.13: use a concurrent collector to avoid merging partial maps.
    Timer concurrentTimer = new Timer();
    Map<Map<Character, Long>, List<String>> anagramConcurrent = cleanWords.parallelStream()
        .collect(Collectors.groupingByConcurrent(TestWordStream::counts));
    double concurrentTime = concurrentTimer.check();

    System.out.println("10.1.13 - Concurrent groups: " + anagramConcurrent.size());
    System.out.printf(Locale.ROOT,
        "Concurrent: %.3f s; speedup over sequential: %.2fx%n",
        concurrentTime, sequentialTime / concurrentTime);

    // Parallel work has splitting, scheduling and collection costs. It may be
    // slower for this data size. These are single-run timings; repeat runs
    // before drawing conclusions because JVM warm-up and GC affect them.

    // 10.1.14: demonstrate the stream-based letter-counting method.
    System.out.println("10.1.14 - Stream-based letter counts:");
    cleanWords.stream()
        .map(TestWordStream::countsS)
        .limit(20)
        .forEach(System.out::println);
  }

  // 10.1.7: a loop is explicitly allowed for the palindrome test.
  public static boolean isPalindrome(String s) {
    for (int left = 0, right = s.length() - 1; left < right; left++, right--) {
      if (s.charAt(left) != s.charAt(right)) {
        return false;
      }
    }
    return true;
  }

  // 10.1.14: group characters and count the occurrences in each group.
  public static Map<Character, Long> countsS(String s) {
    return s.chars()
        .mapToObj(character -> (char) character)
        .collect(Collectors.groupingBy(
            character -> character, TreeMap::new, Collectors.counting()));
  }

  // Keep the method name provided in the starter file as well.
  public static Map<Character, Long> counts2(String s) {
    return countsS(s);
  }

  // The IDE may launch from the repository root; Gradle launches from app.
  // Check the usual project directories without duplicating the dictionary.
  public static String defaultWordFile() throws IOException {
    return Stream.of(
        "words.txt",
        "app/words.txt",
        "week10exercises/app/words.txt",
        "code-exercises/week10exercises/app/words.txt",
        "week10/code-exercises/week10exercises/app/words.txt")
        .filter(filename -> Files.isRegularFile(Path.of(filename)))
        .findFirst()
        .orElseThrow(() -> new IOException(
            "Cannot find words.txt. Pass its path as the first program argument."));
  }

  // Callers use try-with-resources so each file stream is closed.
  public static Stream<String> readWords(String filename) throws IOException {
    return Files.lines(Path.of(filename));
  }

  public static boolean onlyaz(String s) {
    return s.chars().allMatch(character -> 'a' <= character && character <= 'z');
  }

  // The original loop-based method, used in the anagram exercises.
  public static Map<Character, Long> counts(String s) {
    Map<Character, Long> result = new TreeMap<>();
    for (char character : s.toCharArray()) {
      result.putIfAbsent(character, 0L);
      result.put(character, result.get(character) + 1);
    }
    return result;
  }
}
