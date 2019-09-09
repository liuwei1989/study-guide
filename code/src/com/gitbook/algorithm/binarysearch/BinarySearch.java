package com.gitbook.algorithm.binarysearch;

/**
 * @author liuwei
 * @version 2019/9/9 10:52 AM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class BinarySearch {

    public static void main(String[] args) {
        BinarySearch search = new BinarySearch();
        int[] nums = new int[]{1, 2, 3, 4, 5};
        int result = search.binarySearch(nums, 2);
        System.out.println(result);

        nums = new int[]{0, 1, 1, 1, 2};
        result = search.binarySearch2(nums, 1);
        System.out.println(result);
        System.out.println(search.sqrt(8));
        char[] letters = new char[]{'a', 'b', 'l'};
        System.out.println(search.nextGreatestLetter(letters, 'a'));
        System.out.println(search.singleNonDuplicate(new int[]{1, 1, 2, 2, 3, 3, 4, 5, 5}));
        System.out.println(search.findMin(new int[]{3, 4, 5, 1, 2}));
        int[] searchRange = search.searchRange(new int[]{5, 7, 7, 8, 8, 10}, 8);
        System.out.println(searchRange[0] + "===" + searchRange[1]);
        searchRange = search.searchRange(new int[]{5, 7, 7, 8, 8, 10}, 9);
        System.out.println(searchRange[0] + "===" + searchRange[1]);
    }

    public int binarySearch(int[] nums, int key) {
        int l = 0, h = nums.length - 1;
        while (l <= h) {
            int mid = l + (h - l) / 2;
            if (nums[mid] > key) {
                h = mid - 1;
            } else if (nums[mid] < key) {
                l = mid + 1;
            } else {
                return mid;
            }
        }
        return -1;
    }

    /**
     * {0,1,2} ==>1
     *
     * @param nums
     * @param key
     * @return
     */
    public int binarySearch2(int[] nums, int key) {
        int l = 0, h = nums.length - 1;
        while (l < h) {
            int mid = l + (h - l) / 2;
            if (nums[mid] >= key) {
                h = mid;
            } else {
                l = mid + 1;
            }
        }
        return l;
    }

    /**
     * [69. Sqrt(x) (Easy)](https://leetcode.com/problems/sqrtx/description/)
     *
     * @param n
     * @return
     */
    public int sqrt(int n) {
        if (n <= 1) {
            return n;
        }
        int l = 1, h = n;
        while (l <= h) {
            int mid = l + (h - l) / 2;
            int sqrt = n / mid;
            if (sqrt > mid) {
                l = mid + 1;
            } else if (sqrt < mid) {
                h = mid - 1;
            } else {
                return mid;
            }
        }
        return h;
    }

    /**
     * [744. Find Smallest Letter Greater Than Target (Easy)](https://leetcode.com/problems/find-smallest-letter-greater-than-target/description/)
     *
     * @param letters
     * @param target
     * @return
     */
    public char nextGreatestLetter(char[] letters, char target) {
        int l = 0, h = letters.length - 1;
        while (l <= h) {
            int mid = l + (h - l) / 2;
            if (letters[mid] <= target) {
                l = mid + 1;
            } else {
                h = mid - 1;
            }
        }
        return l < letters.length ? letters[l] : letters[0];
    }

    /**
     * [540. Single Element in a Sorted Array (Medium)](https://leetcode.com/problems/single-element-in-a-sorted-array/description/)
     *
     * @param nums
     * @return
     */
    public int singleNonDuplicate(int[] nums) {
        int l = 0, h = nums.length - 1;
        while (l < h) {
            int mid = l + (h - l) / 2;
            if (mid % 2 == 1) {
                mid--;
            }
            if (nums[mid] == nums[mid + 1]) {
                l = mid + 2;
            } else {
                h = mid;
            }
        }
        return l < nums.length ? nums[l] : -1;
    }

    /**
     * [278. First Bad Version (Easy)](https://leetcode.com/problems/first-bad-version/description/)
     *
     * @param n
     */
    public void firstBadVersion(int n) {
        return;
    }

    /**
     * [153. Find Minimum in Rotated Sorted Array (Medium)](https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/description/)
     */
    public int findMin(int[] nums) {
        int l = 0, h = nums.length - 1;
        while (l < h) {
            int mid = l + (h - l) / 2;
            if (nums[mid] >= nums[h]) {
                l = mid + 1;
            } else {
                h = mid - 1;
            }
        }
        return l < nums.length ? nums[l] : -1;
    }

    /**
     * [34. Find First and Last Position of Element in Sorted Array](https://leetcode.com/problems/find-first-and-last-position-of-element-in-sorted-array/)
     */
    public int[] searchRange(int[] nums, int target) {
        int first = binarySearch2(nums, target);
        int last = binarySearch2(nums, target + 1) - 1;
        if (first == nums.length || nums[first] != target) {
            return new int[]{-1, -1};
        }
        return new int[]{first, Math.max(first, last)};
    }
}






