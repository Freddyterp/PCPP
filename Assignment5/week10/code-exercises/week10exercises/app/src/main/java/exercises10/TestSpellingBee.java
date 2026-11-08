// PCPP: exercises 10.2.1-10.2.8.
// sestoft@itu.dk * 2026-10-26
package exercises10;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestSpellingBee {
  public static void main(String[] args) throws IOException {
    // Find the dictionary from the IDE or Gradle launch directory.
    // An explicit first argument always takes precedence.
    String filename = args.length == 0 ? TestWordStream.defaultWordFile() : args[0];

    // 10.2.1: the PDF asks for a Set, even though the starter declares a List.
    Set<String> cleanWords;
    try (Stream<String> words = readWords(filename)) {
      cleanWords = words.map(word -> word.toLowerCase(Locale.ROOT))
          .distinct()
          .filter(word -> word.length() >= 4)
          .filter(TestSpellingBee::onlyaz)
          .collect(Collectors.toSet());
    }
    System.out.println("10.2.1 - Clean words: " + cleanWords.size());

    // 10.2.3 asks for a List parameter, so make one list for that method.
    List<String> wordList = cleanWords.stream()
        .sorted()
        .collect(Collectors.toList());

    // 10.2.4: print the solutions to the example puzzle.
    Puzzle example = new Puzzle('h', letterSet("chikmnu"));
    System.out.println("10.2.4 - Solutions for " + example + ":");
    example.solutions(wordList).forEach(System.out::println);
    System.out.println("Example solution count: " + example.solutions(wordList).count());

    // 10.2.5: each distinct seven-letter set gives seven possible puzzles.
    List<Puzzle> puzzles = cleanWords.stream()
        .sorted()
        .map(TestSpellingBee::letterSet)
        .filter(letters -> letters.size() == 7)
        .distinct()
        .flatMap(letters -> letters.stream()
            .sorted()
            .map(mandatory -> new Puzzle(mandatory, letters)))
        .collect(Collectors.toList());
    System.out.println("10.2.5 - Puzzles: " + puzzles.size());

    // 10.2.6: print the first 20 puzzles.
    System.out.println("10.2.6 - First 20 puzzles:");
    puzzles.stream().limit(20).forEach(System.out::println);

    // 10.2.7: count solutions and keep puzzles with 10-80 solutions.
    // This direct solution is intentionally simple and can take several minutes.
    Timer sequentialTimer = new Timer();
    List<Puzzle> goodPuzzles = puzzles.stream()
        .filter(puzzle -> {
          long solutionCount = puzzle.solutions(wordList).count();
          return 10 <= solutionCount && solutionCount <= 80;
        })
        .collect(Collectors.toList());
    double sequentialTime = sequentialTimer.check();

    System.out.println("10.2.7 - Good puzzles: " + goodPuzzles.size());
    System.out.printf(Locale.ROOT, "Sequential time: %.3f s%n", sequentialTime);

    // 10.2.8: parallelize the outer stream; each puzzle is independent.
    Timer parallelTimer = new Timer();
    List<Puzzle> goodPuzzlesParallel = puzzles.parallelStream()
        .filter(puzzle -> {
          long solutionCount = puzzle.solutions(wordList).count();
          return 10 <= solutionCount && solutionCount <= 80;
        })
        .collect(Collectors.toList());
    double parallelTime = parallelTimer.check();

    // Both lists contain the same original Puzzle objects in encounter order.
    if (!goodPuzzles.equals(goodPuzzlesParallel)) {
      throw new AssertionError("Sequential and parallel results differ");
    }
    System.out.println("10.2.8 - Parallel good puzzles: " + goodPuzzlesParallel.size());
    System.out.printf(Locale.ROOT, "Parallel time: %.3f s; speedup: %.2fx%n",
        parallelTime, sequentialTime / parallelTime);
  }

  public static Stream<String> readWords(String filename) throws IOException {
    return Files.lines(Path.of(filename));
  }

  public static boolean onlyaz(String s) {
    return s.chars().allMatch(character -> 'a' <= character && character <= 'z');
  }

  public static Set<Character> letterSet(String s) {
    return s.chars()
        .mapToObj(character -> (char) character)
        .collect(Collectors.toSet());
  }

  static class Puzzle {
    final char m;
    final Set<Character> W;

    public Puzzle(char m, Set<Character> W) {
      if (W.size() != 7 || !W.contains(m)
          || !W.stream().allMatch(letter -> 'a' <= letter && letter <= 'z')) {
        throw new IllegalArgumentException("A puzzle needs seven a-z letters including m");
      }
      this.m = m;
      this.W = Set.copyOf(W); // Prevent later changes to the puzzle's letters.
    }

    // 10.2.2: candidates come from the cleaned dictionary.
    public boolean hasSolution(String s) {
      return s.length() >= 4
          && s.indexOf(m) >= 0
          && letterSet(s).stream().allMatch(W::contains);
    }

    // 10.2.3: only dictionary words satisfying the puzzle rules are returned.
    public Stream<String> solutions(List<String> cleanWords) {
      return cleanWords.stream().filter(this::hasSolution);
    }

    public String toString() {
      return String.format("(%c, %s)", m,
          W.stream().sorted().map(String::valueOf)
              .collect(Collectors.joining(",", "{", "}")));
    }
  }
}
