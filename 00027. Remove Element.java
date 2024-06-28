// Runtime 0ms beats 100.00%
// Memory 41.40MB beats 92.78%
class Solution {
    /**
     * Removes elements equal to {@code val} from the array
     *
     * @param nums array if integers, possibly containing {@code val}
     * @param val the value to remove from the array
     * @return {@code nums} with values equal to {@code val} removed, rearranged
     */
    public int removeElement(int[] nums, int val) {
        // tailIdx points to the tail of nums (excluding)
        int tailIdx = 0;

        for (int seekIdx = 0; seekIdx < nums.length; seekIdx++) {
            if (nums[seekIdx] != val) {
                nums[tailIdx++] = nums[seekIdx];
            }
        }

        return tailIdx;
    }
}
