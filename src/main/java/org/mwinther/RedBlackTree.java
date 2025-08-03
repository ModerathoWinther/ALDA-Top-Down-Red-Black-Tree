// Feel free to use packages in your own environment, but remember to remove when handing it in
package org.mwinther;

//RedBlackTree class
//
//CONSTRUCTION: with no parameters
//
//******************PUBLIC OPERATIONS*********************
//void insert( x )       --> Insert x
//void remove( x )       --> Remove x (unimplemented)
//boolean contains( x )  --> Return true if x is found
//Comparable findMin( )  --> Return smallest item
//Comparable findMax( )  --> Return largest item
//boolean isEmpty( )     --> Return true if empty; else false
//void makeEmpty( )      --> Remove all items
//void printTree( )      --> Print all items
//******************ERRORS********************************
//Throws UnderflowException as appropriate

/**
 * Implements a red-black tree. Note that all "matching" is based on the
 * compareTo method.
 * <p>
 * Modified with a working, but extremely inefficient implementation of remove,
 * and following Checkstyles styleguide.
 *
 * @author Mark Allen Weiss
 */
public class RedBlackTree<AnyType extends Comparable<? super AnyType>> {
    private static final int BLACK = 1; // BLACK must be 1
    private static final int RED = 0;

    private final RedBlackNode<AnyType> header;
    private final RedBlackNode<AnyType> nullNode;

    // Used in insert routine and its helpers
    private RedBlackNode<AnyType> current;
    private RedBlackNode<AnyType> sibling;
    private RedBlackNode<AnyType> parent;
    private RedBlackNode<AnyType> grand;
    private RedBlackNode<AnyType> great;

    /**
     * Construct the tree.
     */
    public RedBlackTree() {
        nullNode = new RedBlackNode<>(null);
        nullNode.left = nullNode.right = nullNode;
        header = new RedBlackNode<>(null);
        header.left = header.right = nullNode;
    }

    /**
     * Compare item and t.element, using compareTo, with caveat that if t is header,
     * then item is always larger. This routine is called if is possible that t is
     * header. If it is not possible for t to be header, use compareTo directly.
     */
    private int compare(AnyType item, RedBlackNode<AnyType> t) {
        if (t == header) return 1;
        else return item.compareTo(t.element);
    }

    /**
     * Insert into the tree.
     *
     * @param item the item to insert.
     */
    public void insert(AnyType item) {
        current = parent = grand = header;
        nullNode.element = item;

        while (compare(item, current) != 0) {
            great = grand;
            grand = parent;
            parent = current;
            current = compare(item, current) < 0 ? current.left : current.right;

            // Check if two red children; fix if so
            if (current.left.color == RED && current.right.color == RED) handleReorient(item);
        }

        // Insertion fails if already present
        if (current != nullNode) return;
        current = new RedBlackNode<>(item, nullNode, nullNode);

        // Attach to parent
        if (compare(item, parent) < 0) parent.left = current;
        else parent.right = current;
        handleReorient(item);
    }

    /**
     * Remove x from the tree.
     * @param x the item to remove.
     */
    public void remove(AnyType x) {
        if (!isEmpty() && contains(x)) {
            current = sibling = parent = grand = header;
            header.color = RED;
            nullNode.element = x;

            while (compare(x, current) != 0) {
                traverseDownTree(x);

                if (isDoubleBlack(current)) handleFirstCase();
                else if (isRed(current.left) || isRed(current.right)) {

                    RedBlackNode<AnyType> next = compare(x, current) < 0 ? current.left : current.right;
                    if (compare(x, current) != 0 && isBlack(next)) {
                        handleSecondCase(x);
                        if (isDoubleBlack(current)) handleFirstCase();
                    }
                }
                header.right.color = nullNode.color = BLACK;
            }
            if (isLeaf(current)) removeLeaf();
            else {
                RedBlackNode<AnyType> toRemove = current;
                AnyType replacementElement = current.right != nullNode ? findSuccessorElement(current) : findPredecessorElement(current);
                remove(replacementElement);
                replaceNodeElement(toRemove, replacementElement);
            }
            header.color = header.right.color = BLACK;
        }
    }

    /**
     * Find the smallest item the tree.
     *
     * @return the smallest item or throw UnderflowExcepton if empty.
     */
    public AnyType findMin() {
        if (isEmpty()) throw new UnderflowException();

        RedBlackNode<AnyType> itr = header.right;

        while (itr.left != nullNode) itr = itr.left;

        return itr.element;
    }

