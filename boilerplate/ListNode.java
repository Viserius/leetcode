/**
 * Definition for singly-linked list — identiek aan de LeetCode-definitie, zodat code
 * 1-op-1 tussen IntelliJ en LeetCode gekopieerd kan worden.
 * <p>
 * Alleen {@link #toString()} is extra (handig in de debugger); LeetCode heeft die niet.
 */
public class ListNode {
    int val;
    ListNode next;

    ListNode() {}

    ListNode(int val) { this.val = val; }

    ListNode(int val, ListNode next) { this.val = val; this.next = next; }

    @Override
    public String toString() {
        return Harness.pretty(this);
    }
}
