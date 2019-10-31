/**
 * The MIT License
 * Copyright (c) 2014-2016 Ilkka Seppälä
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.iluwatar.async.method.invocation;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * Implementation of async executor that creates a new thread for every task.
 * 异步调用任务的执行器，通过多线程来处理多任务
 * 核心类
 */
public class ThreadAsyncExecutor implements AsyncExecutor {

  /**
   * 多线程命名索引，多线程环境需要具有可见性
   */
  private final AtomicInteger idx = new AtomicInteger(0);

  /**
   * 开启异步任务
   * // TODO: 2019/10/31  需要学习 java.util.concurrent.Callable
   * @param task task to be executed asynchronously
   * @param <T>
   * @return
   */
  @Override
  public <T> AsyncResult<T> startProcess(Callable<T> task) {
    return startProcess(task, null);
  }

  /**
   * 开启异步任务，带Callback
   * @param task task to be executed asynchronously
   * @param callback callback to be executed on task completion
   * @param <T>
   * @return
   */
  @Override
  public <T> AsyncResult<T> startProcess(Callable<T> task, AsyncCallback<T> callback) {
    CompletableResult<T> result = new CompletableResult<>(callback);
    new Thread(() -> {
      try {
        // 返回线程调用结果，实际是使用Callable调用返回
        result.setValue(task.call());
      } catch (Exception ex) {
        result.setException(ex);
      }
    } , "executor-" + idx.incrementAndGet()).start();
    return result;
  }

  /**
   * 结束异步任务调用，这里的结束是等待直到正常完成，然后返回异步结果
   * @param asyncResult async result of a task
   * @param <T>
   * @return
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @Override
  public <T> T endProcess(AsyncResult<T> asyncResult) throws ExecutionException, InterruptedException {
    if (!asyncResult.isCompleted()) {
      asyncResult.await();
    }
    return asyncResult.getValue();
  }

  /**
   * 异步任务结果的简单实现类
   * Simple implementation of async result that allows completing it successfully with a value or exceptionally with an
   * exception. A really simplified version from its real life cousins FutureTask and CompletableFuture.
   * @see java.util.concurrent.FutureTask
   * @see java.util.concurrent.CompletableFuture
   */
  private static class CompletableResult<T> implements AsyncResult<T> {

    //定义任务运行的状态值
    static final int RUNNING = 1;
    static final int FAILED = 2;
    static final int COMPLETED = 3;

    // 个人理解，返回结果的对象锁
    final Object lock;
    /**
     * 回调对象
     * java.util.Optional ：工具类，判断返回实例是否为空
     *
     */
    final Optional<AsyncCallback<T>> callback;

    volatile int state = RUNNING;
    T value;
    Exception exception;

    CompletableResult(AsyncCallback<T> callback) {
      this.lock = new Object();
      // TODO: 2019/10/31   ofNullable ：如果非空，则返回一个空的 Optional  待debugger
      this.callback = Optional.ofNullable(callback);
      System.out.println(this.callback);
    }

    /**
     * 设置值的同时，修改返回结果状态为 COMPLETED
     * Sets the value from successful execution and executes callback if available. Notifies any thread waiting for
     * completion.
     *
     * @param value
     *          value of the evaluated task
     */
    void setValue(T value) {
      this.value = value;
      this.state = COMPLETED;
      // 如果存在值，则使用该值调用指定的消费者，否则不执行任何操作(有返回值就设值，没有不做任何操作)
      // lambda 表达式隐藏了 java.util.Objects.Consumer 类的使用
      this.callback.ifPresent((AsyncCallback<T> ac) -> ac.onComplete(value, Optional.<Exception>empty()));
      /**
       *  lambda 表达式理解
        this.callback.ifPresent(new Consumer<AsyncCallback<T>>() {
        @Override
        public void accept(AsyncCallback<T> ac) {
          ac.onComplete(value, Optional.<Exception>empty());
        }
      });*/
      // 同步中释放锁
      synchronized (lock) {
        lock.notifyAll();
      }
    }

    /**
     * 异常时，设置Exception
     * Sets the exception from failed execution and executes callback if available. Notifies any thread waiting for
     * completion.
     *
     * @param exception
     *          exception of the failed task
     */
    void setException(Exception exception) {
      this.exception = exception;
      this.state = FAILED;
      this.callback.ifPresent(ac -> ac.onComplete(null, Optional.of(exception)));
      synchronized (lock) {
        lock.notifyAll();
      }
    }

    @Override
    public boolean isCompleted() {
      return state > RUNNING;
    }

    /**
     * 获取值，这里用到了阻塞模式，只有完成状态的对象才能获取到value值
     * @return
     * @throws ExecutionException
     */
    @Override
    public T getValue() throws ExecutionException {
      if (state == COMPLETED) {
        return value;
      } else if (state == FAILED) {
        throw new ExecutionException(exception);
      } else {
        throw new IllegalStateException("Execution not completed yet");
      }
    }

    /**
     * 释放对象锁，释放资源，线程进入等待状态
     * @throws InterruptedException
     */
    @Override
    public void await() throws InterruptedException {
      synchronized (lock) {
        while (!isCompleted()) {
          lock.wait();
        }
      }
    }
  }
}
