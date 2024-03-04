package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InnerNode extends Node {

    private Node[] children;

    public InnerNode(int capacity) {
        this(new Integer[] {}, new Node[] {null}, capacity);
    }

    public InnerNode(Integer[] keys, Node[] children, int capacity) {
        super(keys, capacity);
        assert keys.length == children.length - 1;
        this.children = Arrays.copyOf(children, capacity + 1);
    }

    public Node[] getChildren() {
        return children;
    }

    public void setChildren(Node[] children) {
        this.children = Arrays.copyOf(children, this.children.length);
    }

    @Override
    public Object[] getPayload() {
        return getChildren();
    }

    @Override
    public void setPayload(Object[] payload) {
        setChildren((Node[]) payload);
    }

    public String toString() {
        String keyList = Arrays.stream(keys).map(String::valueOf)
                               .collect(Collectors.joining(", "));
        String childrenList = Arrays.stream(children).map(String::valueOf)
                                    .collect(Collectors.joining(", "));
        return "keys: [" + keyList + "]; " + "children: [" + childrenList + "]";
    }

    public int size() {
        int count = 0;
        for (Integer key : keys) {
            if (key != null) {
                count++;
            }
        }
        return count;
    }

    // InnerNode: 在数组前端插入键和子节点
    public void insertAtFront(Integer key, Node child) {
        System.arraycopy(keys, 0, keys, 1, size());
        System.arraycopy(children, 0, children, 1, size() + 1); // 子节点比键多一个
        keys[0] = key;
        children[0] = child;
    }

    // InnerNode: 在数组末尾添加键和子节点
    public void add(Integer key, Node child) {
        int s = size();
        keys[s] = key;
        children[s + 1] = child; // 注意子节点的位置
    }

    // InnerNode: 删除指定位置的键和子节点
    public void remove(int index) {
        System.arraycopy(keys, index + 1, keys, index, size() - index - 1);
        System.arraycopy(children, index + 1, children, index, size() - index);
        keys[size() - 1] = null; // 清除最后一个键
        children[size()] = null; // 清除最后一个子节点
    }

    // InnerNode: 将数组中的元素向左移动一位，用于借用操作后的调整
    public void shiftLeft() {
        System.arraycopy(keys, 1, keys, 0, size() - 1);
        System.arraycopy(children, 1, children, 0, size()); // 子节点比键多一个
        keys[size() - 1] = null; // 清除最后一个键
        children[size()] = null; // 清除最后一个子节点
    }

    public void merge(Integer separatorKey, InnerNode sibling) {
        int mergePoint = this.size();
        this.keys[mergePoint] = separatorKey; // 添加分隔键

        // 合并键和子节点
        System.arraycopy(sibling.getKeys(), 0, this.keys, mergePoint + 1, sibling.size());
        System.arraycopy(sibling.getChildren(), 0, this.children, mergePoint + 1, sibling.size() + 1); // 子节点比键多一个
    }

    public void removeChildAt(int childIndex) {
        if (childIndex > 0) { // 如果不是移除第一个子节点，还需要移除一个键
            System.arraycopy(this.keys, childIndex, this.keys, childIndex - 1, this.size() - childIndex);
            this.keys[this.size() - 1] = null; // 清除最后一个键
        }
        // 移除子节点
        System.arraycopy(this.children, childIndex + 1, this.children, childIndex, this.children.length - childIndex - 1);
        this.children[this.children.length - 1] = null; // 清除最后一个子节点
    }
}
