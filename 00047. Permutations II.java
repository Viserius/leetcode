// Runtime 3ms beats 95%
// Memory 46MB beats 74%
class Solution {
    public List<List<Integer>> permuteUnique(int[] nums) {
        Arrays.sort(nums);
        var result = new ArrayList<List<Integer>>();
        permute(nums, result, new ArrayList<>(), new boolean[nums.length]);
        return result;
    }

    private void permute(int[] nums, List<List<Integer>> result, List<Integer> huidig, boolean[] idxVerkend) {
        // Base case
        if (huidig.size() == nums.length) {
            result.add(new ArrayList<>(huidig));
            return;
        }

        // Alle mogelijke keuzes
        for (int i = 0; i < nums.length; i++) {
            // Pruning
            if (i > 0 && nums[i] == nums[i - 1] && !idxVerkend[i - 1]) continue;
            if (idxVerkend[i]) continue;

            // Keuze maken
            huidig.add(nums[i]);
            idxVerkend[i] = true;

            // Verdere keuzes verkennen
            permute(nums, result, huidig, idxVerkend);

            // Keuze ongedaan maken
            huidig.removeLast();
            idxVerkend[i] = false;
        }
    }
}
