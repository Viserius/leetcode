// Runtime 8ms beats ?%
// Memory 40.6MB beats 79.21%
/**
     * Divide the dividend by the divisor
     *
     * @param dividend the number to divide
     * @param divisor  the number to divide by
     * @return quotient rounded towards zero
     */
    public int divide(long dividend, long divisor) {

        // Simplify the solution by making both dividend and divisor positive.
        boolean isResultPositive = (dividend >= 0 && divisor >= 0) || (dividend < 0 && divisor < 0);
        dividend = (dividend < 0) ? -dividend : dividend;
        divisor = (divisor < 0) ? -divisor : divisor;

        if (divisor == 1)
            return toSignedInt(dividend, isResultPositive);

        // dividend >= 0, divisor >= 0
        int quotient = 0;
        while (dividend >= divisor) {
            // Prevent overflow, return early
            if (quotient == Integer.MAX_VALUE)
                return toSignedInt(quotient, isResultPositive);
            dividend -= divisor;
            quotient++;
        }

        return toSignedInt(quotient, isResultPositive);
    }

    /**
     * Convert a long value to an integer of given signage, taking overflow into
     * account
     *
     * @param val              the value to convert to an integer
     * @param isResultPositive whether the result should be positive or negative
     * @return an integer respecting the min and max boundaries to prevent overflow
     */
    private int toSignedInt(long val, boolean isResultPositive) {
        if (!isResultPositive)
            val = -val;
        if (val >= Integer.MAX_VALUE)
            return (isResultPositive) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        return (int) val;
    }
