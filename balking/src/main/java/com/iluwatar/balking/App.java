/**
 * The MIT License
 * Copyright (c) 2014 Ilkka Seppälä
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.iluwatar.balking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * In Balking Design Pattern if an object’s method is invoked when it is in an inappropriate state,
 * then the method will return without doing anything. Objects that use this pattern are generally only in a
 * state that is prone to balking temporarily but for an unknown amount of time
 *
 * Balking Design Pattern :阻塞设计模式
 *
 *
 * In this example implementation WashingMachine is an object that has two states
 * in which it can be: ENABLED and WASHING. If the machine is ENABLED
 * the state is changed into WASHING that any other thread can't invoke this action on this and then do the job.
 * On the other hand if it have been already washing and any other thread execute wash()
 * it can't do that once again and returns doing nothing.
 *
 * example 说明：
 * WashingMachine 对象，有两种状态：ENABLED 启用 和 WASHING 清洗
 * WASHING 状态： 启动WashingMachine，状态更改为WASHING，其他线程不能对其调用此操作，然后执行此任务。
 *                如果已经 washing ，其他线程执行 wash()方法时，不能在执行了，返回什么也不做。
 */

public class App {

  private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

  /**
   * @param args the command line arguments - not used
   * 理解多线程环境下，对象在特定状态才对其调用的操作
   * 实际编码使用场景：多线程环境下的对象的状态改变，例如开关等
   * 1、初始化 WashingMachine 初始状态为 ENABLED
   * 2、多个线程执行 WashingMachine 的 wash()方法，wash()实际被认为只能被一个线程执行
   * 3、在wash()方法加锁，当只有非 WASHING 状态时执行正常的wash()操作，并且更改WashingMachine 对象为 WASHING
   * 4、此时其他线程执行wash()时，拿到的是 WASHING 状态，不执行任务操作，直接返回
   *
   *
   */
  public static void main(String... args) {
    // 这里构建WashingMachine 对象，通过延时来模拟构建所需要的耗时
    final WashingMachine washingMachine = new WashingMachine();
    System.out.println(washingMachine.getWashingMachineState());
    // 开启线程池，执行 washingMachine 对象的 wash 方法
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    /**
     * 线程池的3个线程遍历执行 wash()方法
     */
    for (int i = 0; i < 3; i++) {
      executorService.execute(washingMachine::wash);
    }
    executorService.shutdown();
    try {
      executorService.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      LOGGER.error("ERROR: Waiting on executor service shutdown!");
    }
  }

}
