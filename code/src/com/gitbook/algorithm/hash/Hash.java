package com.gitbook.algorithm.hash;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author liuwei56
 * @version 2019/9/11 10:42 AM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class Hash {

    public static void main(String[] args) {
        int[] twoSum = Hash.twoSum(new int[]{3, 1, 2, 10, 7}, 9);
        if (twoSum != null) {
            System.out.println(twoSum[0] + " " + twoSum[1]);
        }
        System.out.println(Hash.containsDuplicate(new int[]{3, 1, 2, 10, 7, 1}));
        System.out.println(Hash.findLHS(new int[]{1, 3, 2, 2, 5, 2, 3, 7}));
        System.out.println(Hash.longestConsecutive(new int[]{1, 200, 4, 3, 2, 5}));
    }


    /**
     * [1. Two Sum (Easy)](https://leetcode.com/problems/two-sum/description/)
     *
     * @param nums
     * @param target
     * @return
     */
    public static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            int temp = target - nums[i];
            if (map.containsKey(temp)) {
                return new int[]{map.get(temp), i};
            }
            map.put(nums[i], i);
        }
        return null;
    }

    /**
     * [217. Contains Duplicate (Easy)](https://leetcode.com/problems/contains-duplicate/description/)
     *
     * @param nums
     * @return
     */
    public static boolean containsDuplicate(int[] nums) {
        Set<Integer> dataSet = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            dataSet.add(nums[i]);
        }
        return dataSet.size() < nums.length;
    }

    /**
     * [594. Longest Harmonious Subsequence (Easy)](https://leetcode.com/problems/longest-harmonious-subsequence/description/)
     *
     * @param nums
     * @return
     */
    public static int findLHS(int[] nums) {
        Map<Integer, Integer> numForCountMap = new HashMap<>();
        for (int num : nums) {
            numForCountMap.put(num, numForCountMap.getOrDefault(num, 0) + 1);
        }
        int longest = 0;
        for (int num : numForCountMap.keySet()) {
            if (numForCountMap.containsKey(num + 1)) {
                longest = Math.max(longest, num + num + 1);
            }
        }
        return longest;
    }


    /**
     * [128. Longest Consecutive Sequence (Hard)](https://leetcode.com/problems/longest-consecutive-sequence/description/)
     *
     * @param nums
     * @return
     */
    public static int longestConsecutive(int[] nums) {
        Map<Integer, Integer> countForNum = new HashMap<>();
        for (int num : nums) {
            countForNum.put(num, 1);
        }
        for (int num : nums) {
            forward(countForNum, num);
        }
        return maxCount(countForNum);
    }

    private static int forward(Map<Integer, Integer> countForNum, int num) {
        if (!countForNum.containsKey(num)) {
            return 0;
        }
        int cnt = countForNum.get(num);
        if (cnt > 1) {
            return cnt;
        }
        cnt = forward(countForNum, num + 1) + 1;
        countForNum.put(num, cnt);
        return cnt;
    }

    private static int maxCount(Map<Integer, Integer> countForNum) {
        int max = 0;
        for (int num : countForNum.keySet()) {
            max = Math.max(max, countForNum.get(num));
        }
        return max;
    }

}
