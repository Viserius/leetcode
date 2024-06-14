// Runtime 1ms beats 100.00%
// Memory 43.94MB beats 92.21%
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
    public ListNode mergeKLists(ListNode[] lists) {
        return mergeKLists(lists, 0, lists.length - 1);
    }

    private ListNode mergeKLists(ListNode[] lists, int left, int right) {
        // Base case(s)
        // No lists to merge
        if (lists.length == 0)
            return null;
        // Only one list to merge
        if (left == right)
            return lists[left];
        // Only two lists to merge
        if (left == right - 1)
            return merge(lists[left], lists[right]);

        // Recursive case, more than 2 lists to merge
        // Split in the middle
        int mid = (left + right) / 2 + 1;
        ListNode leftListsMerged = mergeKLists(lists, left, mid - 1);
        ListNode rightListsMerged = mergeKLists(lists, mid, right);
        return merge(leftListsMerged, rightListsMerged);
    }

    private ListNode merge(ListNode first, ListNode second) {
        // When one of the lists is empty,
        // the other one is regarded as the merged list
        if (first == null)
            return second;
        if (second == null)
            return first;

        // Keep grabbing from both lists until one of them becomes empty
        ListNode head = null, tail = null;
        while (first != null && second != null) {
            // Grab the minimum node of the two lists and move the pointer
            ListNode min;
            if (first.val < second.val) {
                min = first;
                first = first.next;
            } else {
                min = second;
                second = second.next;
            }
            // Append this minimum value to the resulting list
            if (head == null) {
                head = min;
                tail = min;
            } else {
                tail.next = min;
                tail = tail.next;
            }
        }

        // If one of the lists is consumed, set the tail to the other one.
        if (first == null) {
            tail.next = second;
        } else {
            tail.next = first;
        }

        return head;
    }
}
