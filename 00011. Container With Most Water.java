// Runtime 3ms beats 96%
// Memory 58 MB beats 25%
class Solution {
    public int maxArea(int[] height) {

        int front = 0; // Pointer to the front-side of the container
        int back = height.length - 1; // Pointer to the back-side of the container
        int maxArea = 0; // The maximum area found so far
        int smallestPoleHeight = Integer.MIN_VALUE; // The lowest pole for which a maximum area was found

        // Keep searching for new maxima until the pointers have passed each other,
        //  indicating the search space has been exhausted
        while (front < back) {

            // Optimalization -- the only way to increase the container area is by having poles higher
            //  than the lowest pole of the previous maximum, i.e. the width is decreasing so height must be increasing.
            //  Therefore, only attempt to check for a new maximum if the poles are taller.
            if (height[front] > smallestPoleHeight && height[back] > smallestPoleHeight) {
                // Compute new area and update maximum if applicable
                int heightNow = computeArea(height, front, back);
                if (heightNow > maxArea) {
                    maxArea = heightNow;
                    smallestPoleHeight = Math.min(height[front], height[back]);
                }
            }

            // Move the smallest pointer
            if (height[front] < height[back]) {
                front++;
            } else {
                back--;
            }
        }

        return maxArea;
    }

    private int computeArea(int[] height, int start, int end) {
        return Math.min(height[start], height[end]) * (end - start);
    }
}
