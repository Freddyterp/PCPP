# Question 8

## Question 8.1
### Question 8.1.1
pow                                   8.8 ns       0.16   33554432
exp                                   4.9 ns       0.06   67108864
log                                   5.8 ns       0.55   67108864
sin                                   6.6 ns       0.07   67108864
cos                                   7.2 ns       0.10   67108864
tan                                   9.1 ns       0.14   33554432
asin                                  5.7 ns       0.45   67108864
acos                                  5.4 ns       0.08   67108864
atan                                  6.3 ns       0.89   67108864

pow                                   8.6 ns       0.09   33554432
exp                                   4.0 ns       0.07   67108864
log                                   5.5 ns       0.07   67108864
sin                                   7.3 ns       0.15   67108864
cos                                   7.0 ns       0.08   67108864
tan                                   9.1 ns       0.15   33554432
asin                                  6.0 ns       0.05   67108864
acos                                  5.2 ns       0.05   67108864
atan                                  5.6 ns       0.14   67108864

The results seem plausible, with execution times between 4 and 9 nanoseconds. Most measurements were consistent between the two runs, especially pow and tan. However, some functions like exp and atan showed larger differences

### Question 8.1.2
sqrt                                  2.5 ns       0.04  134217728
random                                6.2 ns       0.07   67108864

The results seem plausible. Math.sqrt() is faster because square root is supported directly by the CPU, while Math.random() takes longer because it involves generating pseudorandom numbers

### Question 8.1.3
sqrt                                  2.5 ns       0.03  134217728
random                                6.2 ns       0.05   67108864
sqrt(random)                          7.0 ns       0.05   67108864

The result is not consistent with each other and this could be explained by JIT optimizations, CPU instruction overlap, or differences in how the operations are executed.

## Question 8.2
### Question 8.2.1
The early measurements show large variations, likely due to JIT compilation, JVM warm-up, and system overhead. As the iteration count increases, the results become more stable. Virtual threads are significantly faster to create, start, and join than platform threads.

### Question 8.2.2
hashCode()                            2.4 ns       0.03  134217728
Point creation                       13.6 ns       0.15   33554432
Thread's work                      1468.4 ns      13.38     262144
Thread pltf cre                      38.8 ns       0.32    8388608
Thread pltf cre strt             325069.8 ns  157756.63       4096
Thread pltf cre strt join         92471.2 ns    3122.51       4096
Thread virt cre                      15.4 ns       0.22   16777216
Thread virt cre strt join          7216.1 ns     194.96      65536
Uncontended lock                      5.3 ns       0.09   67108864

The results seem plausible, as virtual threads are significantly cheaper than platform threads.
Creating, starting, and joining a platform thread takes 92,471.2 ns, compared to 7,216.1 ns for a virtual thread.
Compared to the lecture, platform threads are noticeably slower (92,471 ns vs. 30,400 ns), while virtual threads are almost identical (7,216 ns vs. 7,100 ns).

### Question 8.2.3
The estimated overhead for creating, starting, and joining a platform thread is 91,002.8 ns, compared to 5,747.7 ns for a virtual thread.
Virtual threads therefore have approximately 16 times lower overhead. This is plausible because virtual threads are managed by the JVM and require fewer operating-system resources.

## Question 8.3
### Question 8.3.1
Quicksort took 4,976.5 µs to sort 100,000 integers, with a standard deviation of 45.03 µs. The result seems plausible
### Question 8.3.2
Java's Arrays.sort() took 3,837.0 µs, compared to 4,970.5 µs for our quicksort implementation. This is plausible because Java's built-in sorting algorithm is highly optimized. It is approximately 23% faster than our quicksort.
### Question 8.3.3
Arrays.parallelSort() took 583.9 µs, making it approximately 6.8 times faster than Arrays.sort()
### Question 8.3.4
Parallel quicksort took 5,110.6 µs, slightly slower than sequential quicksort at 5,036.5 µs. This is plausible because creating virtual threads for every recursive call adds overhead
### Question 8.3.5
The optimized parallel quicksort took 967.8 µs, compared to 5,110.6 µs previously. This is a significant improvement because virtual threads are only created for larger partitions, reducing thread creation overhead

## Question 8.4
### Question 8.4.2
The non-volatile counter took 215.6 µs, while the volatile counter took 1,451.4 µs for 1,000,000 updates.
The volatile version is approximately 6.7 times slower. This is plausible because volatile requires stronger memory visibility guarantees