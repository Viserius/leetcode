/**
 * Definition for a binary tree node — identiek aan de LeetCode-definitie, zodat code
 * 1-op-1 tussen IntelliJ en LeetCode gekopieerd kan worden.
 * <p>
 * Alleen {@link #toString()} is extra (level-order zoals LeetCode, handig in de debugger).
 */
public class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode() {}

    TreeNode(int val) { this.val = val; }

    TreeNode(int val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return Harness.pretty(this);
    }
}
