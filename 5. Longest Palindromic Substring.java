class Solution {
    public String longestPalindrome(String s) {
        String longest = "";
        // For every character in the string, try to expand the palindrom from length=1 outwards
        for(int i = 0; i < s.length(); i++) {
            // such as a-b-a
            String longestForCenterPalindrome = findLongest(s, i, i);
            // such as a-b-b-a
            String longestForDoublePalindrome = findLongest(s, i, i+1);

            // update longest
            if(longestForCenterPalindrome.length() > longest.length())
                longest = longestForCenterPalindrome;
            if(longestForDoublePalindrome.length() > longest.length())
                longest = longestForDoublePalindrome;
        }
        return longest;
    }

    private String findLongest(String s, int i, int j) {
        if(j >= s.length() || s.charAt(i) != s.charAt(j))
            return "";
        while(i > 0 && j < s.length()-1 && s.charAt(i-1) == s.charAt(j+1)) {
            i--; j++;
        }
        return s.substring(i, j+1);
    }
}
