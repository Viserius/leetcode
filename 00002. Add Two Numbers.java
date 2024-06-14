// Runtime 2ms beats 60.29%
// Memory 42.9MB beats 88.97%
/**
 * Definition for singly-linked list.
 * public class ListNode {
 *     int val;
 *     ListNode next;
 *     ListNode() {}
 *     ListNode(int val) { this.val = val; }
 *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode result = new ListNode();
        ListNode current = result;
        while (true) {
            // Compute sum and carry
            int l1Val = (l1 == null) ? 0 : l1.val;
            int l2Val = (l2 == null) ? 0 : l2.val;
            int carry = (current.val + l1Val + l2Val) / 10;
            current.val = (current.val + l1Val + l2Val) % 10;

            // Move to the next elements in the list
            l1 = (l1 == null) ? l1 : l1.next;
            l2 = (l2 == null) ? l2 : l2.next;

            // Base case
            if (l1 == null && l2 == null) {
                // add carry if needed
                if (carry != 0)
                    current.next = new ListNode(carry);
                return result;
            }

            // Prepare next node with carry
            current.next = new ListNode(carry);
            current = current.next;
        }
    }
}
