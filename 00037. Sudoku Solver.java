// NOTE: To challenge myself, this problem was completed 
// by implementing backtracking iteratively using a Stack,
// rather than the conventional way of using recursion.
// Tracking all possible values for each box in a stack is 
// in this case less performant than recursion.
//
// Runtime 32ms beats 5.08%
// Memory 45MB beats 5.01%
class Solution {
  private static final List<Character> ALLOWED_VALUES = List.of('1', '2', '3', '4', '5', '6', '7', '8', '9');

  public void solveSudoku(char[][] board) {
        Deque<Attempt> valuesToTry = new ArrayDeque<>();

        // Zolang er lege hokjes in de sudoku zijn, het volgende lege hokje proberen in te vullen
        Position position = getNextBlankPosition(board, 0, 0);
        while (position != null) {
            // Controleren welke geldige waarden we in dit hokje kunnen plaatsen
            Set<Character> allowedValues = getAllowedValuesForPosition(board, position);

            if (allowedValues.isEmpty()) {
                // Backtracking totdat nieuwe mogelijke waarde gevonden is...
                while (valuesToTry.getFirst().val == '.') {
                    var posToClear = valuesToTry.pop().position;
                    board[posToClear.row][posToClear.col] = '.';
                }
            } else {
                // Alle mogelijke waarden voor dit hokje op de stack zetten...
                valuesToTry.push(new Attempt(position, '.')); // backtracking token to reset
                for (Character allowedValue : allowedValues) valuesToTry.push(new Attempt(position, allowedValue));
            }

            // Mogelijke waarde voor hokje invullen
            Attempt attempt = valuesToTry.pop();
            board[attempt.position.row][attempt.position.col] = attempt.val;

            // Volgende hokje vinden om in te vullen
            position = getNextBlankPosition(board, attempt.position.row, attempt.position.col);
        }
    }

    private Set<Character> getAllowedValuesForPosition(char[][] board, Position position) {
        Set<Character> allowedValues = new HashSet<>(ALLOWED_VALUES);

        // Remove all elements in the same row as position
        for (int i = 0; i < 9; i++) {
            if (board[position.row][i] != '.') allowedValues.remove(board[position.row][i]);
            if (board[i][position.col] != '.') allowedValues.remove(board[i][position.col]);

            int gridRow = position.row / 3 * 3 + i / 3, gridCol = position.col / 3 * 3 + i % 3;
            if (board[gridRow][gridCol] != '.') allowedValues.remove(board[gridRow][gridCol]);
        }

        return allowedValues;
    }

    private Position getNextBlankPosition(char[][] board, int row, int col) {
        while (board[row][col] != '.') {
            if (col < 8) {
                col++;
            } else if (row < 8) {
                row++;
                col = 0;
            } else {
                return null;
            }
        }
        return new Position(row, col);
    }

    private void print(char[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                System.out.print(" " + board[row][col] + " |");
            }
            System.out.println();
        }
    }

    private record Position(int row, int col) {
        @Override
        public String toString() {
            return "board[" + row + "][" + col + ']';
        }
    }

    private record Attempt(Position position, char val) {
        @Override
        public String toString() {
            return position + "=" + val;
        }
    }
}
