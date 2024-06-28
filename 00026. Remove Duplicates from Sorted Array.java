// Runtime 1ms beats 79.14%
// Memory 44.43MB beats 82.53%
class Solution {
    /**
     * Removes duplicates from an array by moving the k unique elements to the first
     * k positions
     *
     * @param nums a non-decreasing array
     * @return the number of unique elements in nums
     */
    public int removeDuplicates(int[] nums) {
        // arrangeIdx is the next index to put an increasing number
        int arrangeIdx = 1;

        for (int seekIdx = 1; seekIdx < nums.length; seekIdx++) {
            // A new number to set in our ordered array
            if (nums[seekIdx] > nums[arrangeIdx - 1]) {
                nums[arrangeIdx++] = nums[seekIdx];
            }
        }

        return arrangeIdx;
    }
}
