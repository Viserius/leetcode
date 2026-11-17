// Runtime 1ms beats 89%
// Memory 45MB beats 89%
class Solution {
    public List<List<Integer>> permute(int[] nums) {
        var result = new ArrayList<List<Integer>>();
        permute(nums, new ArrayList<>(), new boolean[nums.length], result);
        return result;
    }

    private void permute(int[] nums, List<Integer> huidig, boolean[] alGebruikt, List<List<Integer>> solution) {
        // Base case
        // We hebben een oplossing gevonden. Voeg de oplossing toe, en backtrack.
        if (huidig.size() == nums.length) {
            solution.add(new ArrayList<>(huidig));
            return;
        }

        // Keuzes verkennen
        for (int i = 0; i < nums.length; i++) {
            if (alGebruikt[i]) continue; // Keuze kan nooit geldig zijn

            huidig.add(nums[i]); // Keuze toevoegen
            alGebruikt[i] = true;
            permute(nums, huidig, alGebruikt, solution); // Verken nieuwe keuzes recursief
            huidig.removeLast(); // Keuze ongedaan maken
            alGebruikt[i] = false;
        }
    }
}
