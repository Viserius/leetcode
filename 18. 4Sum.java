// Runtime 3ms beats 99.64%
// Memory 43.51MB beats 88.50%
class Solution {
    public List<List<Integer>> fourSum(int[] nums, int target) {
        // Sort the input, so we know exactly how we receive the input
        Arrays.sort(nums);
        List<List<Integer>> result = new ArrayList<>();

        // Fixate the first and last value of the input,
        // search within the two markers for all combinations to sum up to target
        // and with each iteration, decrease the search space.
        int outerLeftIdx = 0;
        while (outerLeftIdx < nums.length - 3) {
            // Outer left is always moving right, and therefore increasing the total sums
            // We can terminate early, when the minimum reachable sum is over the target
            if((long) nums[outerLeftIdx] + nums[outerLeftIdx+1] + nums[outerLeftIdx+2] + nums[outerLeftIdx+3] > target) break;

            int outerRightIdx = nums.length - 1, outerLeftVal = nums[outerLeftIdx];
            while (outerRightIdx > outerLeftIdx + 2) {
                // Outer right is always moving left, and therefore decreasing the total sums
                // We can terminate early, when the maximum reachable sum is under the target
                if((long) nums[outerLeftIdx] + nums[outerRightIdx-2] + nums[outerRightIdx-1] + nums[outerRightIdx] < target) break;
                int outerRightVal = nums[outerRightIdx];

                // Create inner left and inner right pointer,
                // and keep going right (increment) if too low
                // and keep going left (decrement) if too high
                int innerLeftIdx = outerLeftIdx + 1;
                int innerRightIdx = outerRightIdx - 1;
                while (innerLeftIdx < innerRightIdx) {
                    // Outer left and right are fixed, inner pointers can move to increase or decrease
                    // We can terminate early, when the maximum reachable sum is under the target
                    if((long) nums[outerLeftIdx] + nums[outerRightIdx] + nums[innerRightIdx] + nums[innerRightIdx-1] < target) break;
                    // We can temrinate early, when the minimum reachable sum is over the target
                    if((long) nums[outerLeftIdx] + nums[outerRightIdx] + nums[innerLeftIdx] + nums[innerLeftIdx+1] > target) break;

                    // Check if the sum is equal to the target
                    int innerLeftVal = nums[innerLeftIdx], innerRightVal = nums[innerRightIdx];
                    long currentSum = (long) outerLeftVal + outerRightVal + innerLeftVal + innerRightVal;
                    if(currentSum == target) {
                        // Valid combination, add to the list of results
                        // and decrement the right pointer to find for more combinations
                        result.add(List.of(outerLeftVal, innerLeftVal, innerRightVal, outerRightVal));
                        while(innerRightIdx > innerLeftIdx && nums[innerRightIdx] == innerRightVal) innerRightIdx--;
                    } else if(currentSum < target) {
                        // Our sum is too low, so we need to increment by moving the left pointer to the right
                        while(innerLeftIdx < innerRightIdx && nums[innerLeftIdx] == innerLeftVal) innerLeftIdx++;
                    } else {
                        // Our sum is too high, so we need to decrement by moving the right pointer to the left
                        while(innerRightIdx > innerLeftIdx && nums[innerRightIdx] == innerRightVal) innerRightIdx--;
                    }
                }

                while(outerRightIdx > outerLeftIdx + 2 && nums[outerRightIdx] == outerRightVal) outerRightIdx--;
            }
            while(outerLeftIdx < nums.length - 3 && nums[outerLeftIdx] == outerLeftVal) outerLeftIdx++;
        }
        return result;
    }
}
