// Runtime 0ms beats 100.00%
// Memory 41.37MB beats 82.12%
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
    public ListNode removeNthFromEnd(ListNode head, int n) {
        // The goal is to remove the nth last node from a singly linked list of nodes.
        // The problem is that n is counted backwards, but our list does not allow backwards traversal
        // Instead, we use a simple cache to associate the index of the node with the node itself
        List<ListNode> nodeCache = new ArrayList<>();
        ListNode curr = head;
        while (curr != null) {
            nodeCache.add(curr);
            curr = curr.next;
        }

        int nodeIdxToRemove = nodeCache.size()-n;
        // Remove the first node
        if(nodeIdxToRemove == 0) return head.next;

        // Remove the last node
        if(nodeIdxToRemove == nodeCache.size()-1) {
            nodeCache.get(nodeIdxToRemove-1).next = null;
            return head;
        }

        // Update the node before the one that is being removed
        nodeCache.get(nodeIdxToRemove-1).next = nodeCache.get(nodeIdxToRemove+1);

        return head;
    }
}
