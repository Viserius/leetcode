// Runtime 0ms beats 100.00%
// Memory 41.24MB beats 64.11%
class Solution {
    /**
     * Finds the index of the first occurrence of `needle` in `haystack`
     *
     * @param haystack String to search
     * @param needle   String to find in {@code haystack}
     * @return index of the first occurrence of `needle` in `haystack`
     */
    public int strStr(String haystack, String needle) {
        // for optimization outside
        int needleLength = needle.length(), maxValidIdx = haystack.length() - needleLength;

        for (int haystackIdx = 0; haystackIdx <= maxValidIdx; haystackIdx++) {
            for (int needleIdx = 0; haystack.charAt(haystackIdx + needleIdx) == needle.charAt(needleIdx); needleIdx++) {
                if (needleIdx == needleLength - 1) {
                    return haystackIdx;
                }
            }
        }
        return -1;
    }
}
