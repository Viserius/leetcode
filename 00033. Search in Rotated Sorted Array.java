// Runtime 0ms beats 100.00%
// Memory 41.96MB beats 54.00%
class Solution {
    
    /**
     * Regular binarysearch
     */
    public int search(int[] nums, int target) {
        int left = 0, right = nums.length - 1, mid;
        while (left <= right) {
            mid = (left + right) / 2;
            if (nums[mid] == target)
                return mid;

            // Check if target is in left half
            if (isBetween(nums[left], nums[mid], target)) {
                right = mid - 1;
                continue;
            }

            // Check if target is in right half
            if (isBetween(nums[mid], nums[right], target)) {
                left = mid + 1;
                continue;
            }
            // Outside of range
            return -1;
        }
        return -1;
    }

    /**
     * Compute whether a value is expected to fall between an interval that may be
     * rotated
     * isBetween(0, 3, 4) = false
     * isBetween(3, 0, 4) = true
     *
     * @param first  one end of the interval
     * @param second the other end of the interval
     * @param test   the value to check
     * @return true if within interval (inclusive), false otherwise
     */
    private boolean isBetween(int first, int second, int test) {
        if (second < first) {
            // Wraparound applies
            return test <= second || test >= first;
        } else {
            // Regular interval without wraparound
            return test >= first && test <= second;
        }
    }
}
