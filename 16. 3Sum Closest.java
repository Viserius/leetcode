// Runtime 12ms beats 94.86%
// Memory 42.64MB beats 95.25%
class Solution {
    /**
     * Return the sum of three integers that are closest to the target
     *
     * @param nums   array of integers
     * @param target target sum
     * @return sum that is closest to target
     */
    public int threeSumClosest(int[] nums, int target) {
        // Sort numbers to make assumptions on the ordering
        Arrays.sort(nums);

        // For each number in the array, traverse the remaining to a local optimum
        int minDiffSoFar = Integer.MAX_VALUE, leftIdx, rightIdx, threeSum, diff, closestSum = 0, firstIdx = 0;
        while (firstIdx < nums.length) {
            leftIdx = firstIdx + 1;
            rightIdx = nums.length - 1;
            while (leftIdx < rightIdx) {
                threeSum = nums[firstIdx] + nums[leftIdx] + nums[rightIdx];

                // Check if new closest sum found
                diff = Math.abs(target - threeSum);
                if (diff < minDiffSoFar) {
                    minDiffSoFar = diff;
                    closestSum = threeSum;
                    if (closestSum == target) return closestSum;
                }
                
                // Smaller than target, move left pointer
                if (threeSum < target) leftIdx++;
                // Larger than target, move right pointer
                if (threeSum > target) rightIdx--;
            }
            firstIdx++;
        }

        return closestSum;
    }
}