    /**
     * Find the largest item in the tree.
     *
     * @return the largest item or throw UnderflowExcepton if empty.
     */
    public AnyType findMax() {
        if (isEmpty()) throw new UnderflowException();

        RedBlackNode<AnyType> itr = header.right;

        while (itr.right != nullNode) itr = itr.right;

        return itr.element;
    }

    /**
     * Find an item in the tree.
     *
     * @param x the item to search for.
     * @return true if x is found; otherwise false.
     */
    public boolean contains(AnyType x) {
        nullNode.element = x;
        current = header.right;

        for (; ; ) {
            if (x.compareTo(current.element) < 0) current = current.left;
            else if (x.compareTo(current.element) > 0) current = current.right;
            else return current != nullNode;
        }
    }

    /**
     * Make the tree logically empty.
     */
    public void makeEmpty() {
        header.right = nullNode;
    }

    /**
     * Print the tree contents in sorted order.
     */
    public void printTree() {
        if (isEmpty()) System.out.println("Empty tree");
        else printTree(header.right);
    }

    /**
     * Internal method to print a subtree in sorted order.
     *
     * @param t the node that roots the subtree.
     */
    private void printTree(RedBlackNode<AnyType> t) {
        if (t != nullNode) {
            printTree(t.left);
            System.out.println(t.element);
            printTree(t.right);
        }
    }

    /**
     * Test if the tree is logically empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return header.right == nullNode;
    }

    /**
     * Internal routine that is called during an insertion if a node has two red
     * children. Performs flip and rotations.
     *
     * @param item the item being inserted.
     */
    private void handleReorient(AnyType item) {
        // Do the color flip
        current.color = RED;
        current.left.color = BLACK;
        current.right.color = BLACK;

        if (parent.color == RED) // Have to rotate
        {
            grand.color = RED;
            if ((compare(item, grand) < 0) != (compare(item, parent) < 0))
                parent = rotate(item, grand); // Start dbl rotate
            current = rotate(item, great);
            current.color = BLACK;
        }
        header.right.color = BLACK; // Make root black
    }

    /**
     * Internal routine that performs a single or double rotation. Because the
     * result is attached to the parent, there are four cases. Called by
     * handleReorient.
     *
     * @param item   the item in handleReorient.
     * @param parent the parent of the root of the rotated subtree.
     * @return the root of the rotated subtree.
     */
    private RedBlackNode<AnyType> rotate(AnyType item, RedBlackNode<AnyType> parent) {
        if (compare(item, parent) < 0)
            return parent.left = compare(item, parent.left) < 0 ? rotateWithLeftChild(parent.left) : // LL
                    rotateWithRightChild(parent.left); // LR
        else return parent.right = compare(item, parent.right) < 0 ? rotateWithLeftChild(parent.right) : // RL
                rotateWithRightChild(parent.right); // RR
    }

    /**
     * Rotate binary tree node with left child.
     */
    private RedBlackNode<AnyType> rotateWithLeftChild(RedBlackNode<AnyType> node) {
        RedBlackNode<AnyType> leftChild = node.left;
        node.left = leftChild.right;
        leftChild.right = node;
        return leftChild;
    }

    /**
     * Rotate binary tree node with right child.
     */
    private RedBlackNode<AnyType> rotateWithRightChild(RedBlackNode<AnyType> node) {
        RedBlackNode<AnyType> rightChild = node.right;
        node.right = rightChild.left;
        rightChild.left = node;
        return rightChild;
    }

    /**
     * Traverses the tree based on value of x and updates pointers.
     * @param x x in remove
     */
    private void traverseDownTree(AnyType x) {
        great = grand;
        grand = parent;
        parent = current;
        current = compare(x, current) < 0 ? current.left : current.right;
        sibling = parent.left == current ? parent.right : parent.left;
    }

    /**
     *
     * @param node to be evaluated.
     * @return true if node is black or NIL.
     */
    private boolean isDoubleBlack(RedBlackNode<AnyType> node) {
        return isBlack(node) && isBlack(node.left) && isBlack(node.right);
    }

    /**
     *
     * @param node to be evaluated
     * @return true if the node is black or NIL.
     */
    private boolean isBlack(RedBlackNode<AnyType> node) {
        return node == nullNode || node.color == BLACK;
    }

    /**
     *
     * @param node to be evaluated
     * @return true if node is red.
     */
    private boolean isRed(RedBlackNode<AnyType> node) {
        return !isBlack(node);
    }

