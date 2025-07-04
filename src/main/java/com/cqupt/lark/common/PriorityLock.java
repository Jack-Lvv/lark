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
        try {
            highPriorityWaiters++;
            while (isLocked) {
                highPriorityCond.await(); // 等待
            }
            highPriorityWaiters--;
            isLocked = true;
        } finally {
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
                highPriorityCond.signalAll(); // 先唤醒高优先级
            } else {
                lowPriorityCond.signal(); // 再唤醒低优先级
            }
        } finally {
            lock.unlock();
        }
    }
}
