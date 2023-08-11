class Solution {
    public int[] twoSum(int[] nums, int target) {
        // Store to retrieve values in O(1)
        Map<Integer, int[]> valToIdx = new HashMap();
        for(int i = 0; i < nums.length; i++) {
            if(!valToIdx.containsKey(nums[i])) {
                valToIdx.put(nums[i], new int[] { i, Integer.MIN_VALUE });
            } else {
                valToIdx.get(nums[i])[1] = i;
            }
        }

        // Loop over hashmap to find all values
        for(Integer i : valToIdx.keySet()) {
            int opposite = target - i;
            if(valToIdx.containsKey(opposite)) {
                int headIdx = valToIdx.get(i)[0];
                int tailIdx = (opposite == (int) i) ? valToIdx.get(i)[1] : valToIdx.get(opposite)[0];
                return new int[] { headIdx, tailIdx };
            }
        }
        return new int[] {};
    }
}
