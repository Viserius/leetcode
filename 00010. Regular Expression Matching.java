// Runtime 1ms beats 100%
// Memory 41.2 MB beats 45.36%
class Solution {
 private final byte[][] lookUp = new byte[21][21];
    private char[] s;
    private char[] p;

    /**
     * Compute whether a string satisfies a given regex
     * isMatch("", "") -> true, terminal case
     * isMatch("a", "") -> false, terminal case
     * isMatch("aa", "a")
     * -> isMatch("a", "")
     * -> false
     * isMatch("aa", "a*")
     * -> isMatch("a", "a*") || isMatch("aa", "")
     * -> isMatch("", "a*") || isMatch("a", "") || false
     * -> isMatch("", "") || false || false
     * -> true || false || false
     * -> true
     * isMatch("ab", ".*")
     * -> isMatch("b", ".*") || isMatch("ab", "")
     * -> isMatch("", ".*") || isMatch("b", "") || false
     * -> isMatch("", "") || false || false
     * -> true || false || false
     * -> true
     *
     * @param s String to match
     * @param p Regex pattern to match against
     * @return true if a match, false otherwise
     */
    public boolean isMatch(String s, String p) {
        this.s = s.toCharArray();
        this.p = p.toCharArray();
        return isMatchRecursive(0, 0);
    }

    private boolean isMatchRecursive(int sStart, int pStart) {
        // Base case
        // We have a match if there are no more characters to match and the pattern is fulfilled
        if (s.length - sStart <= 0 && p.length - pStart <= 0)
            return true;
        // We have no match if there are still characters to match against no pattern
        if (p.length - pStart <= 0)
            return false;

        // Return answer if memoized
        if (lookUp[sStart][pStart] != 0)
            return lookUp[sStart][pStart] == 1;

        // Recursive case
        // When s is empty, we only need to reduce & check if the right-hand side can be omitted
        // and the remainder without the wildcard is also a match
        if (s.length - sStart <= 0)
            return this.cacheReturn(sStart, pStart,
                    p.length - pStart >= 2 && p[pStart + 1] == '*'
                            && isMatchRecursive(sStart, pStart + 2));

        // When s is non-empty
        // Try to find a match in all combinations by reducing the left- and right-hand side.
        // When * in pattern p
        if (p.length - pStart >= 2 && p[pStart + 1] == '*') {
            if (p[pStart] == '.')
                return this.cacheReturn(sStart, pStart,
                        isMatchRecursive(sStart, pStart + 2) ||
                                isMatchRecursive(sStart + 1, pStart));
            else
                return this.cacheReturn(sStart, pStart,
                        isMatchRecursive(sStart, pStart + 2)
                                || isMatchRecursive(sStart + 1, pStart) && s[sStart] == p[pStart]);
        }

        // When . in pattern p
        if (p[pStart] == '.')
            return this.cacheReturn(sStart, pStart,
                    isMatchRecursive(sStart + 1, pStart + 1));


        // Just match and consume both without special characters
        return this.cacheReturn(sStart, pStart,
                s[sStart] == p[pStart] && isMatchRecursive(sStart + 1, pStart + 1));
    }

    private boolean cacheReturn(int sStart, int pStart, boolean val) {
        lookUp[sStart][pStart] = (val) ? (byte) 1 : -1;
        return val;
    }
}
