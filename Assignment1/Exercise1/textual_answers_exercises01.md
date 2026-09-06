
# Exercise 1.1
## Question 1
the value we got was 19.643.714 and it was not the expected value of 20.000.000

## Question 2
Since the count got reduced that inturn affects the interleaving of the program and that makes it appear that the program is without a data race even thought the race condition is still there

## Question 3
It wouldn't make a difference in this case, since all three variants have the same effect: they read the current value of count, add 1, and store the result back in count.

Conceptually, this can be thought of as:

```java
int temp = count
temp = temp + 1
count = temp
```

## Question 4
Since the critical section is protected by mutual exclusion, only one thread can execute it at a time. This means that interleavings where one thread is interrupted in the middle of the critical section and another thread enters the same critical section are no longer possible.

## Question 5
Our critical section is the least number of lines of code since the only line we are proctecting with a lock is the read-modify-write line of the shared variable

# Exercise 1.2
## Question 2
```java
System.out.print("-"); (1)
try {Thread.sleep(50);} catch (InterruptedException exn) {} (2)
System.out.print("|"); (3)
```
Since Thread.sleep() does not provide synchronization, and the scheduler is nondeterministic, the two threads can be interleaved in different ways. That means that the program contains a race condition

t1(1), t1(2), t2(1), t2(2), t1(3), t1(1), t1(2), t2(3), t2(1), t2(2)
## Question 3
The solution is correct because the whole sequence that prints - and then | is one critical section protected by a ReentrantLock. The lock provides mutual exclusion, so only one thread can execute this critical section at a time. This also means that interleavings with split critial sections are no longer possible.

# Exercise 1.3
Since we now have to check if a counter has reach a maximum number we have a check-then-act race condition and to fix this, we used a ReentrantLock to make the entire check and increment atomic

# Exercise 1.4
## Question 1
There is a lot of overlap between Goetz and Nygaard:

- **Fairness & Hidden**
A scheduler may use concurrency mainly to make sure several users or programs get a fair share of CPU time. Nygaard's hidden category is related, but it focuses on making shared resources appear privately owned, not on fairness itself.
- **Inherent & Convenience**
A robot may have several sensors producing independent events at unpredictable times. Nygaard describes this as inherent concurrency because the events happen independently of each other. Goetz's convenience category is similar because handling each sensor as a separate concurrent task can make the program easier to structure, but the motivation is not exactly the same.
- **Resource utilization & Exploitation**:
resource utilization and exploitation are very similar. Both include using multiple CPU cores to perform work in parallel. Nygaard specifically describe splitting independent streams across multiple cores.

The categories are therefore not exactly the same. Goetz focuses more on the benefits of concurrency, while Nygaard focuses more on why concurrency occurs in a system.

## Question 2

**Inherent**

- Chrome — user input and network events happen independently.
- Discord — messages can arrive while I am typing.
- Spotify — plays music while responding to input.

**Exploitation**

- Video games — use multiple CPU cores.
- Chrome — different tasks/tabs can run in parallel.
- Compilation tools — compile several files simultaneously.

**Hidden**

- Windows — many programs share the CPU.
- Virtual machines — several systems share physical hardware.
- Cloud servers — applications share CPU, memory, and storage.

# Exercise 1.5
Operating System: Windows
Number of Cores: 8
Size of main memeory: 32 GB
L1 Cache: 512 KB
L2 Cache: 8 MB
L3 Cache: 96 MB