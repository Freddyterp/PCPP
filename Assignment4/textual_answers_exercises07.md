# Exercise 7.1

## 7.1.1

**Answer:** Yes, the execution is sequentially consistent. A valid execution order is:

```text
<q.enq(x), q.enq(y), q.deq(x)>
```

## 7.1.2

**Answer:** No valid linearization exists, because q.deq(x) finishes before q.enq(x) begins. 

## 7.1.3

**Answer:** Yes, the execution linearizable with the execution order:

```text
<q.enq(x), q.deq(x)>
```

## 7.1.4

**Answer:** This execution is not linearizable because it's happening inside a FIFO queue. We cannot dequeue 'y' without dequeueing 'x' first, when `q.enq(x)` finishes before `q.enq(y)` begins, meaning this order is forced. 


# 7.2

## 7.2.1

**Answer:** for push, the linearization point for when CAS succeeds is: 

```text
top.compareAndSet(oldHead,newHead)
```

For pop, the linearization point for when the stack is empty is:

```text
oldHead      = top.get()
```

Since the operation might return 'null', meaning we learn that the stack is empty. And when CAS succeeds:

```text
top.compareAndSet(oldHead,newHead)
```

These linearization points make the implementation linearizable because each operation can be ordered at an atomic point, which preserves the stack's LIFO semantics.

## 7.2.2

```text
@Test
    void pushTestConcurrent() throws InterruptedException {
        LockFreeStack<Integer> stack = new LockFreeStack<>();

        Thread t1 = new Thread(() -> stack.push(1));
        Thread t2 = new Thread(() -> stack.push(2));

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        Integer n = stack.pop();
        Integer m = stack.pop();

        assertNotNull(n);
        assertNotNull(m);

        assertTrue((n == 1 && m == 2) || (n == 2 && m == 1));

        assertNull(stack.pop());
    }
```

## 7.2.3

```text
    @Test
    void popTestConcurrent() throws InterruptedException {
        LockFreeStack<Integer> stack = new LockFreeStack<>();

        stack.push(1);
        stack.push(2);
        stack.push(3);

        Integer[] res = new Integer[3];

        Thread[] ts = new Thread[3];

        int sum = 0;

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            ts[idx] = new Thread(() -> { res[idx] = stack.pop(); });
        }

        for (int i = 0; i < 3; i++) {
            ts[i].start();
        }

        for (int i = 0; i < 3; i++) {
            ts[i].join();
        }

        for (int i = 0; i < 3; i++) {
            assertNotNull(res[i]);
        }

        for (int i = 0; i < 3; i++) {
            sum += res[i]; 
        }

        assertEquals(6, sum);

        for (int i = 0; i < 3; i++) {
            assertNull(stack.pop());
        }
    }
```

## 7.2.4

**Answer:** Yes, the tests now cover the linearization points in the Treiber Stack. The tests cover the successful CAS operations in both push and pop, as well as the empty stack case where pop returns null. 

# 7.3

**Answer:** 

| Method | Progress Guarantee | Explanation |
|---|---|---|
| `writerTryLock()` | Wait-free | Single CAS attempt returning a bool, no retry loop |
| `writerUnlock()` | Wait-free | Throws an exception or releases the lock, no retries |
| `readerTryLock()` | Lock-free | The 'while' loop means a thread can retry indefinitely, but a failed CAS indicates another thread changed 'holders', so the system makes progress |
| `readerUnlock()` | Lock-free | Like above, the CAS has a retry loop but failed attempts imply other threads have successfully updated the shared state |