    /**
     * Evaluates all subcases of case 1 and performs the appropriate action.
     */
    private void handleFirstCase() {
        if (isDoubleBlack(sibling)) {
            // Subcase 1A: Current and sibling is double black, perform simple color flip.
            current.color = sibling.color = RED;
            parent.color = BLACK;
        } else if (sibling != nullNode && isBlack(sibling)) {
            // Subcases 1B and 1C: one or both of sibling's children are red. 1B handles an outer red child and 1C an inside red child.
            if (isRed(sibling.left) && isOuterChild(sibling.left)) handleOuterRedSiblingChild();
            else if (isRed(sibling.right) && isOuterChild(sibling.right)) handleOuterRedSiblingChild();
            else {
                // Sibling has an inner red child.
                if (isRed(sibling.left)) handleInnerRedSiblingChild(sibling.left);
                else handleInnerRedSiblingChild(sibling.right);
            }
        }
    }

    /**
     * Handles subcase 2B by promoting the red sibling, making it the grandparent of current and flips
     * colors of sibling and parent.
     * @param x x in remove.
     */
    private void handleSecondCase(AnyType x) {
        traverseDownTree(x);
        parent.color = RED;
        sibling.color = BLACK;
        grand = rotate(sibling.element, grand);
        sibling = parent.right == current ? parent.left : parent.right;
    }

    /**
     * Subcase 1B: Promotes a red outer sibling child.
     */
    private void handleOuterRedSiblingChild() {
        grand = rotate(sibling.element, grand);
        handleRecolor();
        sibling = parent.right == current ? parent.left : parent.right;
    }

    /**
     * Subcase 1C: Promotes a red inner sibling child.
     * @param child red sibling child identified in handleFirstSubcase
     */
    private void handleInnerRedSiblingChild(RedBlackNode<AnyType> child) {
        sibling = rotate(child.element, parent);
        grand = rotate(sibling.element, grand);
        handleRecolor();
        sibling = parent.right == current ? parent.left : parent.right;
    }

    /**
     * Recolors relevant nodes for unlucky Subcase 2B
     */
    private void handleRecolor() {
        grand.left.color = grand.right.color = BLACK;
        current.color = grand.color = RED;
    }

    /**
     * Evaluates whether the node is an outside or inside child.
     * @param child from handleFirstCase
     * @return true if child is an outside child.
     */
    private boolean isOuterChild(RedBlackNode<AnyType> child) {
        // Check if parent is a left child of grand
        if (parent.left == sibling) {
            // Returns true if child is outer child relative to p and g
            return sibling.left == child;
        } else return sibling.right == child;
    }

    /**
     * Evaluates whether the node is a leaf node.
     * @param node to be evaluated.
     * @return true if the node is a leaf node.
     */
    private boolean isLeaf(RedBlackNode<AnyType> node) {
        return node != nullNode && node.left == nullNode && node.right == nullNode;
    }

    /**
     * Removes the current leaf node from its parent.
     */
    private void removeLeaf() {
        if (compare(current.element, header.right) == 0) header.right = nullNode;
        else {
            if (parent.right == current) parent.right = nullNode;
            else parent.left = nullNode;
        }
    }

    /**
     * Finds the successor of a node.
     * @param node from remove
     * @return element of the successor of node.
     */
    private AnyType findSuccessorElement(RedBlackNode<AnyType> node) {
        RedBlackNode<AnyType> succ = node.right;
        while (succ.left != nullNode) succ = succ.left;
        return succ.element;
    }

    /**
     * Finds the in order predecessor of a node.
     * @param node from remove
     * @return element of the in order predecessor of node.
     */
    private AnyType findPredecessorElement(RedBlackNode<AnyType> node) {
        RedBlackNode<AnyType> pre = node.left;
        while (pre.right != nullNode) pre = pre.right;
        return pre.element;
    }

    /**
     * Replaces the element of toRemove.
     * @param toRemove node to be removed from remove
     * @param element element to insert into toRemove.
     */
    private void replaceNodeElement(RedBlackNode<AnyType> toRemove, AnyType element) {
        if (compare(element, header.right) == 0) header.right.element = element;
        else toRemove.element = element;
    }

    private static class RedBlackNode<AnyType> {
        private AnyType element; // The data in the node
        private RedBlackNode<AnyType> left; // Left child
        private RedBlackNode<AnyType> right; // Right child
        private int color; // Color

        // Constructors
        RedBlackNode(AnyType theElement) {
            this(theElement, null, null);
        }

        RedBlackNode(AnyType theElement, RedBlackNode<AnyType> lt, RedBlackNode<AnyType> rt) {
            element = theElement;
            left = lt;
            right = rt;
            color = RedBlackTree.BLACK;
        }

        @Override
        public String toString() {
            return element == null ? "NULL" : element.toString();
        }

    }

}