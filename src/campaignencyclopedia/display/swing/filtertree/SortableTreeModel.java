package campaignencyclopedia.display.swing.filtertree;

import java.util.Comparator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * A Tree model that supports being sorted. Uses toString comparison ignoring case by default.
 *
 * @author Keith
 */
public class SortableTreeModel extends DefaultTreeModel {

    /**
     * The comparator used for sorting.
     */
    private final Comparator comparator;

    /**
     * Creates an instance of the model. Uses default string comparator in this class.
     *
     * @param node Root node of the model
     * @see TreeStringComparator
     */
    public SortableTreeModel(TreeNode node) {
        this(node, new TreeStringComparator());
    }

    /**
     * Creates an instance of the model using the provided comparator.
     *
     * @param node Root node of the model.
     * @param c comparator to use for sorting.
     */
    public SortableTreeModel(TreeNode node, Comparator c) {
        super(node);
        comparator = c;
    }

    /**
     *
     * Creates an instance of the model using the provided comparator.
     *
     * @param node Root node of the model.
     * @param asksAllowsChildren a boolean, false if any node can have children, true if each node
     * is asked to see if it can have children.
     * @param c comparator to use for sorting.
     * @see DefaultTreeModel#asksAllowsChildren
     */
    public SortableTreeModel(TreeNode node, boolean asksAllowsChildren, Comparator c) {
        super(node, asksAllowsChildren);
        comparator = c;
    }

    /**
     * Inserts the child node under the parent node.
     *
     * @see #insertNodeInto
     * @param child The child to insert.
     * @param parent The parent that will contain the new child.
     */
    public void insertNodeInto(MutableTreeNode child, MutableTreeNode parent) {
        int index = findIndexFor(child, parent);
        super.insertNodeInto(child, parent, index);
    }

    /**
     * This insertion method ignores the index, as the model is self-sorting. {@inheritDoc}
     */
    @Override
    public void insertNodeInto(MutableTreeNode child, MutableTreeNode par, int i) {
        // The index is useless in this model, so just ignore it.
        insertNodeInto(child, par);
    }

    /**
     * Perform a recursive binary search on the children to find the right insertion point for the
     * next node.
     *
     * @param child The node to find the appropriate index for.
     * @param parent The parent node to search from.
     * @return The index for the provided child.
     */
    @SuppressWarnings("unchecked")
    private int findIndexFor(MutableTreeNode child, MutableTreeNode parent) {
        int cc = parent.getChildCount();
        if (cc == 0) {
            return 0;
        }
        if (cc == 1) {
            return comparator.compare(child, parent.getChildAt(0)) <= 0 ? 0 : 1;
        }
        return findIndexFor(child, parent, 0, cc - 1); // First & last index
    }

    /**
     * @see #findIndexFor
     * @param i1 The lower bound to search
     * @param i2 The upper bound to search
     * @return The index of this child node.
     */
    @SuppressWarnings("unchecked")
    private int findIndexFor(MutableTreeNode child, MutableTreeNode parent, int i1, int i2) {
        if (i1 == i2) {
            return comparator.compare(child, parent.getChildAt(i1)) <= 0 ? i1
                    : i1 + 1;
        }
        int half = (i1 + i2) / 2;
        if (comparator.compare(child, parent.getChildAt(half)) <= 0) {
            return findIndexFor(child, parent, i1, half);
        }
        return findIndexFor(child, parent, half + 1, i2);
    }
}

/**
 * Comparator based on string comparison, ignoring case.
 *
 * @author Keith
 */
class TreeStringComparator implements Comparator {

    /**
     * Compares the two {@link DefaultMutableTreeNode} objects using their toString method, ignoring
     * case. {@inheritDoc}
     *
     * @throws IllegalArgumentException if the objects are not instanceof
     * {@link DefaultMutableTreeNode}
     */
    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof DefaultMutableTreeNode && o2 instanceof DefaultMutableTreeNode)) {
            throw new IllegalArgumentException(
                    "Can only compare DefaultMutableTreeNode objects");
        }
        String s1 = ((DefaultMutableTreeNode) o1).getUserObject().toString();
        String s2 = ((DefaultMutableTreeNode) o2).getUserObject().toString();
        return s1.compareToIgnoreCase(s2);
    }
}
