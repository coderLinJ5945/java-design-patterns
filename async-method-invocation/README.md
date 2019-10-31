---
layout: pattern
title: Async Method Invocation
folder: async-method-invocation
permalink: /patterns/async-method-invocation/
categories: Concurrency
tags:
 - Java
 - Difficulty-Intermediate
 - Functional
 - Reactive
---

## Intent
Asynchronous method invocation is pattern where the calling thread
is not blocked while waiting results of tasks. The pattern provides parallel
processing of multiple independent tasks and retrieving the results via
callbacks or waiting until everything is done. 

### 代码说明
1. AsyncCallback ：异步回调接口返回类，用于处理异步调用的结果返回（理解成异步返回对象接口）
2. AsyncResult ： 异步调用的真正的调用结果（是否完成、结果值等）接口类
3. AsyncExecutor ：异步调用执行器接口（方便扩展，开启/关闭调用任务）
4. ThreadAsyncExecutor ：异步调用执行类，多线程实现多任务异步调用。

![alt text](./etc/async-method-invocation.png "Async Method Invocation")

## Applicability
Use async method invocation pattern when

* you have multiple independent tasks that can run in parallel
* you need to improve the performance of a group of sequential tasks
* you have limited amount of processing capacity or long running tasks and the
  caller should not wait the tasks to be ready

## Real world examples

* [FutureTask](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/FutureTask.html), [CompletableFuture](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html) and [ExecutorService](http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html) (Java)
* [Task-based Asynchronous Pattern](https://msdn.microsoft.com/en-us/library/hh873175.aspx) (.NET)

## 异步方法调用
独立任务并行处理，不会阻塞调用线程，并且通过回调检测调用结果。

### 意图
提高程序处理速度，并发情况下处理更多任务

### 使用场景
1. 您有多个可以并行运行的独立任务，需要并发提高效率完成的场景
2. 需要提高一组顺序任务的性能
3. 处理能力有限或任务长时间运行，并且调用方不应等待任务准备就绪（数据采集）
4. jdk中的的实例：java.util.concurrent.FutureTask 、java.util.concurrent.CompletableFuture 、java.util.concurrent.ExecutorService

## jdk 异步调用方法实例   todo

### java.util.concurrent.FutureTask
**源码解读：**

_源码中没有包含new Thread，使用时需要将FutureTask 最为参数创建线程。
有run()线程执行方法_

a. 状态码和其他属性，用于确认任务提交状态
```
    private volatile int state;
    private static final int NEW          = 0;
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;
    /** Callable 底层方法调用类 */
    private Callable<V> callable;
    /** 要返回的结果或要从get()抛出的异常 */
    private Object outcome; // non-volatile, protected by state reads/writes
    /** 具有可见性的可调用线程对象 */
    private volatile Thread runner;
    /** 等待线程的Treiber堆栈 */
    private volatile WaitNode waiters;    
```
b. 返回结果方法:有结果返回结果对象，没有就抛出已完成任务异常, get()方法调用
```report
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL)
            return (V)x;
        if (s >= CANCELLED)
            throw new CancellationException();
        throw new ExecutionException((Throwable)x);
    }
     // 这里可以根据Callback 异步回调接口进行实现，等待直到任务执行完成 or 超时返回
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)
            s = awaitDone(false, 0L);
        return report(s);
    }    
```
c. 设置返回结果：设置要返回的结果outcome
```set
    protected void set(V v) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {
            outcome = v;
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // final state
            //删除并通知所有等待的线程
            finishCompletion();
        }
    }

    private void finishCompletion() {
        // assert state > COMPLETING;
        for (WaitNode q; (q = waiters) != null;) {
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {
                for (;;) {
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);
                    }
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }
        done();
        callable = null;        // to reduce footprint
    }    
    
```
d. 异步调用任务多线程执行方法run：
```run
    public void run() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)
                    set(result);
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }
```



### java.util.concurrent.CompletableFuture

### java.util.concurrent.ExecutorService
