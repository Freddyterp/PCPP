// For week 6
// raup@itu.dk * 2024-09-22

package exercises06;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple, non-reentrant, non-blocking read-write lock built from a single
 * AtomicReference and compareAndSet.
 *
 * holders == null                - the lock is free
 * holders instanceof ReaderList  - held for reading by the threads in the list
 * holders instanceof Writer      - held for writing by one thread
 */
class ReadWriteCASLock implements SimpleRWTryLockInterface {

    private final AtomicReference<Holders> holders = new AtomicReference<Holders>(null);

    /**
     * 6.2.3 - succeeds if the lock is free or held only by readers. Other
     * threads may be adding or removing themselves concurrently, so we retry
     * until a CAS against an unchanged snapshot succeeds.
     */
    public boolean readerTryLock() {
        final Thread current = Thread.currentThread();
        while (true) {
            final Holders h = holders.get();
            if (h instanceof Writer)
                return false;
            final ReaderList old = (ReaderList) h; // null, or a reader list
            if (holders.compareAndSet(h, new ReaderList(current, old)))
                return true;
        }
    }

    /**
     * 6.2.4 - removes the calling thread from the reader list, or throws if
     * the lock is free, write-held, or the calling thread is not a reader.
     */
    public void readerUnlock() {
        final Thread current = Thread.currentThread();
        while (true) {
            final Holders h = holders.get();
            if (!(h instanceof ReaderList))
                throw new IllegalMonitorStateException(
                        "Calling thread does not hold a read lock");
            final ReaderList list = (ReaderList) h;
            if (!list.contains(current))
                throw new IllegalMonitorStateException(
                        "Calling thread is not on the reader list");
            if (holders.compareAndSet(h, list.remove(current)))
                return;
        }
    }

    /**
     * 6.2.1 - succeeds only if the lock is completely unheld. The single CAS
     * from null performs both the check and the update atomically.
     */
    public boolean writerTryLock() {
        final Thread current = Thread.currentThread();
        return holders.compareAndSet(null, new Writer(current));
    }

    /**
     * 6.2.2 - releases the write lock, or throws if the calling thread does
     * not hold it. A plain set is enough: while a Writer is installed, no
     * other thread can change holders (writerTryLock only CASes from null,
     * readerTryLock refuses to touch a Writer, readerUnlock throws), so the
     * calling thread is the only possible writer of the field.
     */
    public void writerUnlock() {
        final Thread current = Thread.currentThread();
        final Holders h = holders.get();
        if (!(h instanceof Writer) || ((Writer) h).thread != current)
            throw new IllegalMonitorStateException(
                    "Calling thread does not hold the write lock");
        holders.set(null);
    }

    /**
     * 6.2.7 (challenging) - as readerTryLock, but throws if the calling
     * thread already holds a read lock.
     *
     * The test-then-set sequence is not atomic, yet it is still correct:
     * only the calling thread can ever add or remove itself from the reader
     * list, so no other thread can invalidate the answer to contains(current);
     * and the CAS commits only if holders still refers to the very snapshot
     * that was inspected, so if another thread changed the list in between the
     * CAS fails and the check is redone on the fresh snapshot.
     */
    public boolean readerTryLockNoReentry() {
        final Thread current = Thread.currentThread();
        while (true) {
            final Holders h = holders.get();
            if (h instanceof Writer)
                return false;
            final ReaderList old = (ReaderList) h;
            if (old != null && old.contains(current))
                throw new IllegalMonitorStateException(
                        "Calling thread already holds a read lock");
            if (holders.compareAndSet(h, new ReaderList(current, old)))
                return true;
        }
    }


    private static abstract class Holders { }

    /** Immutable linked list of the threads currently holding a read lock. */
    private static class ReaderList extends Holders {
        private final Thread thread;
        private final ReaderList next;

        public ReaderList(Thread thread, ReaderList next) {
            this.thread = thread;
            this.next = next;
        }

        public boolean contains(Thread t) {
            for (ReaderList node = this; node != null; node = node.next)
                if (node.thread == t)
                    return true;
            return false;
        }

        /**
         * A new list with the first occurrence of t removed, or null if the
         * result would be empty. The original list is never modified.
         */
        public ReaderList remove(Thread t) {
            if (thread == t)
                return next;
            if (next == null)
                return this;              // t not present: unchanged
            final ReaderList rest = next.remove(t);
            if (rest == next)
                return this;              // nothing changed in the tail
            return new ReaderList(thread, rest);
        }
    }

    /** The single thread currently holding the write lock. */
    private static class Writer extends Holders {
        public final Thread thread;

        public Writer(Thread thread) {
            this.thread = thread;
        }
    }
}