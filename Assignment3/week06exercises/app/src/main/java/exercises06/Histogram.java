// For week 6
// raup@itu.dk * 10/10/2021
package exercises06;

interface Histogram {
    public void increment(int bin);
    public int getCount(int bin);
    public int getSpan();
    public int getAndClear(int bin);
}
