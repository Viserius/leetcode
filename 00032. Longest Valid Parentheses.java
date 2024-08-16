// Runtime 4ms beats 32.86%
// Memory 45.1MB beats 5.45%
class Solution {
    public int longestValidParentheses(String s) {
        // currentDepth is het nestingsniveau van de parentheses
        // waarbij een negatieve waarde een invalide diepte aangeeft
        int currentDepth = 0, maxLength = 0;
        // depthToLastIdx geeft aan wat de index is van de laatste geldige occurrence
        Map<Integer, Integer> depthToLastIdx = new HashMap<>();
        for(int i = 0; i < s.length(); i++) {
            // Diepte updaten op basis van de nieuwe parenthesis
            currentDepth = (s.charAt(i) == '(') ? Math.max(1, currentDepth+1) : currentDepth-1;

            // Voor een geldige waarde
            if(currentDepth >= 0) {
                // de maximaal geldige lengte updaten met de waarde tot nu toe
                if(s.charAt(i) == ')') {
                    if(depthToLastIdx.containsKey(currentDepth+1))
                        maxLength = Math.max(maxLength, i - depthToLastIdx.get(currentDepth+1) + 1);
                    // Vorige open-haakje vergeten bij sluiten van een haakje
                    depthToLastIdx.remove(currentDepth+2);
                } 

                // Registreren dat de laatste diepte-waarde gezien is op deze idx
                depthToLastIdx.putIfAbsent(currentDepth, i);
            }
            // Bij ongeldig worden, open-haakje op diepte 1 vergeten
            if(s.charAt(i) == ')' && currentDepth == -1) {
                depthToLastIdx.remove(1);
            }
        }

        return maxLength;
    }
}
