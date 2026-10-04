// For week 6
// raup@itu.dk * 2024-09-22

package exercises06;

// Very likely you will need some imports here

class ReadWriteCASLock implements SimpleRWTryLockInterface {

    // TODO: Add necessary field(s) for the class

    public boolean readerTryLock() {
        // TODO 6.2.3
        return true;
    }

    public void readerUnlock() {
        // TODO 6.2.4
    }

    public boolean writerTryLock() {
        // TODO 6.2.1
        return true;
    }

    public void writerUnlock() {
        // TODO 6.2.2
    }





    private static abstract class Holders { }

    private static class ReaderList extends Holders {
        private final Thread thread;
        private final ReaderList next;

        private ReaderList(Thread thread, ReaderList next) {
            this.thread = thread;
            this.next = next;
        }

        // TODO: contains

        // TODO: remove
    }

    private static class Writer extends Holders {
        public final Thread thread;

        private Writer(Thread thread) {
            this.thread = thread;
        }

    }
}
