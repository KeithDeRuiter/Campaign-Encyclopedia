package campaignencyclopedia.display.swing.filtertree;

import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityType;
import campaignencyclopedia.data.EntityType.EntityDomain;
import campaignencyclopedia.display.EntityDisplayFilter;
import campaignencyclopedia.display.swing.ColoredDisplayableCellRenderer;
import java.awt.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * This class represents the tree of all entities in the campaign, organized by {@link EntityDomain}
 * and {@link EntityType}. The items in this tree are sorted, and also filterable by
 * {@link EntityDisplayFilter} using the {@link #filterTree} method.
 *
 * @author Keith
 */
public class CampaignTree {

    /**
     * The JTree of all of the Entities in the campaign.
     */
    private final JTree m_entityTree;

    /**
     * Full, unfiltered, model for the entity tree as a complete backing data source.
     */
    private final SortableTreeModel m_entityTreeModel;

    /**
     * Model for the entity tree which only contains the items visible.
     */
    private final SortableTreeModel m_visibleTreeModel;

    /**
     * The original root node of the campaign.
     */
    private final DefaultMutableTreeNode m_campaignRootNode;

    /**
     * The original root node of the "visible" campaign tree structure.
     */
    private final DefaultMutableTreeNode m_visibleCampaignRootNode;

    /**
     * The entity filter applied to this campaign tree.
     */
    private EntityDisplayFilter m_filter;

    /**
     * Construct a new instance of {@link CampaignTree}. A root node is inserted into the display,
     * and default entity type and domain nodes are created.
     */
    public CampaignTree() {
        //Make the root node and model
        m_campaignRootNode = new DefaultMutableTreeNode("Campaign Elements");
        m_visibleCampaignRootNode = new DefaultMutableTreeNode("Campaign Elements");
        m_entityTreeModel = new SortableTreeModel(m_campaignRootNode);
        m_visibleTreeModel = new SortableTreeModel(m_visibleCampaignRootNode);

        //Fill out the model with the default type/domain nodes
        createDefaultEntityTreeNodes(m_campaignRootNode);
        createDefaultEntityTreeNodes(m_visibleCampaignRootNode);

        //Make the tree itself
        m_entityTree = new JTree(m_visibleTreeModel);
        m_entityTree.setRowHeight(30);
        m_entityTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        m_entityTree.setExpandsSelectedPaths(true);
        m_entityTree.setScrollsOnExpand(true);
        m_entityTree.setShowsRootHandles(false);
        m_entityTree.setCellRenderer(new ColoredDisplayableCellRenderer());

        m_entityTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                //Do nothing
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)(event.getPath().getLastPathComponent());
                //Force root node to stay open
                if (node.isRoot()) {
                    throw new ExpandVetoException(event);
                }
            }
        });
        
        //Start out with the domain nodes expanded
        expandDomainNodes(true);
    }

    /**
     * Returns the tree's displayable component.
     *
     * @return A component to display the Campaign Tree.
     */
    public Component getComponent() {
        return m_entityTree;
    }

    /**
     * Selects the entity provided in the campaign tree. If the entity is null or not possible to
     * view, then this call is ignored.
     *
     * @param e The entity to select.
     */
    public void selectEntity(Entity e) {
        TreePath path = findThingInTree(m_visibleTreeModel, e);
        m_entityTree.setSelectionPath(path);
        m_entityTree.scrollPathToVisible(path);
    }

    /**
     * Selects the row at the provided index.
     *
     * @param row The row to select.
     * @see JTree#setSelectionRow(int)
     */
    public void selectRow(int row) {
        m_entityTree.setSelectionRow(row);
        m_entityTree.scrollRowToVisible(row);
    }

    /**
     * Returns the currently selected Entity in the campaign tree. If an entity is not currently
     * selected, such as if a domain heading, or nothing is selected, then the method returns null.
     *
     * @return The currently selected Entity, or null if an Entity is not currently selected.
     */
    public Entity getSelectedEntity() {
        Entity selectedEntity = null;
        TreePath path = m_entityTree.getSelectionPath();

        if (path != null) {
            Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
            if (userObject instanceof Entity) {
                selectedEntity = (Entity) userObject;
            }
        }

        return selectedEntity;
    }

    /**
     * Inserts the entity into the tree's underlying model.
     *
     * @param entity The entity to insert.
     */
    public void insertEntity(Entity entity) {
        insertEntities(Collections.singleton(entity));
    }

    /**
     * Inserts all of the provided entities into the tree's underlying model.
     *
     * @param entities The collection of entities to insert.
     */
    public void insertEntities(Collection<Entity> entities) {
        insertEntityTreeNodes(entities);
    }

    /**
     * Removes the given entity from display in this tree and its underlying model.
     *
     * @param entity The entity to remove.
     */
    public void removeEntity(Entity entity) {
        removeEntities(Collections.singleton(entity));
    }

    /**
     * Removes the given entities from display in this tree and its underlying model.
     *
     * @param entities The collection of entities to remove.
     */
    public void removeEntities(Collection<Entity> entities) {
        for (Entity entity : entities) {
            //Remove from both models.  Don't need to check if it exists or for filtering, since call
            //is ignored if entity not found one of the models.
            removeEntityFromTree(m_entityTreeModel, entity);
            removeEntityFromTree(m_visibleTreeModel, entity);
        }
    }

    /**
     * Removes all data from the tree's display and models and repopulates with the deault
     * type/domain nodes.
     */
    public void clear() {
        //Clear out the original backing data
        DefaultMutableTreeNode root = ((DefaultMutableTreeNode) m_entityTreeModel.getRoot());
        root.removeAllChildren();
        createDefaultEntityTreeNodes(root);
        m_entityTreeModel.reload();

        //Clear out the visible data
        DefaultMutableTreeNode visRoot = ((DefaultMutableTreeNode) m_visibleTreeModel.getRoot());
        visRoot.removeAllChildren();
        createDefaultEntityTreeNodes(visRoot);
        m_visibleTreeModel.reload();
        
        expandDomainNodes(true);
    }

    /**
     * Forces expanding or collapsing of the entity domain nodes in the campaign tree.
     * @param expand {code true} to expand, {@code false} to collapse.
     */
    private void expandDomainNodes(boolean expand) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_entityTree.getModel().getRoot();
        
        //Iterate over all children of the root, and expand if they are domains (should always be for now, more of a protection)
        for (DefaultMutableTreeNode n : JTreeUtil.children(node)) {
            if (n.getUserObject() instanceof EntityDomain) {
                TreePath path = new TreePath(n.getPath());
                if (expand) {
                    m_entityTree.expandPath(path);
                } else {
                    m_entityTree.collapsePath(path);
                }
            }
        }
    }
    
    /**
     * Creates the tree nodes for the entities in the entity tree based on entity domain, type, and
     * the entities themselves.
     *
     * @param top Root node of the campaign.
     * @param entities List of entities to place in the tree.
     */
    private void createDefaultEntityTreeNodes(DefaultMutableTreeNode top) {
        //Set up domain nodes
        for (EntityDomain d : EntityDomain.values()) {
            DefaultMutableTreeNode domainNode = new DefaultMutableTreeNode(d);
            top.add(domainNode);

            //Within this domain, add all of the entity types that apply
            for (EntityType type : EntityType.values()) {
                if (type.getDomain() == d) {
                    DefaultMutableTreeNode entityTypeNode = new DefaultMutableTreeNode(type);
                    domainNode.add(entityTypeNode);
                }
            }
        }
    }

    /**
     * Creates the tree nodes for the entities in the entity tree based on entity domain, type, and
     * the entities themselves.
     *
     * @param top Root node of the campaign.
     * @param entities List of entities to place in the tree.
     */
    private void insertEntityTreeNodes(Collection<Entity> entities) {
        for (Entity e : entities) {
            //Get entity type for categorization
            EntityType type = e.getType();

            //Find the appropriate type node and insert there
            DefaultMutableTreeNode entityTypeNode = (DefaultMutableTreeNode) findThingInTree(m_entityTreeModel, type).getLastPathComponent();
            DefaultMutableTreeNode entityNode = new DefaultMutableTreeNode(e);
            m_entityTreeModel.insertNodeInto(entityNode, entityTypeNode);

            //Check filter and insert into visible model as well if it passes (or if there was no filter at all).
            if (m_filter == null || m_filter.accept(e)) {
                DefaultMutableTreeNode visibleEntityTypeNode = (DefaultMutableTreeNode) findThingInTree(m_visibleTreeModel, type).getLastPathComponent();
                DefaultMutableTreeNode visibleEntityNode = new DefaultMutableTreeNode(e);
                m_visibleTreeModel.insertNodeInto(visibleEntityNode, visibleEntityTypeNode);
            }
        }
    }

    /**
     * Finds a particular Entity-related or node-related Thing in the tree and returns its path.
     *
     * @param model The root of the tree to search
     * @param t The EntityDomain, EntityType, or Entity to search for.
     * @return The path to the provided content's node, or null if not found.
     */
    private TreePath findThingInTree(DefaultTreeModel model, Object t) {
        @SuppressWarnings("unchecked")
        Enumeration<DefaultMutableTreeNode> e = ((DefaultMutableTreeNode) model.getRoot()).breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            Object userObj = node.getUserObject();
            if (userObj instanceof EntityType.EntityDomain) {
                if (((EntityType.EntityDomain) userObj).equals(t)) {
                    return new TreePath(node.getPath());
                }
            } else if (userObj instanceof EntityType) {
                if (((EntityType) userObj).equals(t)) {
                    return new TreePath(node.getPath());
                }
            } else if (userObj instanceof Entity) {
                if (((Entity) userObj).equals(t)) {
                    return new TreePath(node.getPath());
                }
            }
        }
        return null;
    }

    /**
     * Finds the given entity in the tree and removes it from the tree's model. If the entity is not
     * found in the tree, call is ignored.
     *
     * @param e The entity to remove.
     */
    private void removeEntityFromTree(DefaultTreeModel model, Entity e) {
        //Find the entity's path in the tree
        TreePath pathToRemove = findThingInTree(model, e);

        //Remove the node at the end of the path from the tree if it was found
        if (pathToRemove != null) {
            model.removeNodeFromParent((MutableTreeNode) pathToRemove.getLastPathComponent());
        }
    }

    /**
     * Applies the entity display filter to the leaf nodes of the tree. This creates a duplicate
     * series of nodes and sets them in the visible model. The core model remains unchanged.
     *
     * @param filter
     */
    public void filterTree(EntityDisplayFilter filter) {
        //Store the filter being used
        this.m_filter = filter;

        //Rebuild the tree for display based on the filter
        DefaultMutableTreeNode newRootNode = matchAndBuildNode(m_campaignRootNode);
        m_visibleTreeModel.setRoot(newRootNode);

        //Reload model and expand the tree's nodes
        m_visibleTreeModel.reload();
        JTreeUtil.setTreeExpandedState(m_entityTree, true);
    }

    /**
     * Recursively examines the tree, matching nodes against the current filter, to clone a new
     * model for display.
     *
     * @param oldNode The root node to start matching and cloning from.
     * @return A cloned tree comprised of the content from the original nodes which passed the
     * filter.
     */
    private DefaultMutableTreeNode matchAndBuildNode(DefaultMutableTreeNode oldNode) {
        //If there is no filter, then just clone the whole tree.
        if (m_filter == null) {
            return JTreeUtil.cloneNode(oldNode);
        }

        //If there is a filter, and if the object is an Entity match, clone and return it!
        Object userObject = oldNode.getUserObject();
        if (userObject instanceof Entity) {
            Entity entityObject = (Entity) userObject;
            //If there is either no filter, or the tree is filtered and this node passes the filter
            if (m_filter == null || (!oldNode.isRoot() && m_filter.accept(entityObject))) {
                return JTreeUtil.cloneNode(oldNode);
            }
        }

        //Otherwise not a match, continue searching down the tree
        DefaultMutableTreeNode newMatchedNode = oldNode.isRoot() ? new DefaultMutableTreeNode(oldNode.getUserObject()) : null;
        for (DefaultMutableTreeNode childOldNode : JTreeUtil.children(oldNode)) {
            DefaultMutableTreeNode newMatchedChildNode = matchAndBuildNode(childOldNode);
            if (newMatchedChildNode != null) {
                if (newMatchedNode == null) {
                    newMatchedNode = new DefaultMutableTreeNode(oldNode.getUserObject());
                }
                newMatchedNode.add(newMatchedChildNode);
            }
        }
        return newMatchedNode;
    }
}
