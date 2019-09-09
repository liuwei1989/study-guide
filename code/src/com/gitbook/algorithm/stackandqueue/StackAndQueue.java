package com.gitbook.algorithm.stackandqueue;

import java.util.Arrays;
import java.util.Stack;

/**
 * @author liuwei
 * @version 2019/9/9 5:34 PM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class StackAndQueue {

    public static void main(String[] args) {
        StackAndQueue stackAndQueue = new StackAndQueue();
        System.out.println(stackAndQueue.isValid("()[]{}"));
        int[] dailyTemperatures = stackAndQueue.dailyTemperatures(new int[]{73, 74, 75, 71, 69, 72, 76, 73});
        for (int i = 0; i < dailyTemperatures.length; i++) {
            System.out.print(dailyTemperatures[i] + " ");
        }
        System.out.println();
        int[] ints = stackAndQueue.nextGreaterElements(new int[]{1, 3, 1});
        for (int i = 0; i < ints.length; i++) {
            System.out.print(ints[i] + " ");
        }
    }

    /**
     * [20. Valid Parentheses (Easy)](https://leetcode.com/problems/valid-parentheses/description/)
     */
    public boolean isValid(String s) {
        Stack<Character> stack = new Stack<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else {
                if (stack.isEmpty()) {
                    return false;
                }
                Character cStack = stack.pop();
                if (c == ')' && cStack != '(') {
                    return false;
                } else if (c == '[' && cStack != ']') {
                    return false;
                } else if (c == '{' && cStack != '}') {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    /**
     * [739. Daily Temperatures (Medium)](https://leetcode.com/problems/daily-temperatures/description/)
     * {73, 74, 75, 71, 69, 72, 76, 73}
     *
     * @param temperatures
     * @return
     */
    public int[] dailyTemperatures(int[] temperatures) {
        int n = temperatures.length;
        int[] dist = new int[n];
        Stack<Integer> indexs = new Stack<>();
        for (int i = 0; i < n; i++) {
            while (!indexs.isEmpty() && temperatures[i] > temperatures[indexs.peek()]) {
                int preIndex = indexs.pop();
                dist[preIndex] = i - preIndex;
            }
            indexs.add(i);
        }
        return dist;
    }

    /**
     * [503. Next Greater Element II (Medium)](https://leetcode.com/problems/next-greater-element-ii/description/)
     * 1,3,1,4
     *
     * @param nums
     * @return
     */
    public int[] nextGreaterElements(int[] nums) {
        int n = nums.length;
        int[] next = new int[n];
        Arrays.fill(next, -1);
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < n * 2; i++) {
            int num = nums[i % n];
            while (!stack.isEmpty() && nums[stack.peek()] < num) {
                next[stack.pop()] = num;
            }
            if (i < n) {
                stack.push(i);
            }
        }
        return next;
    }
}
