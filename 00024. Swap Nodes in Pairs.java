// Runtime 0ms beats 100.00%
// Memory 41.15MB beats 41.31%
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
    public ListNode swapPairs(ListNode head) {
        // Base cases
        // empty list
        if (head == null)
            return null;
        // list of single item
        if (head.next == null)
            return head;

        // For the first swap, update the head
        // so it still points to the start of the newly swapped list.
        head = swap(head);

        // Maintain a pointer of the node after which to swap the next two values
        ListNode swapAfter = head.next;

        // Keep on swapping until reaching the end of the nodes
        while (swapAfter != null && swapAfter.next != null && swapAfter.next.next != null) {
            swapAfter.next = swap(swapAfter.next);
            swapAfter = swapAfter.next.next;
        }

        return head;
    }

    /**
     * Swap this node with the next node.
     *
     * @param first The first node to swap
     * @return The swapped node that is now in front
     */
    private ListNode swap(ListNode first) {
        ListNode second = first.next;
        first.next = second.next;
        second.next = first;
        return second;
    }
}
