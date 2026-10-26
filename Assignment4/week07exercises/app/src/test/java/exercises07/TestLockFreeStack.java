// raup@itu.dk * 2023-10-20
package exercises07;

// Very likely you will need some imports here
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestLockFreeStack {

    // The imports above are just for convenience, feel free add or remove imports

    // TODO: 7.2.2 - Test push
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
    

    // TODO: 7.2.3 - Test pop
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
}
