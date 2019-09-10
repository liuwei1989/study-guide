package com.gitbook.lang;

/**
 * @author liuwei56
 * @version 2019/8/22 11:13 AM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class CustomString {

    public static void main(String[] args) {
        String s = new String("功能描述功能描述".getBytes(), 2, 2);
        System.out.println(s);

        String s1 = "abc";
        String s2 = "abcde";
        System.out.println(s1.compareTo(s2));
        System.out.println(s2.startsWith("cde", 2));
    }
}
