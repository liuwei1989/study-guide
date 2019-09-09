package com.gitbook.algorithm.stackandqueue;

import java.util.Stack;

/**
 * @author liuwei
 * @version 2019/9/9 5:14 PM
 * @description 功能描述
 * [155. Min Stack (Easy)](https://leetcode.com/problems/min-stack/description/)
 * @see
 * @since 1.0
 */
public class MinStack {

    public static void main(String[] args) {
        MinStack minStack = new MinStack();
        minStack.push(1);
        minStack.push(2);
        minStack.push(8);
        minStack.push(3);
        minStack.push(4);
        System.out.println(minStack.getTop());
        System.out.println(minStack.getMin());
//        minStack.pop();
//        System.out.println(minStack.getTop());
//        System.out.println(minStack.getMin());
    }

    private Stack<Integer> dataStack;
    private Stack<Integer> minStack;
    private Integer min;

    public MinStack() {
        dataStack = new Stack<>();
        minStack = new Stack<>();
        min = Integer.MAX_VALUE;
    }

    public void push(int x) {
        dataStack.push(x);
        min = Math.min(x, min);
        minStack.push(min);
    }

    public void pop() {
        dataStack.pop();
        minStack.pop();
        min = minStack.isEmpty() ? Integer.MAX_VALUE : minStack.peek();
    }

    public int getTop() {
        return dataStack.peek();
    }

    public int getMin() {
        return minStack.peek();
    }
}
