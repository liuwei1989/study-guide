package com.gitbook.algorithm;

/**
 * @author liuwei56
 * @version 2019/7/11 10:44 AM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public abstract class Sort<T extends Comparable<T>> {

    protected abstract void sort(T[] nums);

    public boolean less(T v, T w) {
        return v.compareTo(w) < 0;
    }

    public void swap(T[] a, int i, int j) {
        T t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
}
