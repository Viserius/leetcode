// Runtime 4ms beats 16%
// Memory 46MB beats 52%
// Runtime kan eenvoudig verbeterd worden door sort weg te laten en van de break; in de loop een continue te maken, maar mijn voorkeur gaat hiernaar uit omdat de input nu voorspelbaar en geordend is.
class Solution {
    public List<List<Integer>> combinationSum(int[] candidates, int target) {
        var result = new ArrayList<List<Integer>>();
        combinationSum(candidates, 0, target, new ArrayList<>(target / candidates[0] + 1), result);
        return result;
    }

    private void combinationSum(int[] candidates, int fromIndex, int target, List<Integer> huidig, List<List<Integer>> result) {
        // Base case: Bij een target van nul is er een combinatie gevonden:
        if (target == 0) {
            result.add(new ArrayList<>(huidig));
            return;
        }
        // Voor elke mogelijke candidate, probeer te zoeken naar oplossingen
        for (int i = fromIndex; i < candidates.length; i++) {
            if (candidates[i] > target) continue;
            huidig.add(candidates[i]);
            combinationSum(candidates, i, target - candidates[i], huidig, result);
            huidig.remove(huidig.size() - 1);
        }
    }
}