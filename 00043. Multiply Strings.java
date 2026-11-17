// Note, implementation is far from optimal, as I compute the answer without using any multiplication whatsoever
// Runtime 22ms beats 5%
// Memory 47MB beats 5%
class Solution {
    public String multiply(String num1, String num2) {
        // Input ordering - num1 always smaller than num2
        if (num1.length() > num2.length() || (num1.length() == num2.length() && num1.compareTo(num2) > 0)) {
            String temp = num1;
            num1 = num2;
            num2 = temp;
        }

        String answer = "0";
        for (int num1Idx = 0; num1Idx < num1.length(); num1Idx++) {
            if (num1Idx != 0) answer = answer + "0";
            int num1Digit = num1.charAt(num1Idx) - '0';

            String result = "0";
            for (int i = 0; i < num1Digit; i++) {
                result = add(result, num2);
            }


            answer = add(answer, result);
        }

        return answer;
    }

    private String add(String num1, String num2) {
        // Input ordering - num1 always smaller than num2
        if (num1.length() > num2.length() || (num1.length() == num2.length() && num1.compareTo(num2) > 0)) {
            String temp = num1;
            num1 = num2;
            num2 = temp;
        }
        int digitFromRight = 0;
        int carry = 0;
        var sb = new StringBuilder();
        while (digitFromRight < num2.length()) {
            char num1Digit = num1.length() > digitFromRight ? num1.charAt(num1.length() - 1 - digitFromRight) : '0';
            char num2Digit = num2.charAt(num2.length() - 1 - digitFromRight);
            int result = num1Digit - '0' + num2Digit - '0' + carry;
            char resultDigit = (char) (result % 10 + '0');
            carry = result / 10;
            sb.append(resultDigit);
            digitFromRight++;
        }
        if (carry > 0) sb.append(carry);
        return sb.reverse().toString();
    }
}