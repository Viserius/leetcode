// Runtime 4ms beats 100%
// Memory 43.18 MB beats 24.71%
class Solution {
    public boolean isPalindrome(int x) {
        // Negative numbers are never a palindrome
        if(x < 0)
            return false;

        int xCopy = x;

        // Construct a new int in reverse of the original int
        int reversed = 0;
        while (x > 0) {
            reversed = reversed * 10 + x % 10;
            x /= 10;
        }

        return xCopy == reversed;
    }
}
