package com.gitbook.algorithm.doublepointer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * @author liuwei56
 * @version 2019/9/10 11:12 AM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class DoublePointer {

    public static void main(String[] args) {
        DoublePointer doublePointer = new DoublePointer();
        int[] twoSum = doublePointer.twoSum(new int[]{2, 7, 11, 15}, 9);
        if (twoSum != null) {
            System.out.println(twoSum[0] + " " + twoSum[1]);
        }
        System.out.println(doublePointer.judgeSquareSum(5));
        System.out.println(doublePointer.judgeSquareSum(6));
        System.out.println(doublePointer.reverseVowels("leetcode"));
        System.out.println(doublePointer.validPalindrome("abdba"));
        List list = new ArrayList<String>();
        list.add("ale");
        list.add("apple");
        list.add("monkey");
        list.add("plea");
        System.out.println(doublePointer.findLongestWord("abpcplea", list));
    }

    /**
     * [Leetcode ：167. Two Sum II - Input array is sorted (Easy)](https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/description/)
     *
     * @param numbers
     * @param target
     * @return
     */
    public int[] twoSum(int[] numbers, int target) {
        int l = 0, r = numbers.length - 1;
        while (l < r) {
            int result = numbers[l] + numbers[r];
            if (result == target) {
                return new int[]{l + 1, r + 1};
            } else if (result > target) {
                r--;
            } else {
                l++;
            }
        }
        return null;
    }

    /**
     * [633. Sum of Square Numbers (Easy)](https://leetcode.com/problems/sum-of-square-numbers/description/)
     *
     * @param c
     * @return
     */
    public boolean judgeSquareSum(int c) {
        int i = 0, j = (int) Math.sqrt(c);
        while (i <= j) {
            int sum = i * i + j * j;
            if (sum == c) {
                return true;
            } else if (sum > c) {
                j--;
            } else {
                i++;
            }
        }
        return false;
    }

    public String reverseVowels(String s) {
        HashSet<Character> vowels = new HashSet<>(Arrays.asList('a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U'));
        int i = 0, j = s.length() - 1;
        char[] result = new char[s.length()];
        while (i <= j) {
            char ci = s.charAt(i);
            char cj = s.charAt(j);
            if (!vowels.contains(ci)) {
                result[i++] = ci;
            } else if (!vowels.contains(cj)) {
                result[j--] = cj;
            } else {
                result[i++] = cj;
                result[j--] = ci;
            }
        }
        return new String(result);
    }

    /**
     * [680. Valid Palindrome II (Easy)](https://leetcode.com/problems/valid-palindrome-ii/description/)
     *
     * @param s
     * @return
     */
    public boolean validPalindrome(String s) {
        for (int i = 0, j = s.length() - 1; i < j; i++, j--) {
            if (s.charAt(i) != s.charAt(j)) {
                return isPalindrome(s, i, j - 1) || isPalindrome(s, i + 1, j);
            }
        }
        return true;
    }

    private boolean isPalindrome(String s, int i, int j) {
        while (i < j) {
            if (s.charAt(i++) != s.charAt(j)) {
                return false;
            }
        }
        return true;
    }

    public String findLongestWord(String s, List<String> d) {
        String longestWord = "";
        for (String target : d) {
            int l1 = longestWord.length(), l2 = target.length();
            if (l1 > l2 || (l1 == l2 && longestWord.compareTo(target) < 0)) {
                continue;
            }
            if (isSubstr(s, target)) {
                longestWord = target;
            }
        }
        return longestWord;
    }

    private boolean isSubstr(String s, String target) {
        int i = 0, j = 0;
        while (i < s.length() && j < target.length()) {
            if (s.charAt(i) == target.charAt(j)) {
                j++;
            }
            i++;
        }
        return j == target.length();
    }
}
