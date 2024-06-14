// Runtime 27ms beats 90.28%
// Memory 51.78MB beats 43.13%
class Solution {
    /**
     * Compute a list of triplets, for which their sums equal 0
     *
     * @param nums an array of allowed integers for forming triplets
     * @return a list of triplets
     */
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> triplets = new ArrayList<>();
        int leftIdx, rightIdx, valueToSkip;

        // Start with a sorted array, such that assumptions can be made on the order of the input
        Arrays.sort(nums);

        // Simplify the problem by taking the -1*first as the two-sum of second + third.
        int firstIdx = 0;
        while (firstIdx < nums.length) {
            // Can stop early once we have passed 0 due to symmetry of + operator
            if (nums[firstIdx] > 0) break;

            // Fixate the sum to the first number. Find two numbers such that they sum up to the first number.
            leftIdx = firstIdx + 1;
            rightIdx = nums.length - 1;
            while (leftIdx < rightIdx) {
                // In case the two-sum is less than the goal,move the left pointer to increase the two-sum
                if (nums[leftIdx] + nums[rightIdx] < -1 * nums[firstIdx]) {
                    leftIdx++;
                    continue;
                }
                // In case the two-sum is > goal, move the right pointer to decrease the two-sum
                if (nums[leftIdx] + nums[rightIdx] > -1 * nums[firstIdx]) {
                    rightIdx--;
                    continue;
                }
                // Sum is found, keep moving a pointer until it is a different value
                triplets.add(List.of(nums[firstIdx], nums[leftIdx], nums[rightIdx]));
                valueToSkip = nums[leftIdx];
                while (leftIdx < nums.length && nums[leftIdx] == valueToSkip) leftIdx++;
            }
            valueToSkip = nums[firstIdx];
            while (firstIdx < nums.length && nums[firstIdx] == valueToSkip) firstIdx++;
        }

        return triplets;
    }
}
