package com.gitbook.algorithm.linklist;

/**
 * @author liuwei56
 * @version 2019/9/4 10:57 AM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class ReverseLinkList {

    public static void main(String[] args) {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        n1.setNext(n2);
        n2.setNext(n3);
        n3.setNext(n4);
//        Node node = reverseList(n1);
        Node node = reverse(n1);
        while (node != null) {
            System.out.print(node.getValue() + ">");
            node = node.getNext();
        }
    }

    public static Node reverseList(Node node) {
        Node pre = null;
        Node next;
        while (node != null) {
            next = node.getNext();
            node.setNext(pre);
            pre = node;
            node = next;
        }
        return pre;
    }

    public static Node reverse(Node node) {
        if (node == null || node.getNext() == null) {
            return node;
        }
        Node tempNode = node.getNext();
        Node newNode = reverse(node.getNext());
        tempNode.setNext(node);
        node.setNext(null);
        return newNode;
    }

}
