// For week 6
// raup@itu.dk * 2026-09-23

package exercises06;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLocks {

    // TODO: 6.2.5  - sequential correctness tests

    @Test
    public void writeLockCanBeTakenAndReleased() {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        assertTrue(lock.writerTryLock());
        lock.writerUnlock();
        assertTrue(lock.writerTryLock(), "lock must be free again after unlock");
        lock.writerUnlock();
    }

    @Test
    public void readLockCanBeTakenAndReleased() {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        assertTrue(lock.readerTryLock());
        lock.readerUnlock();
        assertTrue(lock.writerTryLock(), "no readers left, so a writer may enter");
        lock.writerUnlock();
    }

    /** Not possible to take a read lock while holding a write lock. */
    @Test
    public void cannotTakeReadLockWhileHoldingWriteLock() {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        assertTrue(lock.writerTryLock());
        assertFalse(lock.readerTryLock());
        lock.writerUnlock();
    }

    /** Not possible to take a write lock while holding a read lock. */
    @Test
    public void cannotTakeWriteLockWhileHoldingReadLock() {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        assertTrue(lock.readerTryLock());
        assertFalse(lock.writerTryLock());
        lock.readerUnlock();
    }

    @Test
    public void cannotTakeWriteLockTwice() {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        assertTrue(lock.writerTryLock());
        assertFalse(lock.writerTryLock(), "the lock is not reentrant");
        lock.writerUnlock();
    }

    /** Not possible to write-unlock a lock you do not hold. */
    @Test
    public void cannotWriterUnlockALockYouDoNotHold() {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        assertThrows(IllegalMonitorStateException.class, lock::writerUnlock);

        assertTrue(lock.readerTryLock());
        assertThrows(IllegalMonitorStateException.class, lock::writerUnlock,
                     "holding a read lock is not holding the write lock");
        lock.readerUnlock();
    }

    /** Not possible to read-unlock a lock you do not hold. */
    @Test
    public void cannotReaderUnlockALockYouDoNotHold() {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        assertThrows(IllegalMonitorStateException.class, lock::readerUnlock);

        assertTrue(lock.writerTryLock());
        assertThrows(IllegalMonitorStateException.class, lock::readerUnlock,
                     "holding the write lock is not holding a read lock");
        lock.writerUnlock();
    }

    @Test
    public void readerCannotUnlockOnBehalfOfAnotherReader() throws InterruptedException {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        final Thread other = new Thread(() -> assertTrue(lock.readerTryLock()));
        other.start();
        other.join();
        // The main thread is not on the reader list.
        assertThrows(IllegalMonitorStateException.class, lock::readerUnlock);
    }

    @Test
    public void severalReadersMayHoldTheLockAtTheSameTime() throws InterruptedException {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        assertTrue(lock.readerTryLock());

        final Thread second = new Thread(() -> {
            assertTrue(lock.readerTryLock(), "a second reader must be admitted");
            assertFalse(lock.writerTryLock(), "no writer while readers hold the lock");
            lock.readerUnlock();
        });
        second.start();
        second.join();

        assertFalse(lock.writerTryLock(), "the first reader still holds the lock");
        lock.readerUnlock();
        assertTrue(lock.writerTryLock());
        lock.writerUnlock();
    }

    /** 6.2.7 (challenging): the strict variant rejects a second read lock. */
    @Test
    public void strictReaderTryLockRejectsReentrancy() {
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        assertTrue(lock.readerTryLockNoReentry());
        assertThrows(IllegalMonitorStateException.class, lock::readerTryLockNoReentry);
        lock.readerUnlock();
        assertTrue(lock.writerTryLock(), "the single read lock was really released");
        lock.writerUnlock();
    }


    // TODO: 6.2.6  - parallel correctness test

    /**
     * 6.2.6 - many writer threads repeatedly take and release the write lock.
     * Each thread that gets in bumps a shared counter and records the maximum
     * value seen; if the lock were broken, two writers could be inside at the
     * same time and that maximum would exceed 1. The check is made after all
     * threads have finished.
     */
    @Test
    public void twoWritersNeverHoldTheLockAtTheSameTime() throws InterruptedException {
        final int threadCount = 16, attemptsPerThread = 20_000;
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        final AtomicInteger insideNow    = new AtomicInteger(0);
        final AtomicInteger maxObserved  = new AtomicInteger(0);
        final AtomicInteger acquisitions = new AtomicInteger(0);
        final CountDownLatch startSignal = new CountDownLatch(1);
        final Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    startSignal.await();
                } catch (InterruptedException exn) {
                    Thread.currentThread().interrupt();
                    return;
                }
                for (int k = 0; k < attemptsPerThread; k++) {
                    if (lock.writerTryLock()) {
                        final int now = insideNow.incrementAndGet();
                        maxObserved.accumulateAndGet(now, Math::max);
                        acquisitions.incrementAndGet();
                        insideNow.decrementAndGet();
                        lock.writerUnlock();
                    }
                }
            });
            threads[i].start();
        }

        startSignal.countDown();
        for (Thread t : threads)
            t.join();

        assertTrue(acquisitions.get() > 0, "no thread ever got the lock");
        assertEquals(1, maxObserved.get(),
                     "more than one writer held the lock at the same time");
        assertTrue(lock.writerTryLock(), "the lock must be free at the end");
        lock.writerUnlock();
    }

    /**
     * Extra confidence: with readers and writers mixed, a writer is never
     * inside at the same time as anybody else.
     */
    @Test
    public void readersAndWritersNeverOverlap() throws InterruptedException {
        final int readers = 12, writers = 6, attempts = 20_000;
        final ReadWriteCASLock lock = new ReadWriteCASLock();
        final AtomicInteger readersInside = new AtomicInteger(0);
        final AtomicInteger writersInside = new AtomicInteger(0);
        final AtomicInteger violations    = new AtomicInteger(0);
        final CountDownLatch startSignal  = new CountDownLatch(1);
        final Thread[] threads = new Thread[readers + writers];

        for (int i = 0; i < readers; i++)
            threads[i] = new Thread(() -> {
                try { startSignal.await(); } catch (InterruptedException exn) { return; }
                for (int k = 0; k < attempts; k++)
                    if (lock.readerTryLock()) {
                        readersInside.incrementAndGet();
                        if (writersInside.get() != 0) violations.incrementAndGet();
                        readersInside.decrementAndGet();
                        lock.readerUnlock();
                    }
            });

        for (int i = 0; i < writers; i++)
            threads[readers + i] = new Thread(() -> {
                try { startSignal.await(); } catch (InterruptedException exn) { return; }
                for (int k = 0; k < attempts; k++)
                    if (lock.writerTryLock()) {
                        final int w = writersInside.incrementAndGet();
                        if (w != 1 || readersInside.get() != 0) violations.incrementAndGet();
                        writersInside.decrementAndGet();
                        lock.writerUnlock();
                    }
            });

        for (Thread t : threads) t.start();
        startSignal.countDown();
        for (Thread t : threads) t.join();

        assertEquals(0, violations.get(), "mutual exclusion was violated");
    }

}