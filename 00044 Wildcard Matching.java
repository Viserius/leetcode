// Runtime 17ms beats 66%
// Memory 48MB beats 39.6%
class Solution {
    public boolean isMatch(String s, String p) {
        // Vereenvoudig p: meerdere opeenvolgende '*' worden teruggebracht naar één '*'
        // Bijvoorbeeld: aa******a -> aa*a
        p = sanitize(p);

        Boolean[][] memo = new Boolean[s.length() + 1][p.length() + 1];
        return isMatch(memo, s, 0, p, 0);
    }

    private String sanitize(String p) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < p.length(); i++) {
            char current = p.charAt(i);

            if (current != '*' || result.isEmpty() || result.charAt(result.length() - 1) != '*') {
                result.append(current);
            }
        }

        return result.toString();
    }

    private boolean isMatch(Boolean[][] memo, String s, int sIndex, String p, int pIndex) {
        if (memo[sIndex][pIndex] != null) {
            return memo[sIndex][pIndex];
        }

        // Zowel s als p zijn volledig geconsumeerd: match
        if (sIndex >= s.length() && pIndex >= p.length()) {
            memo[sIndex][pIndex] = true;
            return true;
        }

        // Pattern is volledig geconsumeerd, maar s niet: geen match
        if (pIndex >= p.length()) {
            memo[sIndex][pIndex] = false;
            return false;
        }

        // String is volledig geconsumeerd; alleen '*' kan nog matchen
        if (sIndex >= s.length() && p.charAt(pIndex) != '*') {
            memo[sIndex][pIndex] = false;
            return false;
        }

        boolean result = switch (p.charAt(pIndex)) {
            case '*' -> {
                // '*' matcht geen karakter: consumeer alleen de '*'
                boolean matchesEmpty = isMatch(memo, s, sIndex, p, pIndex + 1);

                // '*' matcht één karakter: consumeer één karakter van s, maar behoud de '*'
                boolean matchesCharacter = sIndex < s.length()
                        && isMatch(memo, s, sIndex + 1, p, pIndex);

                yield matchesEmpty || matchesCharacter;
            }

            case '?' -> {
                // '?' matcht precies één karakter
                yield isMatch(memo, s, sIndex + 1, p, pIndex + 1);
            }

            default -> {
                // Normaal karakter moet exact overeenkomen
                if (s.charAt(sIndex) != p.charAt(pIndex)) {
                    yield false;
                }

                yield isMatch(memo, s, sIndex + 1, p, pIndex + 1);
            }
        };

        memo[sIndex][pIndex] = result;
        return result;
    }
}