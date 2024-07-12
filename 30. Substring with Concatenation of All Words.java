// Runtime 19ms beats 54.07%
// Memory 45.08 MB beats 92.52%
class Solution {
    public List<Integer> findSubstring(String s, String[] words) {
        // Input sanitization
        int wordLength = words[0].length();
        Map<String, Integer> wordsCount = new HashMap<>();
        for (String word : words)
            wordsCount.compute(word, (k, v) -> (v == null) ? 1 : v + 1);

        Map<String, Integer> wordsFound = new HashMap<>();
        List<Integer> result = new ArrayList<>();

        // Loop over the string word-by-word instead of character by character
        for (int offset = 0; offset < wordLength; offset++) {
            for (int strIdx = offset; strIdx < s.length() - wordLength + 1; strIdx += wordLength) {
                // Sliding window
                // Instead of starting at the next index and testing all values again,
                // we remove the first char
                int idxToRemove = strIdx - words.length * wordLength;
                if (idxToRemove >= 0) {
                    String toRemove = s.substring(idxToRemove, idxToRemove + wordLength);
                    wordsFound.compute(toRemove, (k, v) -> (v == 1) ? null : v - 1);
                }

                // Include the next word in our sliding window
                String candidate = s.substring(strIdx, strIdx + wordLength);
                wordsFound.compute(candidate, (k, v) -> (v == null) ? 1 : v + 1);

                // In case our sliding window matches the words we need to find, we have a valid match
                if (wordsFound.equals(wordsCount)) {
                    // Store the index of the start of our sliding window
                    result.add(strIdx - ((words.length - 1) * wordLength));
                }
            }
            wordsFound = new HashMap<>();
        }
        return result;
    }
}
