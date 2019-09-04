package com.gitbook.algorithm.linklist;

/**
 * @author liuwei56
 * @version 2019/9/4 4:31 PM
 * @description 功能描述
 * @see
 * @since 1.0
 */
public class Node {
    private int value;
    private Node next;

    public Node(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}
