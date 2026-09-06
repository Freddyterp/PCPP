package exercises02;

public class RWMonitor {
    private int activeReaders = 0;
    private boolean activeWriter = false;
    private int writersInQueue = 0;

    public synchronized void enterRead() throws InterruptedException {
        while (activeWriter || writersInQueue > 0) {
            wait();
        }

        activeReaders++;
    }

    public synchronized void exitRead() {
        activeReaders--;

        if (activeReaders == 0) {
            notifyAll();
        }
    }

    public synchronized void enterWrite() throws InterruptedException {
        writersInQueue++;

        try {
            while (activeWriter || activeReaders > 0) {
                wait();
            }
            activeWriter = true;
        } finally {
            writersInQueue--;
        }
    }

    public synchronized void exitWrite() {
        activeWriter = false;
        notifyAll();
    }
}