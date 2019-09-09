package com.gitbook.algorithm.stackandqueue;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author liuwei
 * @version 2019/9/9 4:21 PM
 * @description 功能描述
 * [225. Implement Stack using Queues (Easy)](https://leetcode.com/problems/implement-stack-using-queues/description/)
 * @see
 * @since 1.0
 */
public class MyQueue {

    public static void main(String[] args) {
        MyQueue myQueue = new MyQueue();
        myQueue.push(1);
        myQueue.push(2);
        myQueue.push(3);
        while (!myQueue.isEmpty()) {
            System.out.println(myQueue.pop());
        }
    }

    private Queue<Integer> queue;

    public MyQueue() {
        queue = new LinkedList<>();
    }

    public void push(int n) {
        queue.add(n);
        int count = queue.size();
        while (count-- > 1) {
            queue.add(queue.poll());
        }
    }

    public int pop() {
        return queue.remove();
    }

    public int peek() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}








