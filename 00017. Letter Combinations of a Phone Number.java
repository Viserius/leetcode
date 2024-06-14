// Runtime 1ms beats 69.03%
// Memory 41.83MB beats 78.31%
class Solution {
    Map<Character, char[]> digitToChars = Map.of(
            '2', new char[] { 'a', 'b', 'c' },
            '3', new char[] { 'd', 'e', 'f' },
            '4', new char[] { 'g', 'h', 'i' },
            '5', new char[] { 'j', 'k', 'l' },
            '6', new char[] { 'm', 'n', 'o' },
            '7', new char[] { 'p', 'q', 'r', 's' },
            '8', new char[] { 't', 'u', 'v' },
            '9', new char[] { 'w', 'x', 'y', 'z' });

    public List<String> letterCombinations(String digits) {
        if (digits.isEmpty()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        this.letterCombinationsRecursive(new StringBuilder(), digits, result);
        return result;
    }

    public void letterCombinationsRecursive(StringBuilder combinationSoFar,
                                            String remainingDigits,
                                            List<String> results) {
        // Base case
        if (remainingDigits.isEmpty()) {
            results.add(combinationSoFar.toString());
            return;
        }

        // Recursive case
        for (char c : digitToChars.get(remainingDigits.charAt(0))) {
            letterCombinationsRecursive(new StringBuilder(combinationSoFar).append(c), remainingDigits.substring(1), results);
        }
    }
}
