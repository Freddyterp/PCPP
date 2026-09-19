# Question 3.1

## Question 3.1.1

```java
public class CountingThread extends Thread {
    public void run() {
        int temp = count; //(1)
        count = temp + 1; //(2)
    }
}
```

Thread naming:
- m = main thread
- t1 = first CountingThread
- t2 = second CountingThread


| Action           | Meaning                       | Classification          |
| ---------------- | ----------------------------- | ----------------------- |
| `m(init(count))` | `count = 0`                   | variable access — write |
| `m(create(t1))` | `CountingThread t1 = new CountingThread()`                   | Other |
| `m(create(t2))` | `CountingThread t1 = new CountingThread()`                   | Other |
| `m(start(t1))`   | `t1.start()`                  | synchronization         |
| `m(start(t2))`   | `t2.start()`                  | synchronization         |
| `m(join(t1))`    | `t1.join()`                   | synchronization         |
| `m(join(t2))`    | `t2.join()`                   | synchronization         |
| `m(print)`       | reading `count` for `println` | variable access — read  |
| `t1(1)`          | `temp = count`                | variable access — read  |
| `t1(2)`          | `count = temp + 1`            | variable access — write |
| `t2(1)`          | `temp = count`                | variable access — read  |
| `t2(2)`          | `count = temp + 1`            | variable access — write |


## Question 3.1.2


For the main thread:
```math
HB_{po}^{m} = \{ m(init(count)) \to m(start(t1)) \to m(start(t2)) \to m(join(t1)) \to m(join(t2)) \to m(print) \}
```

For the t1 thread:
```math
HB_{po}^{t1} = \{ t1(1) \to t1(2) \}
```
For the T2 thread:
```math
HB_{po}^{t2} = \{ t2(1) \to t2(2) \}
```
Yes. The program order is the same in every execution because it only depends on the order of actions inside each thread. The threads may run at different times, but the order of the actions within each thread does not change.

## Question 3.1.3

```math
HB_{start} = \{m(start(t1)) \to t1(1), m(start(t2)) \to t2(1)\}
```

```math
HB_{term} = \{t1(2) \to m(join(t1)), t2(2) \to m(join(t2))\}
```

Yes, these happens-before relations are the same for every execution because each start() always happens-before the first action of its thread, and the last action of each thread always happens-before its corresponding join().

## Question 3.1.4
There are four synchronization actions: m(start(t1)), m(start(t2)), m(join(t1)), and m(join(t2)). Since all four are executed by the main thread, their order is fixed by program order. Therefore, there is only one synchronization order:
```math
SO = \{\langle m(start(t1)), m(start(t2)), m(join(t1)), m(join(t2))\rangle\}
```

## Question 3.1.5
The program has a data race because t1 and t2 both access the shared variable count, and at least one of the accesses is a write. For example, t1(2) writes to count and t2(1) reads count. These two actions are not ordered by happens-before, since there is no edge $ t1(2) \to t2(1) $ or $ t2(1) \to t1(2) $ because of this the program contains a data race.

## Question 3.1.6

```java
public class CountingThread extends Thread {
    public void run() {
        l.lock() // (1)
        int temp = count; //(2)
        count = temp + 1; //(3)
        l.unlock() //(4)
    }
}
```

$HB_{po}^{t1} = \{t1(1) \to t1(2) \to t1(3) \to t1(4)\}$

$HB_{po}^{t2} = \{t2(1) \to t2(2) \to t2(3) \to t2(4)\}$


The program order changes because each thread now has a lock() action before accessing count and an unlock() action afterwards.The lock also introduces new happens-before relations through the monitor rule. If t1 acquires the lock first: $ t1(4) \to t2(1) $ If t2 acquires the lock first: $ t2(4) \to t1(1) $ as a result of this, there are now two different happens-before order sets, depending on which thread acquires the lock first.

## Question 3.1.7

The updated program does not contain data races because all accesses to count are protected by the same lock.
If t1 gets the lock first, then: $$ t1(4) \to t2(1) $$ and by program order: $$ t1(2) \to t1(3) \to t1(4) \to t2(1) \to t2(2) \to t2(3) $$ Therefore, the accesses to count in t1 happen-before the accesses to count in t2. If t2 gets the lock first, the opposite ordering holds: $$ t2(4) \to t1(1) $$ so the accesses in t2 happen-before the accesses in t1. In both cases, the conflicting accesses are ordered by happens-before. Therefore, there are no data races.

## Question 3.1.8
If count is declared volatile, all accesses to count are volatile accesses. According to the Java memory model, accesses to volatile variables are not considered conflicting. Therefore, there cannot be a data race on count, so all executions are data-race free