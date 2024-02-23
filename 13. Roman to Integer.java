// Runtime 3ms beats 84%
// Memory 45 MB beats 75%
class Solution {
    private int total = 0;

    /**
     * Convert a String representing a roman numeral to a decimal-represented int
     *
     * @param s String representing a roman numeral
     * @return int representing the same value as a roman numeral
     */
    public int romanToInt(String s) {
        char[] charArray = s.toCharArray();
        int idx = extractRomanNumeral(charArray, 0, 'M', 1000);
        idx = extractRomanNumeralOnce(charArray, idx, 'C', 'M', 900);
        idx = extractRomanNumeral(charArray, idx, 'D', 500);
        idx = extractRomanNumeralOnce(charArray, idx, 'C', 'D', 400);
        idx = extractRomanNumeral(charArray, idx, 'C', 100);
        idx = extractRomanNumeralOnce(charArray, idx, 'X', 'C', 90);
        idx = extractRomanNumeral(charArray, idx, 'L', 50);
        idx = extractRomanNumeralOnce(charArray, idx, 'X', 'L', 40);
        idx = extractRomanNumeral(charArray, idx, 'X', 10);
        idx = extractRomanNumeralOnce(charArray, idx, 'I', 'X', 9);
        idx = extractRomanNumeral(charArray, idx, 'V', 5);
        idx = extractRomanNumeralOnce(charArray, idx, 'I', 'V', 4);
        extractRomanNumeral(charArray, idx, 'I', 1);
        return total;
    }

    /**
     * Extract as many roman numerals from sb and add to the sumSoFar
     *
     * @param s            String with roman numerals
     * @param idx          int index pointing to string position
     * @param numeral      char representing the roman numeral to extract
     * @param numeralValue int representing the decimal value of the roman numeral
     * @return sumSoFar with the roman numeral values added
     */
    private int extractRomanNumeral(char[] s, int idx, char numeral, int numeralValue) {
        while (idx < s.length && s[idx] == numeral) {
            idx++;
            total += numeralValue;
        }
        return idx;
    }

    /**
     * Extract at most one roman numeral from sb and add to the sumSoFar
     *
     * @param s            String with roman numerals
     * @param idx          int index pointing to string position
     * @param numeral1     char representing the first character of the roman numeral to extract
     * @param numeral2     char representing the second character of the roman numeral to extract
     * @param numeralValue int representing the decimal value of the roman numeral
     * @return sumSoFar with the roman numeral values added
     */
    private int extractRomanNumeralOnce(char[] s, int idx, char numeral1, char numeral2, int numeralValue) {
        if (idx + 1 < s.length && s[idx] == numeral1 && s[idx + 1] == numeral2) {
            idx += 2;
            total += numeralValue;
        }
        return idx;
    }
}
