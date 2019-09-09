package com.gitbook.leetcode;

import org.junit.jupiter.api.Test;

/**
 * @author liuwei56
 * @version 2019/9/2 11:23 AM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class BitOperation {

    /**
     * [461. Hamming Distance (Easy)](https://leetcode.com/problems/hamming-distance/)
     */
    @Test
    public void testHammingDistance1() {
        int x = 2, y = 4;
        int z = x ^ y;
        int result = 0;
        while (z != 0) {
            if ((z & 1) == 1) result++;
            z = z >> 1;
        }
        System.out.println(result);
    }

    @Test
    public void testHammingDistance2() {
        int x = 2, y = 4;
        int z = x ^ y;
        int result = 0;
        while (z != 0) {
            z = z & (z - 1);
            result++;
        }
        System.out.println(result);
    }

    @Test
    public void testHammingDistance3() {
        int x = 2, y = 4;
        System.out.println(Integer.bitCount(x ^ y));
    }

    /**
     * [136. Single Number (Easy)](https://leetcode.com/problems/single-number/description/)
     */
    @Test
    public void testSingleNumber() {
        int ret = 0;
        int[] arr = new int[]{3, 1, 2, 2, 1};
        for (int a : arr) {
            ret ^= a;
        }
        System.out.println(ret);
    }

    /**
     * [268. Missing Number (Easy)](https://leetcode.com/problems/missing-number/description/)
     */
    @Test
    public void testMissingNumber() {
        int[] arr = new int[]{3, 1, 2, 0, 5};
        int ret = 0;
        for (int i = 0; i < arr.length; i++) {
            ret = ret ^ i ^ arr[i];
        }
        System.out.println(ret ^ arr.length);
    }

    /**
     * [260. Single Number III (Medium)](https://leetcode.com/problems/single-number-iii/description/)
     */
    @Test
    public void testSingleNumber2() {
        int[] nums = new int[]{3, 1, 2, 2, 1, 5};
        int diff = 0;
        for (int num : nums) diff ^= num;
        diff &= -diff;  // 得到最右一位
        int[] ret = new int[2];
        for (int num : nums) {
            if ((num & diff) == 0) ret[0] ^= num;
            else ret[1] ^= num;
        }
        System.out.println(ret[0] + "--" + ret[1]);
    }

    /**
     * [190. Reverse Bits (Easy)](https://leetcode.com/problems/reverse-bits/description/)
     */
    @Test
    public void testReverseBits() {
        int n = 2;
        int ret = 0;
        for (int i = 0; i < 32; i++) {
            ret <<= 1;
            ret |= (n & 1);
            n >>>= 1;
        }
        System.out.println(ret);
    }

    /**
     * [231. Power of Two (Easy)](https://leetcode.com/problems/power-of-two/description/)
     */
    @Test
    public void testIsPowerOfTwo() {
        int n = 16;
        boolean ret = n > 0 && (n & (n - 1)) == 0;
        System.out.println(ret);
    }

    /**
     * [342. Power of Four (Easy)](https://leetcode.com/problems/power-of-four/)
     */
    @Test
    public void testIsPowerOfFour() {
        int n = 8;
        boolean ret = n > 0 && (n & (n - 1)) == 0 && (n & 0b01010101010101010101010101010101) != 0;
        System.out.println(ret);
    }

    /**
     * [693. Binary Number with Alternating Bits (Easy)](https://leetcode.com/problems/binary-number-with-alternating-bits/description/)
     */
    @Test
    public void testHasAlternatingBits() {
        int n = 10;
        int a = n ^ (n >> 1);
        boolean ret = (a & (a + 1)) == 0;
        System.out.println(ret);
    }
}
