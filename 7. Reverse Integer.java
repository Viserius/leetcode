class Solution {
    public int reverse(int x) {
        int result = 0;
        int before = 0;
        while(x != 0) {
            before = result;
            
            // Add number from input to result
            result = (result*10) + x % 10;
            
            // Check if reverse operation is the same number as before -- if so, no int overflow
            if((result - (x % 10))/10 != before)
                return 0;
            
            // prepare next digit
            x /= 10;
        }
        return result;
    }
}
