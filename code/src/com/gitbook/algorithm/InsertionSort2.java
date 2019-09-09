package com.gitbook.algorithm;

import java.util.Arrays;

/**
 * @author liuwei
 * @date 2019/9/5 0:32
 */
public class InsertionSort2 {

    public static void main(String[] args) {
        InsertionSort2 sort = new InsertionSort2();
        int[] a = new int[]{4, 5, 6, 1, 3, 2};
        sort.insertionSort(a, 6);
        System.out.println(Arrays.toString(a));
    }

    public void insertionSort(int[] a, int n) {
        if (n <= 1) return;
        for (int i = 1; i < n; ++i) {
            int value = a[i];
            int j = i - 1;
            // 查找插入的位置
            for (; j >= 0; j--) {
                if (a[j] > value) {
                    a[j + 1] = a[j]; // 数据移动
                } else {
                    break;
                }
            }
            a[j +1 ] = value; // 插入数据
        }
    }
}
