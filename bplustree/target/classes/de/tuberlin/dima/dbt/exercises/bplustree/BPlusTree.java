package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Implementation of a B+ tree.
 * <p>
 * The capacity of the tree is given by the capacity argument to the
 * constructor. Each node has at least {capacity/2} and at most {capacity} many
 * keys. The values are strings and are stored at the leaves of the tree.
 * <p>
 * For each inner node, the following conditions hold:
 * <p>
 * {pre}
 * Integer[] keys = innerNode.getKeys();
 * Node[] children = innerNode.getChildren();
 * {pre}
 * <p>
 * - All keys in {children[i].getKeys()} are smaller than {keys[i]}.
 * - All keys in {children[j].getKeys()} are greater or equal than {keys[i]}
 * if j > i.
 */
public class BPlusTree {

    ///// Implement these methods
    private void insertIntoLeafNode(Integer key, String value, LeafNode node, Deque<InnerNode> parents) {
        int index = 0;
        while (index < node.getKeys().length && (node.getKeys()[index] != null && key.compareTo(node.getKeys()[index]) > 0)) {
            index++;
        }

        Integer[] newKeys = new Integer[node.size() + 1];
        String[] newValues = new String[node.size() + 1];

        System.arraycopy(node.getKeys(), 0, newKeys, 0, index);
        newKeys[index] = key;
        System.arraycopy(node.getKeys(), index, newKeys, index + 1, node.size() - index);

        System.arraycopy(node.getValues(), 0, newValues, 0, index);
        newValues[index] = value;
        System.arraycopy(node.getValues(), index, newValues, index + 1, node.size() - index);

        node.setKeysTemp(newKeys);
        node.setValueTemp(newValues);

        /**
         * 调整node
         * 此时node.size()==node.keys.length
         * 1. node.keys.length<capacity
         * 2. node.keys.length>capacity
         * */
        if (node.size() < capacity) {
            node.setKeys(node.keys);
            node.setValues(node.getValues());
        }else if (node.size() > capacity) {
            LeafNode newLeafNode = splitLeafNode(node);
            InnerNode parent = updateParents(parents);
            // Corrected: Insert new key from the split leaf into the parent node
            insertIntoInnerNode(newLeafNode.getKeys()[0], newLeafNode, parent, parents);
        }
    }

    private void insertIntoInnerNode(Integer key, Node child, InnerNode node, Deque<InnerNode> parents) {
        int index = 0;
        while (index < node.getKeys().length && (node.getKeys()[index] != null && key.compareTo(node.getKeys()[index]) > 0)) {
            index++;
        }
        // Shift keys and children to make space for the new entry
        System.arraycopy(node.getKeys(), index, node.getKeys(), index + 1, node.getKeys().length - index - 1);
        System.arraycopy(node.getChildren(), index + 1, node.getChildren(), index + 2, node.getChildren().length - index - 2);
        // Insert new key and child
        node.getKeys()[index] = key;
        node.getChildren()[index + 1] = child;
        // Split inner node if necessary
        if (node.getKeys().length > capacity) {
            InnerNode newInnerNode = splitInnerNode(node);
            InnerNode parent = updateParents(parents);
            insertIntoInnerNode(newInnerNode.getKeys()[0], newInnerNode, parent, parents);
        }
    }


    /**
     * Insert the key/value pair into the B+ tree.
     */
    public void insert(Integer key, String value) {
        Deque<InnerNode> parents = new LinkedList<>();
        LeafNode leafNode = findLeafNode(key, parents);
        insertIntoLeafNode(key, value, leafNode, parents);
    }


    private InnerNode splitInnerNode(InnerNode node) {
        int midIndex = node.getKeys().length / 2;
        Integer[] newKeys = Arrays.copyOfRange(node.getKeys(), midIndex + 1, node.getKeys().length);
        Node[] newChildren = Arrays.copyOfRange(node.getChildren(), midIndex + 1, node.getChildren().length);
        InnerNode newInnerNode = new InnerNode(newKeys, newChildren, capacity);

        // Corrected: Update the parent keys with the first key of the new inner node
        for (int i = midIndex; i < node.getKeys().length; i++) {
            node.getKeys()[i] = null;
        }

        return newInnerNode;
    }


    private LeafNode splitLeafNode(LeafNode node) {
        int midIndex = node.getKeys().length / 2;
        Integer[] newKeys = Arrays.copyOfRange(node.getKeys(), midIndex, node.getKeys().length);
        String[] newValues = Arrays.copyOfRange(node.getValues(), midIndex, node.getValues().length);
        LeafNode newLeafNode = new LeafNode(newKeys, newValues, capacity);
        // Corrected: Remove the keys that have been moved to the new leaf node
        node.setKeys(Arrays.copyOf(node.getKeys(), midIndex));
        node.setValues(Arrays.copyOf(node.getValues(), midIndex));

        // Corrected: Return the new leaf node
        return newLeafNode;
    }


