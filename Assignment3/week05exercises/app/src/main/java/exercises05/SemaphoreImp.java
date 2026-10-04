// For week 5
// raup@itu.dk * 12/09/2023

package exercises05;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class SemaphoreImp {
    private final int capacity;
    private int state; // safely published because it is initialized to zero (see constructor)
    private Lock lock;
    private Condition condition;

    public SemaphoreImp(int c) {
        capacity  = c;
        state     = 0;
        lock      = new ReentrantLock();
        condition = lock.newCondition();
    }

    public void acquire() throws InterruptedException {
        lock.lock();
        try {
            while(state == capacity) {
                condition.await();
            }
            state++;
        } finally {
            lock.unlock();
        }
    }

    public void release() {
        lock.lock();
        try {
            state--;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
