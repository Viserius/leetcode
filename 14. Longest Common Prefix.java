// Runtime 0ms beats 100%
// Memory 41.04 MB beats 93.93%
class Solution {
   /**
     * Compute the longest common prefix of an array of strings
     *
     * @param strs array of strings
     * @return the longest common prefix
     */
    public String longestCommonPrefix(String[] strs) {
        // Compute the minimum string length, as this is the maximum prefix to check
        int maxPrefixLength = Integer.MAX_VALUE;
        for (String string : strs) maxPrefixLength = Math.min(maxPrefixLength, string.length());

        // Take the next prefix character
        char currentChar;
        for (int charIdxInWord = 0; charIdxInWord < maxPrefixLength; charIdxInWord++) {
            currentChar = strs[0].charAt(charIdxInWord);

            // And check whether the remainder of all words start with the same character
            for (int strIndex = 1; strIndex < strs.length; strIndex++) {

                // In case a word is found with a different prefix char, return the prefix found thus far
                if (strs[strIndex].charAt(charIdxInWord) != currentChar) {
                    return strs[strIndex].substring(0, charIdxInWord);
                }

            }

        }

        return strs[0].substring(0, maxPrefixLength);
    }
}
