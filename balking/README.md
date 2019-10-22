---
layout: pattern
title: Balking
folder: balking
permalink: /patterns/balking/
categories: Concurrency
tags:
 - Java
 - Difficulty-Beginner
---

## Intent
Balking Pattern is used to prevent an object from executing certain code if it is an
incomplete or inappropriate state

**Balking Pattern设计目的：防止对象在不完整（构造中）或不适当的时候执行某些代码**

![alt text](./etc/balking.png "Balking")

## Applicability
Use the Balking pattern when

* you want to invoke an action on an object only when it is in a particular state
* objects are generally only in a state that is prone to balking temporarily
but for an unknown amount of time

阻塞设计模式 使用场景：

* 对象在特定状态才对其调用的操作，实际使用例如：多线程下开关代码的编写。
* 对象通常仅处于易于暂时停止但状态未知的状态，实际使用例如：视频播放的暂停代码编写。




## Related patterns
* Guarded Suspension Pattern
* Double Checked Locking Pattern
