// Runtime 1ms beats 100%
// Memory 41.1 MB beats 94.55%
class Solution {
    public int myAtoi(String s) {
        int i = 0, c, result = 0, sign = 1;

        // Ignore whitespace
        while(i < s.length()) {
            c = s.charAt(i);
            if(c == ' ') {
                i++;
            } else {
                break;
            }
        }

        // Check positive or negative
        if(i < s.length() && s.charAt(i) == '+') {
            i++;
        } else if(i < s.length() && s.charAt(i) == '-') {
            sign = -1;
            i++;
        }

        // Increment result
        while(i < s.length()) {
            c = s.charAt(i);
            if(c < '0' || c > '9')
                break;

            int before = result;
            result = result * 10 + sign * (c - '0');

            // Check for overflow
            if((result - sign * (c - '0')) / 10 != before || (result > 0 && sign == -1) || (result < 0 && sign == 1)) {
                return (sign == 1) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
            }

            i++;
        }

        return result;
    }
}
