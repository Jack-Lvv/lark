package com.cqupt.lark.common;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PriorityLock {
    private final Lock lock = new ReentrantLock();
    private final Condition highPriorityCond = lock.newCondition();
    private final Condition lowPriorityCond = lock.newCondition();
    private int highPriorityWaiters = 0;
    private boolean isLocked = false;

    // 高优先级获取锁
    public void lockHighPriority() throws InterruptedException {
        lock.lock();
        highPriorityWaiters++;
        try {
            while (isLocked) {
                highPriorityCond.await(); // 等待
            }
            isLocked = true;
        } finally {
            highPriorityWaiters--;
            lock.unlock();
        }
    }

    // 低优先级获取锁
    public void lockLowPriority() throws InterruptedException {
        lock.lock();
        try {
            while (isLocked || highPriorityWaiters > 0) {
                lowPriorityCond.await(); // 有高优先级等待时低优先级阻塞
            }
            isLocked = true;
        } finally {
            lock.unlock();
        }
    }

    // 释放锁（优先唤醒高优先级线程）
    public void unlock() {
        lock.lock();
        try {
            isLocked = false;
            if (highPriorityWaiters > 0) {
                highPriorityCond.signal(); // 先唤醒高优先级，只唤醒一个线程，防止惊群效应
            } else {
                lowPriorityCond.signal(); // 再唤醒低优先级
            }
        } finally {
            lock.unlock();
        }
    }
}
