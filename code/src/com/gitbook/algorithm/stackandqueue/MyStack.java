package com.gitbook.algorithm.stackandqueue;

import java.util.Stack;

/**
 * @author liuwei
 * @version 2019/9/9 4:10 PM
 * @description 功能描述
 * [232. Implement Queue using Stacks (Easy)](https://leetcode.com/problems/implement-queue-using-stacks/description/)
 * @see
 * @since 1.0
 */
public class MyStack {

    public static void main(String[] args) {
        MyStack myStack = new MyStack();
        myStack.push(1);
        myStack.push(2);
        System.out.println(myStack.pop());
        myStack.push(3);
        myStack.push(4);
        while (!myStack.isEmpty()) {
            System.out.println(myStack.pop());
        }
    }

    private Stack<Integer> in = new Stack<>();
    private Stack<Integer> out = new Stack<>();

    public void push(int n) {
        in.push(n);
    }

    public int pop() {
        in2Out();
        return out.pop();
    }

    public int peek() {
        in2Out();
        return out.peek();
    }

    public boolean isEmpty() {
        return in.isEmpty() && out.isEmpty();
    }

    private void in2Out() {
        if (out.isEmpty()) {
            while (!in.isEmpty()) {
                out.push(in.pop());
            }
        }
    }


}
