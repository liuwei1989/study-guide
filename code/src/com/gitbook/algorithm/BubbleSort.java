package com.gitbook.algorithm;

import java.util.Arrays;

/**
 * @author liuwei56
 * @version 2019/7/11 2:18 PM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class BubbleSort<T extends Comparable<T>> extends Sort<T> {
    @Override
    protected void sort(T[] nums) {
        boolean sorted = false;
        for (int i = 0; i < nums.length - 1 && !sorted; i++) {
            sorted = true;
            for (int j = i; j < nums.length; j++) {
                if (less(nums[j + 1], nums[j])) {
                    sorted = false;
                    swap(nums, j, j + 1);
                }
            }
        }
    }

    public static void main(String[] args) {
        SelectionSort selection = new SelectionSort();
        Integer[] nums = {4, 12, 5, 1, 2, 9, 1, 7};
        selection.sort(nums);
        System.out.println(Arrays.toString(nums));
    }
}
