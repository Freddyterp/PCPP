// For week 6
// raup@itu.dk * 10/10/2021
package exercises06;

interface SimpleRWTryLockInterface {
    public boolean readerTryLock();
    public void readerUnlock();
    public boolean writerTryLock();
    public void writerUnlock();
}
