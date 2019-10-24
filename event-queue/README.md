---
layout: pattern
title: Event Queue
folder: event-queue
permalink: /patterns/event-queue/
categories: Concurrency
tags:
 - Java
 - Difficulty Intermediate
 - Queue
---

## Intent
Event Queue is a good pattern if You have a limited accessibility resource (for example: 
Audio or Database), but You need to handle all the requests that want to use that.
It puts all the requests in a queue and process them asynchronously.
Gives the resource for the event when it is the next in the queue and in same time
removes it from the queue.

![alt text](./etc/model.png "Event Queue")

## Applicability
Use the Event Queue pattern when

* You have a limited accessibility resource and the asynchronous process is acceptable to reach that

## Credits

* [Mihaly Kuprivecz - Event Queue] (http://gameprogrammingpatterns.com/event-queue.html)

## 事件队列设计模式  todo 待详细学习

### 意图
访问有限的资源（音频，数据库等），需要处理所有的资源访问请求，使用事件队列模式合理。

将所有请求放入队列中，异步处理队列请求，处理完请求移除队列。

简单理解，将发送消息或事件和处理消息事件进行解耦操作
### 使用场景
1. 需要排队处理事件的所有场景。（简单理解类似于光谷地铁口，新加的排队通道）

### 事件队列详细参考
http://gameprogrammingpatterns.com/event-queue.html
