# Week 05 answers

## Exercise 5.1
### 5.1.1
**Implementation:** We wrote a repeated JUnit test where 16 threads add the same integer to "ConcurrentIntegerSetBuggy". A "CyclicBarrier" is used to coordinate the start of execution of the worker threads. We check that exactly one call returns "true" and the final size is 1.

**Result:** Repetition 500 fails because two calls return "true". The previous 499 passed, and the remaining 500 were skipped. 

**Notes:** Two threads can both observe an empty bucket before either inserts. Both then insert and return "true", which violates the set contract. This explains a possible interleaving. The test does not record the exact execution order. 

**Command:** 
gradle --no-daemon cleanTest test --tests 'exercises05.ConcurrentSetTest.concurrentAddSameElement'

### 5.1.2
**Implementation:** The set was populated sequentially with 16000 distinct integers. Then 16 threads each remove 1000 assigned elements, using a barrier to coordinate their start.

**Result:** The first repetition failed. All 16000 removals reported success, but the final size was 5428 instead of 0.

**Notes:** A possible cause is lost decrements. Two threads read the same size and both write that value minus one. Two removals decrease the counter only once. The test detects the incorrect size but does not record the exact interleaving. 

### 5.1.3
**Implementation:** In "ConcurrentIntegerSetSync", the add, remove and size methods are now synchronized. All three methods acquire the same instance lock, ensuring mutual exclusion and visibility. This prevents duplicate succesful insertions and lost size updates. 

**Result:** Both tests pass all 1000 repetitions after synchronization being added to the aforementioned methods. 

### 5.1.4
**Implementation:** Without changing the tests, we ran them against "ConcurrentIntegerSetLibrary".

**Result:** Both tests pass all 1000 repetitions.

**Notes:** This behaviour matches that which is expected of the thread-safe "ConcurrentSkipListSet". Unlike the buggy implementation, there were no duplicate succesful additions or incorrect final sizes observed. This increases confidence in, but does not conclude or prove thread safety. 

### 5.1.5
**Notes:** Yes,m provided the test is correct and the failure demonstrates a violation of the set contract during concurrent execution. One counterexample is sufficient to disprove thread safety. The tests exposed duplicate successful additions and an incorrect final size of the set.

### 5.1.6
**Notes:** No, passing tests does not prove thread-safety, even when only using "add()" and "remove()". The tests only cover the executions that occured. Untested interleavings may still cause errors. Repetition increases confidence but doesn't prove thread safety. A proof must cover all possible executions. 

## Exercise 5.2
### 5.2.1
**Counterexample:** With capacity 1, the main thread calls "release()" before any acquisition, changing the internal state from 0 to -1. Worker A calls "acquire()", changing the state to 0, and remains inside. Worker B then calls "acquire()", which changes the state to 1. Two workers are now inside despite the capacity being 1.

"release()" allows the state to become negative, so it no longer represents the number of threads inside. This counterexample uses an unmatched "release()". 

### 5.2.2
**Implementation:** A JUnit test with capacity 1 was written. The main thread calls "release()" before starting the workers. A latch holds the first worker inside after acquisition; the second then attempts to acquire. An atomic counter checks whether occupancy exceeds 1. 

**Result:** The test failed because two threads entered simultaneously with capacity being 1. Calling "release()" before any acquisition made the internal state negative, allowing both acquisitions to succeed. This confirms the counterexample from 5.2.1. 