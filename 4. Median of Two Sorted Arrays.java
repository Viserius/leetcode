class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        // Search range of 2nd array
        if (nums1.length > nums2.length)
            return findMedianSortedArrays(nums2, nums1);
        int leftIdx = 0;
        int rightIdx = nums1.length - 1;
        int numTotalElements = nums1.length + nums2.length;
        int numLeftMedianElements = numTotalElements / 2;

        // Finding median
        while (true) {
            // Take middle pivot of the first array
            int pivotFirstIdx = (leftIdx + rightIdx) / 2;
            if (rightIdx == -1) pivotFirstIdx = -1;
            int numElementsInFirst = pivotFirstIdx + 1;
            int pivotFirstVal = eval(nums1, pivotFirstIdx);

            // Based on the size of the first array, take the remainder of the second array
            int numElementsShouldBeInSecond = numLeftMedianElements - numElementsInFirst;
            int pivotSecondIdx = numElementsShouldBeInSecond - 1;
            int pivotSecondVal = eval(nums2, pivotSecondIdx);

            // Check if our partitions are sound
            if (pivotFirstVal > eval(nums2, pivotSecondIdx + 1)) {
                // Our first partition is too large in the first array. We decrease the size of the first array.
                rightIdx = pivotFirstIdx - 1;
                continue;
            }
            if (pivotSecondVal > eval(nums1, pivotFirstIdx + 1)) {
                // Our first partition is large in the second array. We increase the size of the first array.
                leftIdx = pivotFirstIdx + 1;
                continue;
            }

            // Partition is sound
            if (numTotalElements % 2 == 0) {
                // Even number of elements, so we take the average of the last of partition 1 + first of partition 2
                return (Math.max(pivotFirstVal, pivotSecondVal)
                        + Math.min(eval(nums1, pivotFirstIdx + 1), eval(nums2, pivotSecondIdx + 1)))
                        / 2.0;
            } else {
                // Odd number of elements, so we take the first value after the first partition
                return Math.min(eval(nums1, pivotFirstIdx + 1), eval(nums2, pivotSecondIdx + 1));
            }
        }
    }

    int eval(int[] arr, int idx) {
        if (idx < 0)
            return Integer.MIN_VALUE;
        else if (idx >= arr.length)
            return Integer.MAX_VALUE;
        return arr[idx];
    }
}
