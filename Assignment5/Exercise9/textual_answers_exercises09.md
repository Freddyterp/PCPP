# Exercise 9

## Exercise 9.1


### Exercise 9.1.1 — Platform Threads Benchmark Results

**OS:   Windows 11; 10.0; amd64**
**JVM:  Oracle Corporation; 25**
**CPU:  AMD64 Family 25 Model 97 Stepping 2, AuthenticAMD; 16 "cores"**

| Threads | Mean Time (µs) | Std. Dev. (µs) | Repetitions |
|---:|---:|---:|---:|
| 1 | 84,569.2 | 377.44 | 4 |
| 2 | 53,146.0 | 1,069.50 | 8 |
| 3 | 38,403.3 | 1,228.68 | 8 |
| 4 | 30,265.1 | 626.03 | 16 |
| 5 | 29,087.4 | 562.28 | 16 |
| 6 | 25,769.8 | 581.09 | 16 |
| 7 | 22,988.3 | 496.57 | 16 |
| 8 | 21,964.7 | 820.18 | 16 |
| 16 | 14,424.5 | 178.36 | 32 |
| 32 | 13,489.3 | 160.02 | 32 |
| **64** | **12,750.6** | **122.29** | **32** |
| 128 | 12,956.8 | 134.67 | 32 |
| 256 | 15,283.1 | 265.29 | 32 |
| 512 | 25,088.9 | 158.09 | 16 |
| 1024 | 49,838.3 | 444.72 | 8 |
| 2048 | 101,047.3 | 2,080.97 | 4 |


### Exercise 9.1.2 — Virtual Threads Benchmark Results

| Threads | Mean Time (µs) | Std. Dev. (µs) | Repetitions |
|---:|---:|---:|---:|
| 1 | 83,716.1 | 603.59 | 4 |
| 2 | 52,947.8 | 466.75 | 8 |
| 3 | 36,861.9 | 296.39 | 8 |
| 4 | 28,919.3 | 493.84 | 16 |
| 5 | 23,349.6 | 183.97 | 16 |
| 6 | 20,224.4 | 460.77 | 16 |
| 7 | 19,006.3 | 833.25 | 16 |
| 8 | 18,332.4 | 557.46 | 16 |
| 16 | 13,632.0 | 254.57 | 32 |
| 32 | 12,160.2 | 118.48 | 32 |
| 64 | 11,467.5 | 43.33 | 32 |
| 128 | 11,030.8 | 81.33 | 32 |
| 256 | 10,865.9 | 137.33 | 32 |
| 512 | 10,654.5 | 79.36 | 32 |
| 1024 | 10,650.2 | 90.23 | 32 |
| **2048** | **10,634.8** | **84.72** | **32** |


### Exercise 9.1.3 — Executor Tasks Benchmark Results

| Tasks | Mean Time (µs) | Std. Dev. (µs) | Repetitions |
|---:|---:|---:|---:|
| 1 | 83,417.1 | 827.06 | 4 |
| 2 | 52,323.2 | 381.18 | 8 |
| 3 | 36,941.8 | 496.48 | 8 |
| 4 | 28,965.4 | 326.46 | 16 |
| 5 | 23,721.9 | 560.42 | 16 |
| 6 | 20,733.5 | 895.78 | 16 |
| 7 | 18,988.5 | 823.33 | 16 |
| 8 | 22,601.5 | 557.75 | 16 |
| 16 | 13,819.2 | 158.76 | 32 |
| 32 | 12,722.0 | 360.25 | 32 |
| 64 | 11,652.8 | 134.25 | 32 |
| 128 | 11,089.2 | 53.35 | 32 |
| 256 | 10,905.7 | 98.66 | 32 |
| **512** | **10,764.8** | **70.94** | **32** |
| 1024 | 10,838.8 | 41.15 | 32 |
| 2048 | 10,790.1 | 138.02 | 32 |

### Exercise 9.1.4

Yes the result seems plausible, They all became faster as the number of threads increase to a certain point.
The platform threads reach the best result at 64 threads and became slower as the thread count increases as the overhead of creating and managing the threads became higher
than the gain of the number of threads. There is a small different between virtual threads and executor tasks which can be explained by measurement variation.

## Exercise 9.2

### Exercise 9.2.1
| Implementation | Execution Time |
|---|---:|
| SynchronizedHistogram | 7933.5 ms |

