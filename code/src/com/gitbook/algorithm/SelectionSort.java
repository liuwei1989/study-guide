package com.gitbook.algorithm;

import java.util.Arrays;

/**
 * @author liuwei56
 * @version 2019/7/11 11:10 AM
 * @description 从数组中选择最小元素，将它与数组的第一个元素交换位置。再从数组剩下的元素中选择出最小的元素，将它与数组的第二个元素交换位置。不断进行这样的操作，直到将整个数组排序。
 * @see
 * @since 1.0
 */
public class SelectionSort<T extends Comparable<T>> extends Sort<T> {

    @Override
    protected void sort(T[] nums) {
        int length = nums.length;
        for (int i = 0; i < length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < length; j++) {
                if (less(nums[j], nums[min])) {
                    min = j;
                }
            }
            swap(nums, i, min);
        }
    }

    public static void main(String[] args) {
        SelectionSort selection = new SelectionSort();
        Integer[] nums = {4, 12, 5, 1, 2, 9, 1, 7};
        selection.sort(nums);
        System.out.println(Arrays.toString(nums));
    }
}
