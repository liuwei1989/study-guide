package com.gitbook.algorithm.linklist;

/**
 * @author liuwei56
 * @version 2019/9/4 4:58 PM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class RemoveNthNodeFromEnd {

    public static void main(String[] args) {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(8);
        Node n5 = new Node(5);
        Node n7 = new Node(7);
        Node n9 = new Node(9);
        n1.setNext(n2);
        n2.setNext(n3);
        n3.setNext(n5);
        n5.setNext(n7);
        n7.setNext(n9);
        Node node = removeNthNode(n1, 2);
        while (node != null) {
            System.out.println(node.getValue());
            node = node.getNext();
        }
    }

    public static Node removeNthNode(Node head, int n) {
        Node fast = head;
        while (n-- > 0) {
            fast = fast.getNext();
        }
        if (fast == null) {
            return head.getNext();
        }
        Node slow = head;
        while (fast.getNext() != null) {
            slow = slow.getNext();
            fast = fast.getNext();
        }
        slow.setNext(slow.getNext().getNext());
        return head;
    }

}
