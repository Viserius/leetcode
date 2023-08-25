// Runtime 7 ms beats 58.58%
// Memory 42.6 MB beats 91.75%
class Solution {
    public int lengthOfLongestSubstring(String s) {
        int maxLength = 0;
        int currentLength = 0;
        int left = 0;
        Map<Character, Integer> characterToIdx = new HashMap<>();
        for (int right = 0; right < s.length(); right++) {
            char current = s.charAt(right);
            if (characterToIdx.containsKey(current) && characterToIdx.get(current) >= left) {
                // Duplicate character
                currentLength = currentLength - characterToIdx.get(current) + left;
                left = characterToIdx.get(current) + 1;
                characterToIdx.put(current, right);
            } else {
                // Non-duplicate character, so our unique substring increases in size
                characterToIdx.put(current, right);
                currentLength++;
                if (currentLength > maxLength)
                    maxLength = currentLength;
            }
        }
        return maxLength;
    }
}
