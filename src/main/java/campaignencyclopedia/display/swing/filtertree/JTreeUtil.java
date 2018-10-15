package campaignencyclopedia.display.swing.filtertree;

import java.util.Collections;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * A class with various utilities for working with {@link JTree}.
 *
 * @author Keith
 */
public class JTreeUtil {

    /**
     * Sets all nodes in the provided tree to be expanded or contracted.
     *
     * @param tree The tree to expand or contract the nodes of.
     * @param expanded {@code true} to expand, {@code false} to contract.
     */
    public static void setTreeExpandedState(JTree tree, boolean expanded) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        setNodeExpandedState(tree, node, expanded);
    }

    /**
     * Sets all nodes below the provided node in the provided tree to be expanded or contracted.
     *
     * @param tree The tree to expand or contract the nodes of.
     * @param node The root node to expand or contract down from.
     * @param expanded {@code true} to expand, {@code false} to contract.
     */
    public static void setNodeExpandedState(JTree tree, DefaultMutableTreeNode node, boolean expanded) {
        for (TreeNode treeNode : children(node)) {
            DefaultMutableTreeNode mutableNode = (DefaultMutableTreeNode)treeNode;
            setNodeExpandedState(tree, mutableNode, expanded);
        }
        if (!expanded && node.isRoot()) {
            return;
        }
        TreePath path = new TreePath(node.getPath());
        if (expanded) {
            tree.expandPath(path);
        } else {
            tree.collapsePath(path);
        }
    }

    /**
     * Clones the node and all of its children, making new DefaultTreeNode's for each with the User
     * Objects pulled from the original children.
     *
     * @param oldNode The node to clone down.
     * @return A new node instance with the same contents and children's contents as the original
     * node.
     */
    public static DefaultMutableTreeNode cloneNode(DefaultMutableTreeNode oldNode) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(oldNode.getUserObject());
        for (TreeNode oldChildNode : JTreeUtil.children(oldNode)) {
            DefaultMutableTreeNode oldMutableChild = (DefaultMutableTreeNode) oldChildNode;
            DefaultMutableTreeNode newChildNode = new DefaultMutableTreeNode(oldMutableChild.getUserObject());
            newNode.add(newChildNode);
            if (!oldChildNode.isLeaf()) {
                cloneChildrenObjectsTo(oldMutableChild, newChildNode);
            }
        }
        return newNode;
    }

    /**
     * Makes clones of the children nodes recursively from one node to another. This creates new
     * tree nodes whose contents are pulled from the existing user objects on the original tree node
     * provided.
     *
     * @param from The source node to copy children from.
     * @param to The node to copy children nodes to.
     */
    public static void cloneChildrenObjectsTo(DefaultMutableTreeNode from, DefaultMutableTreeNode to) {
        for (TreeNode oldChildNode : JTreeUtil.children(from)) {
            DefaultMutableTreeNode oldMutableChild = (DefaultMutableTreeNode) oldChildNode;
            DefaultMutableTreeNode newChildNode = new DefaultMutableTreeNode(oldMutableChild.getUserObject());
            to.add(newChildNode);
            if (!oldChildNode.isLeaf()) {
                cloneChildrenObjectsTo(oldMutableChild, newChildNode);
            }
        }
    }

    /**
     * Returns a list of tree nodes which are the children of the provided node. Convenience to
     * avoid using {@link java.util.Enumeration}
     *
     * @param node The node whose children will be retrieved.
     * @return A {@link List} of the provided node's children.
     */
    @SuppressWarnings("unchecked")
    public static List<? extends TreeNode> children(DefaultMutableTreeNode node) {
        
        List<TreeNode> list = Collections.list(node.children());
        return list;
    }
}
