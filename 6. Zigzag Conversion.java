// Runtime 5 ms beats 70.88%
// Memory 44.3 MB beats 56.31%
class Solution {
    public String convert(String s, int numRows) {
        // Initialize rows
        StringBuilder[] rowTexts = new StringBuilder[numRows];
        for(int i = 0; i < numRows; i++) {
            rowTexts[i] = new StringBuilder();
        }

        // Add every character to the corresponding row
        for(int i = 0; i < s.length(); i++) {
            int y = this.transformIdxToRow(i, numRows-1);
            rowTexts[y].append(s.charAt(i));
        };

        return String.join("", rowTexts);
    }

    int transformIdxToRow(int i, int maxRowIdx) {
        if(maxRowIdx == 0)
            return 0;
        if((i / maxRowIdx) % 2 == 0) {
            // Increasing
            return i % maxRowIdx;
        } else {
            // Decreasing
            return maxRowIdx - i % maxRowIdx;
        }
    }
}
