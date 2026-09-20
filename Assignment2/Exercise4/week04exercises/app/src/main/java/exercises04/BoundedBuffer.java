package exercises04;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class BoundedBuffer<T> implements BoundedBufferInteface<T> {
    private final Queue<T> buffer;
    private final Semaphore empty;
    private final Semaphore full;
    private final Semaphore mutex;

    public BoundedBuffer(int size) {
        buffer = new LinkedList<>();

        empty = new Semaphore(size);
        full = new Semaphore(0);
        mutex = new Semaphore(1);
    }

    @Override
    public void insert(T elem) throws InterruptedException {
        empty.acquire();
        mutex.acquire();

        buffer.add(elem);

        mutex.release();
        full.release();
    }

    @Override
    public T take() throws InterruptedException {
        full.acquire();
        mutex.acquire();

        T elem = buffer.remove();

        mutex.release();
        empty.release();

        return elem;
    }
}