package com.gitbook.algorithm.string;

/**
 * @author liuwei56
 * @version 2019/9/11 3:21 PM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class StringDemo {

    public static void main(String[] args) {
        System.out.println(StringDemo.isSubStr("AABCD", "CDAA"));
        System.out.println(StringDemo.cycleShift("abcd123", 3));
        System.out.println(StringDemo.isAnagram("abcd", "abcd"));
        System.out.println(StringDemo.longestPalindrome("abccccdd"));
        System.out.println(StringDemo.isIsomorphic("agg", "add"));
        System.out.println(StringDemo.countSubstrings("aaa"));
        System.out.println(StringDemo.isPalindrome(12321));
        System.out.println(StringDemo.countBinarySubstrings("00110011"));
    }

    /**
     * 给定两个字符串 s1 和 s2，要求判定 s2 是否能够被 s1 做循环移位得到的字符串包含。
     *
     * @param cycleStr
     * @param subStr
     * @return
     */
    public static boolean isSubStr(String cycleStr, String subStr) {
        if (cycleStr.length() < subStr.length()) {
            return false;
        }
        cycleStr = cycleStr + cycleStr;
        int j = 0;
        int i;
        for (i = 0; i < subStr.length(); j++) {
            if (cycleStr.charAt(j) == subStr.charAt(i)) {
                i++;
            } else if (i > 0) {
                break;
            }
        }
        return i == subStr.length();
    }

    /**
     * 字符串循环移位
     *
     * @param str
     * @param k
     * @return
     */
    public static String cycleShift(String str, int k) {
        int n = str.length();
        String s1 = reverseString(str.substring(0, n - k));
        String s2 = reverseString(str.substring(n - k, n));
        return new String(reverseString(s1 + s2));
    }

    private static String reverseString(String s1) {
        char[] chars = s1.toCharArray();
        for (int i = 0, j = chars.length - 1; i < j; i++, j--) {
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * [242. Valid Anagram (Easy)](https://leetcode.com/problems/valid-anagram/description/)
     *
     * @param s
     * @param t
     * @return
     */
    public static boolean isAnagram(String s, String t) {
        if (s.length() != t.length()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != t.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * [409. Longest Palindrome (Easy)](https://leetcode.com/problems/longest-palindrome/description/)
     *
     * @param s
     * @return
     */
    public static int longestPalindrome(String s) {
        int[] cnts = new int[256];
        for (char c : s.toCharArray()) {
            cnts[c]++;
        }
        int palindrome = 0;
        for (int cnt : cnts) {
            palindrome += (cnt / 2) * 2;
        }
        if (palindrome < s.length()) {
            palindrome++;
        }
        return palindrome;
    }

    /**
     * [205. Isomorphic Strings (Easy)](https://leetcode.com/problems/isomorphic-strings/description/)
     *
     * @param s
     * @param t
     * @return
     */
    public static boolean isIsomorphic(String s, String t) {
        int[] sIndex = new int[256];
        int[] tIndex = new int[256];
        for (int i = 0; i < s.length(); i++) {
            char cs = s.charAt(i), ts = t.charAt(i);
            if (sIndex[cs] != tIndex[ts]) {
                return false;
            }
            sIndex[cs]++;
            tIndex[ts]++;
        }
        return true;
    }

    private static int cnt = 0;

    /**
     * [647. Palindromic Substrings (Medium)](https://leetcode.com/problems/palindromic-substrings/description/)
     *
     * @param s
     * @return
     */
    public static int countSubstrings(String s) {
        for (int i = 0; i < s.length(); i++) {
            extendSubstrings(s, i, i);
            extendSubstrings(s, i, i + 1);
        }
        return cnt;
    }

    private static void extendSubstrings(String s, int start, int end) {
        while (start >= 0 && end < s.length() && s.charAt(start) == s.charAt(end)) {
            start--;
            end++;
            cnt++;
        }
    }

    /**
     * [9. Palindrome Number (Easy)](https://leetcode.com/problems/palindrome-number/description/)
     *
     * @param x
     * @return
     */
    public static boolean isPalindrome(int x) {
        if (x == 0) {
            return true;
        }
        if (x < 0 || x % 10 == 0) {
            return false;
        }
        int right = 0;
        while (x > right) {
            right = right * 10 + x % 10;
            x /= 10;
        }
        return x == right || x == right / 10;
    }

    /**
     * [696. Count Binary Substrings (Easy)](https://leetcode.com/problems/count-binary-substrings/description/)
     *
     * @param s
     * @return
     */
    public static int countBinarySubstrings(String s) {
        int preLen = 0, curLen = 1, count = 0;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                curLen++;
            } else {
                preLen = curLen;
                curLen = 1;
            }

            if (preLen >= curLen) {
                count++;
            }
        }
        return count;
    }
}
