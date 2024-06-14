// Runtime 0ms beats 100.00%
// Memory 42.20MB beats 80.92%
/**
 * Definition for singly-linked list.
 * public class ListNode {
 * int val;
 * ListNode next;
 * ListNode() {}
 * ListNode(int val) { this.val = val; }
 * ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 * }
 */
class Solution {
    public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
        // Ensure both lists have nodes, otherwise return the other (possibly null as
        // well)
        if (list1 == null)
            return list2;
        if (list2 == null)
            return list1;

        // Build the list nodes by comparing both lists
        ListNode head = new ListNode();
        ListNode current = head;
        while (list1 != null && list2 != null) {
            if (list1.val <= list2.val) {
                current.val = list1.val;
                list1 = list1.next;
            } else {
                current.val = list2.val;
                list2 = list2.next;
            }

            // A new node needs to be added to the list
            if (list1 != null && list2 != null) {
                current.next = new ListNode();
                current = current.next;
            }
        }

        // The one that ends up being null is discarded,
        // and the other one is the remainder of the chain
        if (list1 == null)
            current.next = list2;
        else
            current.next = list1;

        return head;
    }
}
