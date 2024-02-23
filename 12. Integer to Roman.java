// Runtime 3ms beats 98%
// Memory 44 MB beats 97%
class Solution {
    /**
     * Convert a decimal integer to a roman numeral
     *
     * @param num decimal integer to convert
     * @return roman numeral representing the same value as the decimal integer
     */
    public String intToRoman(int num) {
        StringBuilder output = new StringBuilder();
        num = appendRomans(output, num, 1000, "M");
        num = appendRomans(output, num, 900, "CM");
        num = appendRomans(output, num, 500, "D");
        num = appendRomans(output, num, 400, "CD");
        num = appendRomans(output, num, 100, "C");
        num = appendRomans(output, num, 90, "XC");
        num = appendRomans(output, num, 50, "L");
        num = appendRomans(output, num, 40, "XL");
        num = appendRomans(output, num, 10, "X");
        num = appendRomans(output, num, 9, "IX");
        num = appendRomans(output, num, 5, "V");
        num = appendRomans(output, num, 4, "IV");
        appendRomans(output, num, 1, "I");
        return output.toString();
    }

    /**
     * For a given value and roman numeral, append the roman numeral as often as possible to a StringBuilder
     *
     * @param sb              StringBuilder to append the roman numeral to
     * @param numberToProcess int to extract as much roman numerals of
     * @param decimal         int value of the roman numeral
     * @param roman           String representation of the roman numeral
     * @return original StringBuilder appended with as much roman numerals as in the numberToProcess
     */
    private int appendRomans(StringBuilder sb, int numberToProcess, int decimal, String roman) {
        int occurences = Math.max(0, numberToProcess / decimal);
        sb.append(String.valueOf(roman).repeat(occurences));
        return numberToProcess - occurences * decimal;
    }
}
