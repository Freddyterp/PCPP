// For week 3
// raup@itu.dk * 2025-09-03

package exercises03;

import java.util.List;
import java.util.ArrayList;

public class TestStringSet {

  StringSet s;

  public TestStringSet() throws InterruptedException {
    s = new StringSet();

    Thread t1 = new Thread(() -> {
      s.findOrAdd("PCPP");
    });
    Thread t2 = new Thread(() -> {
      s.find("PCPP");
    });

    t1.start();
    t2.start();
  }



  // Simplification of the class StringNumberer https://github.com/soot-oss/soot/blob/9290783ff21cccbc3a10cea5e97e0bfb352071c1/src/main/java/soot/util/StringNumberer.java#L36
  public class StringSet {

    private final List<String> list = new ArrayList<String>();

    public synchronized int findOrAdd(String s) {
      int ret = list.indexOf(s);
      if (ret == -1) {
        list.add(s);
      }
      return ret;
    }

    public int find(String s) {
      return list.indexOf(s);
    }
  }

  public static void main(String[] args) throws InterruptedException {
    new TestStringSet();
  }
}
