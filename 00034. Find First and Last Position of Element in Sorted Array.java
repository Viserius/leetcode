// Runtime 0ms beats 100.00%
// Memory 45.45MB beats 96.93%
class Solution {
    public int[] searchRange(int[] nums, int target) {
        return new int[]{
                findFirstOccurrenceOfTarget(nums, target),
                findLastOccurrenceOfTarget(nums, target)
        };
    }

    private int findFirstOccurrenceOfTarget(int[] nums, int target) {
        int left = 0, right = nums.length - 1, mid;
        while (left <= right) {
            mid = (left + right) / 2;

            // Result found
            if (nums[left] == target) return left;
            if (left == right) return -1;

            // Check if start position is in left half
            if (nums[mid] >= target) 
                right = mid;
            else if (nums[mid] < target) // Check if start position is in right half
                left = mid + 1;
        }
        // Nothing found, range exhausted
        return -1;
    }

    private int findLastOccurrenceOfTarget(int[] nums, int target) {
        int left = 0, right = nums.length - 1, mid;
        while (left <= right) {
            mid = (left + right) / 2 + 1;

            // Result found
            if (nums[right] == target) return right;
            if (left == right) return -1;

            // Check if start position is in left half
            if (nums[mid] > target)
                right = mid - 1;
            else if (nums[mid] <= target) // Check if start position is in right half
                left = mid;
        }
        // Nothing found, range exhausted
        return -1;
    }
}
