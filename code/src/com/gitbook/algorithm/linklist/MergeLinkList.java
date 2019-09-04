package com.gitbook.algorithm.linklist;

/**
 * @author liuwei56
 * @version 2019/9/4 4:30 PM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class MergeLinkList {

    public static void main(String[] args) {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(8);
        n1.setNext(n2);
        n2.setNext(n3);

        Node n5 = new Node(5);
        Node n7 = new Node(7);
        Node n9 = new Node(9);
        n5.setNext(n7);
        n7.setNext(n9);

        Node merge = merge(n1, n5);
        while (merge != null) {
            System.out.println(merge.getValue());
            merge = merge.getNext();
        }
    }

    public static Node merge(Node node1, Node node2) {
        if (node1 == null) {
            return node2;
        }
        if (node2 == null) {
            return node1;
        }
        if (node1.getValue() < node2.getValue()) {
            node1.setNext(merge(node1.getNext(), node2));
            return node1;
        } else {
            node2.setNext(merge(node1, node2.getNext()));
            return node2;
        }
    }
}
