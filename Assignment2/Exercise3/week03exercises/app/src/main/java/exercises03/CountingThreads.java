// For week 3 
// raup@itu.dk * 2025-09-03

package exercises03;

public class CountingThreads {
  int count;
  Lock l = new ReentractLock();

  public CountingThreads() throws InterruptedException {
    count = 0;

    CountingThread t1 = new CountingThread();
    CountingThread t2 = new CountingThread();

    t1.start();
    t2.start();

    t1.join();
    t2.join();

    System.out.println("count="+count);
  }

  public class CountingThread extends Thread {
    public void run() {
      l.lock();
      int temp = count;
      count = temp + 1;
      l.unlock(); 
    }
  }


  public static void main(String[] args) throws InterruptedException {
    new CountingThreads();
  }
}
