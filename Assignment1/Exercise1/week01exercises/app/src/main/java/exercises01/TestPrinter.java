package exercises01;

import java.util.concurrent.locks.ReentrantLock;

public class TestPrinter {
    Printer printer = new Printer();

    public TestPrinter() {
       Thread t1 = new Thread(() -> {
            while (true) {
                printer.print();
            }
       });
       Thread t2 = new Thread(() -> {
            while (true) {
                printer.print();
            }
       });
       t1.start(); t2.start();
    
    }

    public static void main(String[] args) {
        new TestPrinter();
    }
}

class Printer {
    private ReentrantLock lock = new ReentrantLock();

    public void print() {
        lock.lock();
        try {
            System.out.print("-");
            try {
                Thread.sleep(50);
            } catch (InterruptedException exn) {
            }
            System.out.print("|");
        } finally {
            lock.unlock();
        }
    }
}