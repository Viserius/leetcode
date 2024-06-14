// Runtime 1ms beats 98.62%
// Memory 41.31MB beats 60.30%
class Solution {
    public boolean isValid(String s) {
        Stack<Character> parenthesisTracker = new Stack<>();
        HashMap<Character, Character> parenthesisTypes = new HashMap<>();
        parenthesisTypes.put(')', '(');
        parenthesisTypes.put(']', '[');
        parenthesisTypes.put('}', '{');
        for (int i = 0; i < s.length(); i++) {
            if (parenthesisTypes.containsKey(s.charAt(i))) {
                if (parenthesisTracker.empty() || parenthesisTracker.pop() != parenthesisTypes.get(s.charAt(i)))
                    return false;
            } else {
                parenthesisTracker.add(s.charAt(i));
            }
        }

        return parenthesisTracker.empty() ? true : false;
    }
}