### Exercise 9.2.2
| Implementation | Execution Time |
|---|---:|
| SynchronizedHistogram | 7933.5 ms |
| CasHistogram | 7871.3 ms |

the 2 times are almost identical and this is because both version have a synchronization overhead, since SynchronizedHistogram is using single lock and the CasHistogram is using CAS operations where the operations might require retries

### Exercise 9.2.3
| Implementation | Execution Time |
|---|---:|
| SynchronizedHistogram | 7952.4 ms |
| CasHistogram | 7706.9 ms |
| AtomicHistogram | 7614.5 ms |

AtomicHistogram is slightly faster than the others

### Exercise 9.2.4

| Implementation | Execution Time |
|---|---:|
| SynchronizedHistogram | 8574.0 ms |
| CasHistogram | 8595.7 ms |
| AtomicHistogram | 8333.4 ms |
| StripedHistogram | 8072.0 ms |

StripedHistogram was the fastest implementation
This is likely because of the stripes distribute updates across multiple atomic arrays, reducing contention between threads updating the same bin.

### Exercise 9.2.5
StripedHistogram most closely resembles LA7 LongAdderArray from Chapter 9.

Both implementations use atomic arrays and distribute updates across multiple counters based on the thread ID. This reduces contention between threads. They also calculate the total by summing the values from all counters.

The main difference is that StripedHistogram uses 16 AtomicIntegerArray objects, while LongAdderArray uses a single AtomicLongArray.

### Exercise 9.2.6

| Implementation | Execution Time |
|---|---:|
| SynchronizedHistogram | 8158.0 ms |
| CasHistogram | 8100.2 ms |
| AtomicHistogram | 8071.5 ms |
| StripedHistogram | 8026.2 ms |
| LongAdderHistogram | 8124.2 ms |

One of the drawbacks of LongAdderHistogram is its higher memory usage.
Additionally, sum() and sumThenReset() are not atomic when updates happen concurrently, which can lead to inconsistent results compared to the previous implementations.

## Exercise 9.3

### Exercise 9.3.1
| Implementation | Mean Time (µs) |
|---|---:|
| SyncLongAdder | 466299.4 |
| SyncLongArrayAdder | 114811.9 |
| SyncLongArrayAdderPadded | 11381.7 |
| WrappedAtomicLong | 173018.6 |
| MyAtomicLong | 457931.5 |
| ThreadLocalAtomicLong | 7095.7 |
| LongAdderArray | 42206.4 |
| LongAdderArrayPadded | 5935.2 |
| WrappedLongAdder | 5712.9 |

The results seem plausible. SyncLongAdder and MyAtomicLong were the slowest because of lock contention and CAS retries.
The computer has 16 available logical processors, while the benchmark used 20 threads. This means not all threads could execute simultaneously, making efficient synchronization important for performance.

### Exercise 9.3.2
SyncLongArrayAdderPadded is faster because it reduces false sharing. In SyncLongArrayAdder, multiple counters are stored close together and may share the same CPU cache line. When different threads update these counters, it causes unnecessary cache invalidations.

SyncLongArrayAdderPadded adds unused elements between counters, placing them on separate cache lines and reducing cache-coherence overhead.

### Exercise 9.3.3
| Implementation | Mean Time (µs) |
|---|---:|
| SyncLongAdder | 486488.1 |
| SyncLongArrayAdder | 102425.5 |
| SyncLongArrayAdderPadded | 12485.4 |
| SyncLongArrayAdderLessPad | 54393.4 |

The LessPad version was significantly slower than the padded version
This could be caused by false sharing between lock objects. In the original padded implementation, the unused lock objects may help separate the active locks in memory.

In LessPad, fewer objects are created, so the active locks may be stored closer together and share cache lines. This can increase cache-coherence overhead and reduce performance.


## Exercise 9.4
I ran the memory latency benchmark on my AMD Ryzen 7 7800X3D, which has 512 KB of total L1 cache.

The measurements suggest an L1 data cache of approximately 32 KB per core, since memory access latency increases significantly beyond this size.

The 512 KB total L1 cache consists of 256 KB data cache and 256 KB instruction cache across the CPU's 8 cores.
The RAM access latency is approximately 84 ns, based on the largest memory size tested (380 MB).