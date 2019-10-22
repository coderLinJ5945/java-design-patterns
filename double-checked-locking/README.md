---
layout: pattern
title: Double Checked Locking
folder: double-checked-locking
permalink: /patterns/double-checked-locking/
categories: Concurrency
tags:
 - Java
 - Difficulty-Beginner
 - Idiom
---

## Intent
Reduce the overhead of acquiring a lock by first testing the
locking criterion (the "lock hint") without actually acquiring the lock. Only
if the locking criterion check indicates that locking is required does the
actual locking logic proceed.

![alt text](./etc/double_checked_locking_1.png "Double Checked Locking")

## Applicability
Use the Double Checked Locking pattern when

* there is a concurrent access in object creation, e.g. singleton, where you want to create single instance of the same class and checking if it's null or not maybe not be enough when there are two or more threads that checks if instance is null or not.
* there is a concurrent access on a method where method's behaviour changes according to the some constraints and these constraint change within this method.

## 双重检查锁定模式

### 意图
测试锁定标准（被锁定的提示），**减少获取锁的开销！！！**
仅当锁定标准（被锁定的提示）ok时，执行实际的锁定逻辑。

### 使用场景
1. 多线程并发创建对象时，singleton 单例的实现，并在有两个或多个检查实例是否为null的线程时检查它是否为null。
2. 在方法上存在并发访问，其中方法的行为根据一些约束而变化，并且这些约束在该方法内变化（简单理解，方法约束的改变由该方法自己改变时）
   例如: 并发往固定大小的集合对象中添加元素

