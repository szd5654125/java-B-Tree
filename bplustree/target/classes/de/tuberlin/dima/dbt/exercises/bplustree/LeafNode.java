package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.Arrays;

public class LeafNode extends Node {

    private String[] values;

    public LeafNode(int capacity) {
        this(new Integer[]{}, new String[]{}, capacity);
    }

    public LeafNode(Integer[] keys, String[] values, int capacity) {
        super(keys, capacity);
        assert keys.length == values.length;
        this.values = Arrays.copyOf(values, capacity);
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = Arrays.copyOf(values, BPlusTreeUtilities.CAPACITY);
    }

    public void setValueTemp(String[] values){
        this.values = Arrays.copyOf(values, values.length);
    }

    @Override
    public Object[] getPayload() {
        return getValues();
    }

    @Override
    public void setPayload(Object[] payload) {
        setValues((String[]) payload);
    }

    public String toString() {
        return new BPlusTreePrinter(this).toString();
    }

    public int size() {
        int count = 0;
        for (String value : values) {
            if (value == "" || value == null) {
                return count;
            }
            count++;
        }
        return count;
    }

    // LeafNode: 在数组前端插入键值对
    public void insertAtFront(Integer key, String value) {
        System.arraycopy(keys, 0, keys, 1, size());
        System.arraycopy(values, 0, values, 1, size());
        keys[0] = key;
        values[0] = value;
    }

    // LeafNode: 在数组末尾添加键值对
    public void add(Integer key, String value) {
        int s = size();
        keys[s] = key;
        values[s] = value;
    }

    // LeafNode: 删除指定位置的键值对
    public void remove(int index) {
        System.arraycopy(keys, index + 1, keys, index, size() - index - 1);
        System.arraycopy(values, index + 1, values, index, size() - index - 1);
        keys[size() - 1] = null; // 清除最后一个元素
        values[size() - 1] = null; // 清除最后一个元素
    }

    // LeafNode: 将数组中的元素向左移动一位，用于借用操作后的调整
    public void shiftLeft() {
        System.arraycopy(keys, 1, keys, 0, size() - 1);
        System.arraycopy(values, 1, values, 0, size() - 1);
        keys[size() - 1] = null; // 清除最后一个元素
        values[size() - 1] = null; // 清除最后一个元素
    }

    public void merge(LeafNode sibling) {
        int mergeSize = this.size() + sibling.size();
        assert mergeSize <= this.keys.length; // 确保不会超出容量

        System.arraycopy(sibling.getKeys(), 0, this.keys, this.size(), sibling.size());
        System.arraycopy(sibling.getValues(), 0, this.values, this.size(), sibling.size());
    }
}
