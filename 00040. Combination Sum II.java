// Runtime 6ms beats 73%
// Memory 45MB beats 82%
class Solution {
    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        // Sorteer input zodat volgorde voorspelbaar is
        Arrays.sort(candidates);
        var solutions = new ArrayList<List<Integer>>();
        this.combinationSum2(candidates, 0, new ArrayList<>(), target, solutions);
        return solutions;
    }

    private void combinationSum2(int[] candidates, int fromIdx, List<Integer> workingList, int remainingTarget, List<List<Integer>> solutions) {
        // Base case - de huidige som is  gelijk aan de target
        if (remainingTarget == 0) {
            // We hebben een oplossing gevonden
            solutions.add(new ArrayList<>(workingList));
            return;
        }

        // Backtracking case - opbouwen van workingList met alle getallen van klein naar groot
        for (int i = fromIdx; i < candidates.length; i++) {
            // Early termination -- als de waarde de som te groot maakt, kunnen we de loop afbreken
            // en zullen er geen oplossingen meer gevonden kunnen worden
            if (remainingTarget - candidates[i] < 0) break;

            // Probeer een geldige oplossing te vinden met het huidige getal
            workingList.add(candidates[i]);
            combinationSum2(candidates, i + 1, workingList, remainingTarget - candidates[i], solutions);
            workingList.removeLast();

            // Sla alle volgende elementen over die dezelfde waarde hebben
            // Dit kunnen we doen omdat bij [1, 1, 2, 3], we na de eerste 1 we precies dezelfde antwoorden verwachten
            while (i < candidates.length - 1 && candidates[i + 1] == candidates[i]) i++;
        }
    }
}