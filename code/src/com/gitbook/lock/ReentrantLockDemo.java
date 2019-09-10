package com.gitbook.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author liuwei56
 * @version 2019/8/22 10:55 AM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class ReentrantLockDemo {

    public static void main(String[] args) throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();
        for (int i = 0; i < 3; i++) {
            lock.lock();
            System.out.println("=====");
        }

        for (int i = 0; i < 3; i++) {
            lock.unlock();
        }
    }
}