    ///// Public API
    ///// These can be left unchanged

    /**
     * Lookup the value stored under the given key.
     *
     * @return The stored value, or {null} if the key does not exist.
     */
    public String lookup(Integer key) {
        LeafNode leafNode = findLeafNode(key);
        return lookupInLeafNode(key, leafNode);
    }

    private String lookupInLeafNode(Integer key, LeafNode leafNode) {
        int index = 0;
        while (index < leafNode.getKeys().length && (key == null || key.compareTo(leafNode.getKeys()[index]) >= 0)) {
            if (key != null && key.equals(leafNode.getKeys()[index])) {
                return leafNode.getValues()[index];
            }
            index++;
        }
        return null;
    }


    private LeafNode findLeafNode(Integer key) {
        Node currentNode = root;
        while (currentNode instanceof InnerNode) {
            InnerNode innerNode = (InnerNode) currentNode;
            int index = 0;
            while (index < innerNode.getKeys().length - 1 && (innerNode.getKeys()[index] != null && (key == null || key.compareTo(innerNode.getKeys()[index]) >= 0))) {
                index++;
            }

            // 添加额外的检查，确保 index 不超过数组的有效范围
            if (index < innerNode.getChildren().length) {
                currentNode = innerNode.getChildren()[index];
            } else {
                // 如果 index 超过了数组的有效范围，说明当前节点没有对应的子节点，可以退出循环
                break;
            }
        }
        return (LeafNode) currentNode;
    }


    private LeafNode findLeafNode(Integer key, Deque<InnerNode> parents) {
        return findLeafNode(key, root, parents);
    }

    private LeafNode findLeafNode(Integer key, Node node, Deque<InnerNode> parents) {
        if (node instanceof LeafNode) {
            return (LeafNode) node;
        } else {
            InnerNode innerNode = (InnerNode) node;
            if (parents != null) {
                parents.push(innerNode);
            }
            int index = 0;
//            while (index < innerNode.getKeys().length && key.compareTo(innerNode.getKeys()[index]) >= 0) {
//                index++;
//            }
            while (index < innerNode.getKeys().length && (innerNode.getKeys()[index] != null && (key == null || key.compareTo(innerNode.getKeys()[index]) >= 0))) {
                index++;
            }
            return findLeafNode(key, innerNode.getChildren()[index], parents);
        }
    }


    /**
     * Delete the key/value pair from the B+ tree.
     *
     * @return The original value, or {null} if the key does not exist.
     */
    public String delete(Integer key) {
        Deque<InnerNode> parents = new LinkedList<>();
        LeafNode leafNode = findLeafNode(key, parents);
        String deletedValue = deleteFromLeafNode(key, leafNode);

        // 如果删除后 LeafNode 小于 最小节点数
        if (leafNode.size() < (int) Math.ceil(this.capacity / 2.0)) {
            updateParentsAfterDeletion(leafNode, parents);
        }

        return deletedValue;
    }

    private String deleteFromLeafNode(Integer key, LeafNode node) {
        int index = 0;
        while (index < node.getKeys().length && key > node.getKeys()[index]) {
            index++;
        }
        if (index < node.getKeys().length && key.equals(node.getKeys()[index])) {
            String deletedValue = node.getValues()[index];
            Integer[] newKeys = new Integer[node.getKeys().length - 1];
            String[] newValues = new String[node.getValues().length - 1];
            System.arraycopy(node.getKeys(), 0, newKeys, 0, index);
            System.arraycopy(node.getKeys(), index + 1, newKeys, index, node.getKeys().length - index - 1);
            System.arraycopy(node.getValues(), 0, newValues, 0, index);
            System.arraycopy(node.getValues(), index + 1, newValues, index, node.getValues().length - index - 1);
            node.setKeys(newKeys);
            node.setValues(newValues);
            return deletedValue;
        }
        return null;
    }




    private void updateParentsAfterDeletion(Node node, Deque<InnerNode> parents) {
        if (node.size() >= Math.ceil(capacity / 2.0) || parents.isEmpty()) {
            // 如果节点有足够的键或没有父节点（根节点），则不需要进一步操作
            return;
        }

        InnerNode parent = parents.pop();
        int childIndex = Arrays.asList(parent.getChildren()).indexOf(node);
        Node leftSibling = childIndex > 0 ? parent.getChildren()[childIndex - 1] : null;
        Node rightSibling = childIndex < parent.getChildren().length - 1 ? parent.getChildren()[childIndex + 1] : null;

        if (leftSibling != null && leftSibling.size() > Math.ceil(capacity / 2.0)) {
            // 从左兄弟借用
            borrowFromSibling(node, leftSibling, parent, childIndex, true);
        } else if (rightSibling != null && rightSibling.size() > Math.ceil(capacity / 2.0)) {
            // 从右兄弟借用
            borrowFromSibling(node, rightSibling, parent, childIndex, false);
        } else {
            // 合并节点
            Node sibling = leftSibling != null ? leftSibling : rightSibling;
            boolean isLeftSibling = leftSibling != null;
            mergeNodes(node, sibling, parent, childIndex, isLeftSibling);
            // 递归向上更新父节点
            updateParentsAfterDeletion(parent, parents);
        }
    }

