// For week 4
// raup@itu.dk * 18/09/2021
package exercises04;

interface BoundedBufferInteface<T> {
    public T take() throws Exception;
    public void insert(T elem) throws Exception;
}
