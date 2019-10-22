package com.iluwatar.balking;

import java.util.concurrent.TimeUnit;

/**
 * 在执行某些工作时模拟延迟的接口
 */
public interface DelayProvider {
  void executeAfterDelay(long interval, TimeUnit timeUnit, Runnable task);
}