    private void borrowFromSibling(Node node, Node sibling, InnerNode parent, int childIndex, boolean isLeftSibling) {
        if (node instanceof LeafNode && sibling instanceof LeafNode) {
            LeafNode targetNode = (LeafNode) node;
            LeafNode siblingNode = (LeafNode) sibling;
            if (isLeftSibling) {
                // 从左兄弟节点借用
                Integer borrowedKey = siblingNode.getKeys()[siblingNode.size() - 1];
                String borrowedValue = siblingNode.getValues()[siblingNode.size() - 1];
                targetNode.insertAtFront(borrowedKey, borrowedValue);
                siblingNode.remove(siblingNode.size() - 1);
                parent.getKeys()[childIndex - 1] = targetNode.getKeys()[0];
            } else {
                // 从右兄弟节点借用
                Integer borrowedKey = siblingNode.getKeys()[0];
                String borrowedValue = siblingNode.getValues()[0];
                targetNode.add(borrowedKey, borrowedValue);
                siblingNode.shiftLeft();
                parent.getKeys()[childIndex] = siblingNode.getKeys()[0];
            }
        } else if (node instanceof InnerNode && sibling instanceof InnerNode) {
            InnerNode targetNode = (InnerNode) node;
            InnerNode siblingNode = (InnerNode) sibling;
            if (isLeftSibling) {
                // 从左兄弟节点借用
                Integer borrowedKey = siblingNode.getKeys()[siblingNode.size() - 1];
                Node borrowedChild = siblingNode.getChildren()[siblingNode.size()];
                targetNode.insertAtFront(parent.getKeys()[childIndex - 1], borrowedChild);
                siblingNode.remove(siblingNode.size() - 1);
                parent.getKeys()[childIndex - 1] = borrowedKey;
            } else {
                // 从右兄弟节点借用
                Integer borrowedKey = siblingNode.getKeys()[0];
                Node borrowedChild = siblingNode.getChildren()[0];
                targetNode.add(parent.getKeys()[childIndex], borrowedChild);
                siblingNode.shiftLeft();
                parent.getKeys()[childIndex] = siblingNode.getKeys()[0];
            }
        }
    }

    private void mergeNodes(Node node, Node sibling, InnerNode parent, int childIndex, boolean isLeftSibling) {
        if (node instanceof LeafNode && sibling instanceof LeafNode) {
            LeafNode targetNode = isLeftSibling ? (LeafNode) sibling : (LeafNode) node;
            LeafNode sourceNode = isLeftSibling ? (LeafNode) node : (LeafNode) sibling;
            targetNode.merge(sourceNode);
            parent.removeChildAt(isLeftSibling ? childIndex : childIndex + 1);
        } else if (node instanceof InnerNode && sibling instanceof InnerNode) {
            InnerNode targetNode = isLeftSibling ? (InnerNode) sibling : (InnerNode) node;
            InnerNode sourceNode = isLeftSibling ? (InnerNode) node : (InnerNode) sibling;
            targetNode.merge(parent.getKeys()[isLeftSibling ? childIndex - 1 : childIndex], sourceNode);
            parent.removeChildAt(isLeftSibling ? childIndex : childIndex + 1);
        }
    }


    private InnerNode updateParents(Deque<InnerNode> parents) {
        InnerNode parent = parents.pop();
        if (parents.isEmpty()) {
            root = parent;
            return parent;
        } else {
            InnerNode grandParent = updateParents(parents);
            int index = 0;
            while (index < grandParent.getChildren().length && grandParent.getChildren()[index] != parent) {
                index++;
            }
            grandParent.getChildren()[index] = parent;
            return grandParent;
        }
    }

    ///// Leave these methods unchanged

    private int capacity = 0;

    private Node root;

    public BPlusTree(int capacity) {
        this(new LeafNode(capacity), capacity);
    }

    public BPlusTree(Node root, int capacity) {
        assert capacity % 2 == 0;
        this.capacity = capacity;
        this.root = root;
    }

    public Node rootNode() {
        return root;
    }

    public String toString() {
        return new BPlusTreePrinter(this).toString();
    }
}
