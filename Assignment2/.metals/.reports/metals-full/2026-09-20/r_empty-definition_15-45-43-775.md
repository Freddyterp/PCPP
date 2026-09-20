error id: file:///C:/Users/frede/OneDrive/Skrivebord/Skole/PCPP/Assignment2/Exercise4/week04exercises/app/src/main/java/exercises04/BoundedBuffer.java:_empty_/BoundedBufferInterface#
file:///C:/Users/frede/OneDrive/Skrivebord/Skole/PCPP/Assignment2/Exercise4/week04exercises/app/src/main/java/exercises04/BoundedBuffer.java
empty definition using pc, found symbol in pc: _empty_/BoundedBufferInterface#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 184
uri: file:///C:/Users/frede/OneDrive/Skrivebord/Skole/PCPP/Assignment2/Exercise4/week04exercises/app/src/main/java/exercises04/BoundedBuffer.java
text:
```scala
package exercises04;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class BoundedBuffer<T> implements BoundedBufferInterface@@<T> {
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
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/BoundedBufferInterface#