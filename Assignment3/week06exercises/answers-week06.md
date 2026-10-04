# Week 06 Answers

## Exercise 6.1
### 6.1.1
**Implementation:** "CasHistogram" implements "Histogram" using one "AtomicInteger" per bin. The updates use CAS retry loops with only "get()" and "compareAndSet()". Failed updates retry without acquiring a lock. 

The array and its bin objects are private and never returned. The constructor does not expose "this". The final array reference and the bin objects are initialized before the constructor finishes. Initialization using "final" guarantees visibility of this initial state. Following updates on bin objects use atomic operations.

"counts" reference, array length, and bin references are immutable, but the bin counts value is mutable. 

**Result:** command "gradle --no-daemon classes" completes successfully. 

### 6.1.2
**Notes:** Yes. "getAndClear()" is atomic because "compareAndSet(oldVal, 0)" both verifies the count and resets it to zero in a single atomic operation. The count is returned immediately before that reset. Failed CAS attempts retry with a fresh value. Concurrent increments are either included in that returned count, or they occur are the reset and remain in the bin. 

### 6.1.3
**Implementation:** A JUnit test counts prime factors for the range 0-4999 using 30 bins. The reference result is computed sequentially with "Histogram1". One virtual thread per number updates a shared "CasHistogram", using its "countFactors()" method. 

A latch is used to coordinate the workers' start. Futures ensure that all workers finish and propagate worker exceptions before eaach bin is compared with the result.

**Result:** The test passed. All 30 bins matched the sequential histogram.

**Command:** "gradle --no-daemon cleanTest test --tests 'exercises06.TestHistograms.parallelHistogramMatchesSequential'"

## Exercise 6.2
### 6.2.1

### 6.2.2

### 6.2.3

### 6.2.4

### 6.2.5

### 6.2.6