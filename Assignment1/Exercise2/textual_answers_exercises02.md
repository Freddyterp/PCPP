# Exercise 2.1.
## 1.
Reader enters enterRead() -> monitor lock acquired -> check for active writer -> if yes, reader wait() -> monitor lock released while sleeping.
Possible states: 1 writer 0 readers or 0+ readers and 0 writers.

## 2.
The solution gives priority to queued writer threads over newly arrived reader threads. When a writer is in queue, no new reader may enter. This ensures that the number of active readers eventually falls to zero, allowing a waiting writer to proceed instead of being locked out by new readers indefinitely.

## 3.
The implementation doesn't use separate explicit condition variables. The threads call wait() on the monitor object and are awakened using notifyAll(). The readers and writers do have different logical conditions expressed by their while loops.
After being awakened, the threads will reevaluate its condition before proceeding. 

# Exercise 2.2.
## 1. 
In the test run the worker thread did not observe the main thread's update to "mi.value", and the program runs indefinitely.
This is because access to "value" is unsynchronized. There is no guarantee that the write from the main thread becomes visible to thread "t".

## 2.
After redeclaring both the set() and get() method to synchronized, they acquire the same lock on the MutableInteger object. 
This is because we introduce a happens-before concept. When the main thread exits set(), the write of 42 happens before the acquisition of the same monitor by get().
This way thread t is guaranteed to eventually observe the updated value and the loop gets terminated.

## 3.
If get() is not synchronized, thread t does not acquire the same intrinsic lock that the main thread releases after set(). 
This way there is no happens-before relationship that guarantees that the write to value becomes visible to the worker thread. 
The program may terminate in some instances, but the guarantee of execution is removed.

## 4.
Declaring value as volatile guarantees visibility of writes between threads. The main thread's write of 42 happens before subsequent reads of the same volatile variable by the worker thread.
So t will eventually observe the written value, leave its loop, and terminate. 


# Exercise 2.3.
## 1.
Run 1: Sum is 1759127.0 and should be 2000000
Run 2: Sum is 1680184.0
Run 3: Sum is 1614671.0

The results are inconsistent, but consistently below the expected value. This indicates that concurrent updates get lost during execution because of non-atomic interleaving between threads.

## 2.
Static methods don't have a "this" object. A synchronized static method instead locks the class objects itself. 
The two "synchronized" locks operate on different objects and do not provide mutual exclusivity.
addInstance() acquires the lock of the Mystery instance "m" and addStatic acquires the lock of "Mystery.class".

## 3.
The race condition can be eliminated by making addStatic() and addInstance() synchronize on the same intrinsic lock (Mystery.class).
So by modifying the addInstance() method in mystery class to apply the lock appropriately, we guarantee mutual exclusion and each run returns the expected sum.

## 4.
After fixing the update exclusivity in exercise 2.3.3. sum() does not need to be synchronized for this program. The main thread waits until both worker threads have finished before reading sum.
Thread.join() creates a happens-before relationship that ensures that the main thread observes the writes performed by the joined threads.