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
package com.iluwatar.doublechecked.locking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * Inventory
 *
 * ReentrantLock 实现双重锁定
 *
 */
public class Inventory {

  private static final Logger LOGGER = LoggerFactory.getLogger(Inventory.class);

  private final int inventorySize;
  private final List<Item> items;
  private final Lock lock;

  /**
   * Constructor
   */
  public Inventory(int inventorySize) {
    this.inventorySize = inventorySize;
    this.items = new ArrayList<>(inventorySize);
    this.lock = new ReentrantLock();
  }

  /**
   * Add item
   * 判断size，满足条件之后，锁定对象，再次判断，满足条件才执行
   * 第二次判断的原因是：
   *  多线程环境可能在第一次size的判断成立到上锁执行完成的时间段，
   *  出现了其他线程执行了改变size的操作，导致未知异常
   */
  public boolean addItem(Item item) {
    if (items.size() < inventorySize) {
      lock.lock();
      try {
        if (items.size() < inventorySize) {
          items.add(item);
          System.out.println(Thread.currentThread()+": items.size()="+items.size()+", inventorySize="+inventorySize);
          LOGGER.debug("{}: items.size()={}, inventorySize={}", Thread.currentThread(), items.size(), inventorySize);
          return true;
        }
      } finally {
        lock.unlock();
      }
    }
    return false;
  }

  /**
   * Get all the items in the inventory
   *
   * @return All the items of the inventory, as an unmodifiable list
   */
  public final List<Item> getItems() {
    /**
     * Collections.unmodifiableList() 1.8 新特性，返回一个不可修改的视图
     */
    return Collections.unmodifiableList(items);
  }

}
