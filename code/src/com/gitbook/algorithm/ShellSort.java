package com.gitbook.algorithm;

import java.util.Arrays;

/**
 * @author liuwei56
 * @version 2019/7/11 4:52 PM
 * @description 对于大规模的数组，插入排序很慢，因为它只能交换相邻的元素，每次只能将逆序数量减少 1。希尔排序的出现就是为了解决插入排序的这种局限性，
 * 它通过交换不相邻的元素，每次可以将逆序数量减少大于 1。
 * <p>
 * 希尔排序使用插入排序对间隔 h 的序列进行排序。通过不断减小 h，最后令 h=1，就可以使得整个数组是有序的。
 * @see
 * @since 1.0
 */
public class ShellSort<T extends Comparable<T>> extends Sort<T> {
    @Override
    protected void sort(T[] nums) {
        int length = nums.length;
        int h = length / 3 + 1;
        while (h >= 1) {
            for (int i = h; i < length; i++) {
                for (int j = i; j >= h && less(nums[j], nums[j - h]); j -= h) {
                    swap(nums, j, j - h);
                }
            }
            h = h / 3;
        }
    }

    public static void main(String[] args) {
        ShellSort selection = new ShellSort();
        Integer[] nums = {4, 12, 5, 1, 2, 9, 1, 7, 10, 12, 43, 81, 23};
        selection.sort(nums);
        System.out.println(Arrays.toString(nums));
    }
}
