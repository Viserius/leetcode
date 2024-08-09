// Runtime 0ms beats 100.00%
// Memory 43.09MB beats 34.99%
class Solution {
    public void nextPermutation(int[] nums) {
        // Starting from the back, find the first number num[x], for which num[x] < num[x+1].
        int x = -1;
        for (int xTest = nums.length - 2; xTest >= 0; xTest--) {
            if (nums[xTest] < nums[xTest + 1]) {
                x = xTest;
                break;
            }
        }

        // Edge case, the array is ordered DESC. Invert the entire array;
        if (x == -1) {
            invert(nums, 0);
            return;
        }

        // Given: num[x] < num[x+1]
        // For this num[x], find the smallest (and last) num[y] such that num[y] > num[x], where y > x
        int y = x + 1;
        for (int yTest = x + 2; yTest < nums.length; yTest++) {
            if (nums[yTest] <= nums[y] && nums[yTest] > nums[x])
                y = yTest;
        }

        // Swap num[x] with num[y]
        swap(nums, x, y);

        // Sort num[x+1..]
        invert(nums, x + 1);
    }

    private void swap(int[] nums, int firstIdx, int secondIdx) {
        int temp = nums[firstIdx];
        nums[firstIdx] = nums[secondIdx];
        nums[secondIdx] = temp;
    }

    private void invert(int[] nums, int startIdx) {
        for (int x = startIdx; x - startIdx < (nums.length - startIdx) / 2; x++) {
            swap(nums, x, nums.length - x - 1 + startIdx);
        }
    }
}
