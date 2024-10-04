// Runtime 0ms beats 100.00%
// Memory 42.91 MB beats 39.54%
class Solution {
    public int searchInsert(int[] nums, int target) {
        // Target is not in range, early termination
        if (target <= nums[0])
            return 0;
        if (target > nums[nums.length - 1])
            return nums.length;

        // Binary search for position
        int left = 0, right = nums.length - 1, mid;
        while (left <= right) {
            mid = left + (right - left) / 2;

            // Base case, target found
            if (nums[mid] == target)
                return mid;

            // Base case, between the value on the left and between the current midpoint
            if (target > nums[Math.max(0, mid - 1)] && target <= nums[mid])
                return mid;

            // Target is to the right of mid
            if (nums[mid] < target)
                left = mid + 1;
            // Target is to the left of mid
            else if (nums[mid] > target)
                right = mid - 1;
        }
        return -1;
    }
}
