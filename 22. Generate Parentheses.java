// Runtime 1ms beats 83.79%
// Memory 42.85MB beats 73.50%
class Solution {
    public List<String> generateParenthesis(int n) {
        List<String> result = new ArrayList<>();
        this.generateParenthesisRecursive(result, 1, 0, n, "(");
        return result;
    }

    public void generateParenthesisRecursive(List<String> result,
            int openParenthesis, int closedParenthesis,
            int goalParenthesis, String pair) {
        // Base case -- no more parenthesis to open, just one to close
        if (openParenthesis == goalParenthesis && closedParenthesis == goalParenthesis - 1) {
            result.add(pair + ')');
            return;
        }

        // Recursive case
        if (openParenthesis < goalParenthesis) {
            this.generateParenthesisRecursive(result, openParenthesis + 1,
                    closedParenthesis, goalParenthesis, pair + '(');
        }
        if (closedParenthesis < goalParenthesis && closedParenthesis < openParenthesis) {
            this.generateParenthesisRecursive(result, openParenthesis,
                    closedParenthesis + 1, goalParenthesis, pair + ')');
        }
    }
}
