// Runtime 3ms beats 56.06%
// Memory 44.11MB beats 79.49%
class Solution {
    public boolean isValidSudoku(char[][] board) {
        return validateRows(board)
                && validateCols(board)
                && validateBoxes(board);
    }

    /**
     * A board is valid in case no row contains two equivalent numbers
     *
     * @param board 9x9 Sudoku
     * @return true when all rows are valid
     */
    private boolean validateRows(char[][] board) {
        Set<Character> chars;
        for (int row = 0; row < 9; row++) {
            chars = new HashSet<>();
            for (int col = 0; col < 9; col++) {
                if (board[row][col] != '.' && !chars.add(board[row][col]))
                    return false;
            }
        }
        return true;
    }

    /**
     * A board is valid in case no column contains two equivalent numbers
     *
     * @param board 9x9 Sudoku
     * @return true when all columns are valid
     */
    private boolean validateCols(char[][] board) {
        Set<Character> chars;
        for (int cols = 0; cols < 9; cols++) {
            chars = new HashSet<>();
            for (int row = 0; row < 9; row++) {
                if (board[row][cols] != '.' && !chars.add(board[row][cols]))
                    return false;
            }
        }
        return true;
    }

    /**
     * A board is valid if no 3x3 boxes contain two equivalent numbers
     *
     * @param board 9x9 Sudoku
     * @return true when all 3x3 boxes of sudoku are valid
     */
    private boolean validateBoxes(char[][] board) {
        Set<Character> chars;
        for (int box = 0; box < 9; box++) {
            chars = new HashSet<>();
            for (int num = 0; num < 9; num++) {
                int row = 3 * (box / 3) + num / 3;
                int col = 3 * (box % 3) + num % 3;
                if (board[row][col] != '.' && !chars.add(board[row][col]))
                    return false;
            }
        }
        return true;
    }
}
