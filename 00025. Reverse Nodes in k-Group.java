// Runtime 0 ms beats 100.00%
// Memory 44.09 MB beats 63.93%
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
    public ListNode reverseKGroup(ListNode head, int k) {
        // No need to swap anything when k == 1
        if (k == 1)
            return head;

        ListNode resultHead = null, previousTail = null, previousHead;
        while (tailKExists(head, k - 1)) {
            // Swap k elements and do some maintenance such that the pointers line up
            // correctly
            previousHead = head;
            if (resultHead == null)
                resultHead = this.swap(head, k - 1);
            else
                previousTail.next = this.swap(head, k - 1);
            head = head.next;
            previousTail = previousHead;
        }

        return resultHead;
    }

    /**
     * For a given node, obtain the node k-th element
     *
     * @param head Head of a list
     * @param k    The k-th element to return
     * @return the element #k (1-indexed)
     */
    private boolean tailKExists(ListNode head, int k) {
        if (head == null)
            return false;
        if (k == 0)
            return true;
        return tailKExists(head.next, k - 1);
    }

    /**
     * Swap the ordering of k ListNodes
     *
     * @param firstToSwap The first node to start swapping
     * @param k           The amount of nodes to swap
     * @return The head of the new list
     */
    private ListNode swap(ListNode firstToSwap, int k) {
        // Base case, the last item to swap
        if (k == 1)
            return swapWithNext(firstToSwap);

        // Recursive case, k > 1
        ListNode newHead = this.swap(firstToSwap.next, k - 1);
        swapWithNext(firstToSwap);
        return newHead;
    }

    /**
     * Swap a node with the node next to it.
     *
     * @param firstToSwap The first node to start swapping
     * @return The head of the new list
     */
    private ListNode swapWithNext(ListNode firstToSwap) {
        ListNode second = firstToSwap.next;
        firstToSwap.next = firstToSwap.next.next;
        second.next = firstToSwap;
        return second;
    }
}
