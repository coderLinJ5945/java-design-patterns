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

package com.iluwatar.event.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * 音频文件类，该类实现 Event Queue 事件队列模式
 * @author mkuprivecz
 *
 */
public class Audio {
  private static final Logger LOGGER = LoggerFactory.getLogger(Audio.class);

  //静态常量，实现单例模式
  private static final Audio INSTANCE = new Audio();

  //最大等待个数
  private static final int MAX_PENDING = 16;

  //第一个索引
  private int headIndex;

  //正在播放的索引
  private int tailIndex;

  // 具有可见性线程对象，用于播放文件线程
  private volatile Thread updateThread = null;

  // 播放内容的事件队列， 这里简单理解成数组实现队列
  private PlayMessage[] pendingAudio = new PlayMessage[MAX_PENDING];

  // Visible only for testing purposes
  Audio() {

  }
  /* 单例实现*/
  public static Audio getInstance() {
    return INSTANCE;
  }

  /**
   * 用于停止更新方法的线程，并等待服务停止
   */
  public synchronized void stopService() throws InterruptedException {
    if (updateThread != null) {
      // 通知线程中断，设置中断标志位，这里会等待线程执行完成中断
      updateThread.interrupt();
    }
    //等待线程死亡，之后清空该线程对象
    updateThread.join();
    updateThread = null;
  }
  
  /**
   * 检测更新方法线程 是否是 started 状态
   * @return boolean
   */
  public synchronized boolean isServiceRunning() {
    return updateThread != null && updateThread.isAlive();
  }

  /**
   * Starts the thread for the Update Method pattern if it was not started previously.
   * Also when the thread is is ready initializes the indexes of the queue
   * 如果之前没有启动更新方法模式，则启动该模式的线程
   * 另外，线程初始化队列的索引
   */
  public void init() {
    //判断更新线程是否为null，为null就初始化更新线程
    if (updateThread == null) {
      updateThread = new Thread(() -> {
        while (!Thread.currentThread().isInterrupted()) {
          //用于从队列中获取 audio 并执行播放
          update();
        }
      });
    }
    //同步线程启动器，用于启动 updateThread
    startThread();
  }
  
  /**
   * This is a synchronized thread starter
   * updateThread 同步线程启动器
   */
  private synchronized void startThread() {
    if (!updateThread.isAlive()) {
      updateThread.start();
      headIndex = 0;
      tailIndex = 0;
    }
  }

  /**
   * 播放方法，事件队列的核心使用
   * 用于添加 audio 到 队列中并进行播放
   * This method adds a new audio into the queue.
   * @param stream is the AudioInputStream for the method
   * @param volume is the level of the audio's volume 
   */
  public void playSound(AudioInputStream stream, float volume) {
    //初始化 updateThread 线程
    init();
    // 遍历挂起请求事件，这里就是播放事件
    for (int i = headIndex; i != tailIndex; i = (i + 1) % MAX_PENDING) {
      if (getPendingAudio()[i].getStream() == stream) {
        // Use the larger of the two volumes. ？
        getPendingAudio()[i].setVolume(Math.max(volume, getPendingAudio()[i].getVolume()));

        // Don't need to enqueue.
        return;
      }
    }
    getPendingAudio()[tailIndex] = new PlayMessage(stream, volume);
    tailIndex = (tailIndex + 1) % MAX_PENDING;
  }
  
  /**
   * 从queue队列中获取音频并播放
   * This method uses the Update Method pattern.
   * It takes the audio from the queue and plays it
   */
  private void update() {
    // If there are no pending requests, do nothing.
    if (headIndex == tailIndex) {
      return;
    }
    Clip clip = null;
    try {
      AudioInputStream audioStream = getPendingAudio()[headIndex].getStream();
      headIndex++;
      clip = AudioSystem.getClip();
      clip.open(audioStream);
      clip.start();
    } catch (LineUnavailableException e) {
      LOGGER.trace("Error occoured while loading the audio: The line is unavailable", e);
    } catch (IOException e) {
      LOGGER.trace("Input/Output error while loading the audio", e);
    } catch (IllegalArgumentException e) {
      LOGGER.trace("The system doesn't support the sound: " + e.getMessage(), e);
    }
  }

  /**
   * Returns the AudioInputStream of a file
   * @param filePath is the path of the audio file
   * @return AudioInputStream
   * @throws UnsupportedAudioFileException when the audio file is not supported 
   * @throws IOException when the file is not readable
   */
  public AudioInputStream getAudioStream(String filePath)
      throws UnsupportedAudioFileException, IOException {
    return AudioSystem.getAudioInputStream(new File(filePath).getAbsoluteFile());
  }

  /**
   * 返回队列的消息数组
   * Returns with the message array of the queue 
   * @return PlayMessage[]
   */
  public PlayMessage[] getPendingAudio() {
    return pendingAudio;
  }

}
