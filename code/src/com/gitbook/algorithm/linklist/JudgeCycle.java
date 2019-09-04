package com.gitbook.algorithm.linklist;

/**
 * @author liuwei56
 * @version 2019/9/4 3:54 PM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class JudgeCycle {

    public static void main(String[] args) {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        n1.setNext(n2);
        n2.setNext(n3);
        n3.setNext(n4);
        n4.setNext(n2);

        System.out.println(judgeCycle(n1));

    }

    public static boolean judgeCycle(Node node) {
        if (node == null) {
            return false;
        }
        Node p = node;
        Node q = node;
        while (p.getNext() != null && q.getNext().getNext() != null) {
            p = p.getNext();
            q = q.getNext().getNext();
            if (p == q) {
                return true;
            }
        }
        return false;
    }

}
