/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.ComparisonTools;
import campaignencyclopedia.data.DataAccessor;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityType;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipType;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static java.util.Collections.swap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import toolbox.display.EditListener;

/**
 *
 * @author Keith
 */
public class PlotEntityRelationshipEditor {
    /** This represents the current type of entity being displayed which affects the display of labels and which relationships are added. */
    private EntityType m_currentEntityType;
 
    /** The label of the relationships in part of this editor. */
    private final JLabel m_relsInLabel;
    
    /** The label of the relationships out part of this editor. */
    private final JLabel m_relsOutLabel;

    /** Display for relationships coming in associated with a given Entity. */
    private final JList<Relationship> m_relsInList;

    /** Display for relationships coming out associated with a given Entity. */
    private final JList<Relationship> m_relsOutList;
    
    /** The List Model that backs the JList for links into this node. */
    private final SortableListModel<Relationship> m_relsInModel;
    
    /** The List Model that backs the JList for links out of this node. */
    private final SortableListModel<Relationship> m_relsOutModel;
    
    /** The Add Relationship button for whatever relationship and entity is currently selected. */
    private final JButton m_addPlotConnectionButton;
    
    private JComboBox<String> m_relationshipDropdown;
    private JComboBox<Entity> m_plotEntityDropdown;
    
    private JLabel m_titleLabel;
    private JPanel m_controlBar;

    private Set<Relationship> m_originalRelationshipSet;
    

    
    private final DataAccessor m_accessor;
    private final EditListener m_editListener;
    private final EntityDisplay m_entityDisplay;
    
    private final Comparator<Relationship> m_comparator;

    private Frame m_parent;

    private static final String POINT_OUT_LABEL = "Learn Of";
    private static final String POINT_IN_LABEL = "Revealed By";
    private static final String LEAD_OUT_LABEL = "Leads To";
    private static final String LEAD_IN_LABEL = "Discovered At";
    
    
    /**
     * Creates a new instance of EntityRelationshipEditor.
     * @param parent a parent Frame to center dialogs launched by one of the buttons of this display.
     * @param accessor a data accessor for fetching required information for relationship editing.
     * @param display an EntityDisplay for showing entities if the user chooses to traverse one of the relationships.
     * @param editListener an edit listener to alert of changes made to this editor.
     */
    public PlotEntityRelationshipEditor(Frame parent, DataAccessor accessor, EntityDisplay display, EditListener editListener) {
        m_accessor = accessor;
        m_editListener = editListener;
        m_entityDisplay = display;
        m_parent = parent;
        m_originalRelationshipSet = new HashSet<>();
        m_relsInLabel = new JLabel("In");
        m_relsOutLabel = new JLabel("Out");
        
        
        m_comparator =  new Comparator<Relationship>() {
            @Override
            public int compare(Relationship relationship, Relationship otherRelationship) {
                if (relationship.compareTo(otherRelationship) == 0) {
                    String relationshipName = ComparisonTools.trimForSort(m_accessor.getEntity(relationship.getRelatedEntity()).getName());
                    String otherName = ComparisonTools.trimForSort(m_accessor.getEntity(otherRelationship.getRelatedEntity()).getName());
                    return (relationshipName.compareTo(otherName));
                } else {
                    return relationship.compareTo(otherRelationship);
                }
            }
        };
        
        //Dropdowns
        m_plotEntityDropdown = new JComboBox<>();
        m_plotEntityDropdown.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                
            }
        });
        
        m_relationshipDropdown = new JComboBox<>();
        m_relationshipDropdown.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                
            }
        });
        m_relationshipDropdown.addItem(RelationshipType.REVEALS.getDisplayString());
        m_relationshipDropdown.addItem(RelationshipType.LEADS_TO.getDisplayString());
        
        //Buttons and lists
        m_addPlotConnectionButton = new JButton("Add Relationship");
        m_addPlotConnectionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (m_entityDisplay.getShownEntity() != null) {
                    List<Entity> entities = m_accessor.getAllEntities();
                    if (entities.size() > 0) {
                        UUID relatedTo = ((Entity)m_plotEntityDropdown.getSelectedItem()).getId();
                        String relationshipType = (String)m_relationshipDropdown.getSelectedItem();
                        Relationship newRel = new Relationship(m_entityDisplay.getShownEntity(), relatedTo, relationshipType, false);
                        addRelationship(newRel);
                    } else {
                        JOptionPane.showMessageDialog(m_parent, "No entities exist in this campaign.", "No Entities to Relate To", JOptionPane.OK_OPTION);
                    }
                } else {
                    JOptionPane.showMessageDialog(m_parent, "Current item must be saved before adding relationships.", "Current Item Must be Saved", JOptionPane.OK_OPTION);
                }
            }
        });

        // Initialize the list model.
        m_relsInModel = new SortableListModel<>(m_comparator);
        m_relsOutModel = new SortableListModel<>(m_comparator);
        ListDataListener ldl = new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent lde) {
                m_editListener.edited();
            }
            @Override
            public void intervalRemoved(ListDataEvent lde) {
                m_editListener.edited();
            }
            @Override
            public void contentsChanged(ListDataEvent lde) {
                m_editListener.edited();
            }
        };
        m_relsInModel.addListDataListener(ldl);
        m_relsOutModel.addListDataListener(ldl);
        
        // Initialize the "IN" JList.
        m_relsInList = new JList<>();
        m_relsInList.setCellRenderer(new DoubleEndedRelationshipCellRenderer(accessor));
        m_relsInList.setModel(m_relsInModel);
        m_relsInList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                m_relsInList.setSelectedIndex(m_relsInList.locationToIndex(me.getPoint()));
                int selectedIndex = m_relsInList.getSelectedIndex();
                
                // Double Click to Navigate
                if (me.getClickCount() > 1) {
                    if (selectedIndex >= 0) {
                        Relationship selected = m_relsInModel.getElementAt(selectedIndex);
                        if (selected != null) {
                            m_entityDisplay.showEntity(selected.getEntityId());
                        }
                    }
                 
                // Right-Click, Context Menu
                } else if (SwingUtilities.isRightMouseButton(me)) {
                    if (selectedIndex >= 0) {
                        // Selected Relationship
                        final Relationship relationship = m_relsInModel.getElementAt(m_relsInList.getSelectedIndex());
                        
                        // Create Menu
                        JPopupMenu menu = new JPopupMenu();
                        
//                        // Make Public / Make Secret
//                        if (relationship.isSecret() && 
//                                !m_accessor.getEntity(relationship.getEntityId()).isSecret() && 
//                                !m_accessor.getEntity(relationship.getRelatedEntity()).isSecret()) {
//                            menu.add(new AbstractAction("Make Public") {
//                                @Override
//                                public void actionPerformed(ActionEvent ae) {
//                                    m_relsInModel.removeElement(relationship);
//                                    m_relsInModel.addElement(new Relationship(relationship.getEntityId(), relationship.getRelatedEntity(), relationship.getRelationshipText(), false));
//                                }
//                            });
//                        } else if (!relationship.isSecret()) {
//                            menu.add(new AbstractAction("Make Secret") {
//                                @Override
//                                public void actionPerformed(ActionEvent ae) {
//                                    m_relsInModel.removeElement(relationship);
//                                    m_relsInModel.addElement(new Relationship(relationship.getEntityId(), relationship.getRelatedEntity(), relationship.getRelationshipText(), true));
//                                }
//                            });                            
//                        }
                        
//                        // Edit Action
//                        menu.add(new AbstractAction("Edit") {
//                            @Override
//                            public void actionPerformed(ActionEvent ae) {
//                                if (m_entityDisplay.getShownEntity() != null) {
//                                    List<Entity> entities = m_accessor.getAllEntities();
//                                    if (entities.size() > 0) {
//                                        // Create Dialog
//                                        final RelationshipDialogContent dc = new RelationshipDialogContent(m_accessor, m_entityDisplay.getShownEntity());
//                                        dc.setRelationship(relationship);
//                                        
//                                        // OK Runnable
//                                        Runnable commit = new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                m_relsInModel.removeElement(relationship);
//                                                m_relsInModel.addElement(dc.getDisplayedRelationship());
//                                            }
//                                        };
//                                        
//                                        // Launch Dialog
//                                        DialogFactory.buildDialog(m_parent, "Edit Relationship", true, dc, new OkCancelCommitManager(commit));
//                                    } else {
//                                        JOptionPane.showMessageDialog(m_parent, "No entities exist in this campaign.", "No Entities to Relate To", JOptionPane.OK_OPTION);
//                                    }
//                                }
//                            }
//                        });
                        
                        // Remove Action
                        menu.add(new AbstractAction("Delete") {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                //Removes from all models (for safety) and the original list
                                removeRelationship(relationship);
                            }
                        });
                        
                        // Show the context menu
                        menu.show(m_relsInList, me.getX(), me.getY());
                    }
                }
            }
        });
        
        // Initialize the "OUT" JList.
        m_relsOutList = new JList<>();
        m_relsOutList.setCellRenderer(new DoubleEndedRelationshipCellRenderer(accessor));
        m_relsOutList.setModel(m_relsOutModel);
        m_relsOutList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                m_relsOutList.setSelectedIndex(m_relsOutList.locationToIndex(me.getPoint()));
                int selectedIndex = m_relsOutList.getSelectedIndex();
                
                // Double Click to Navigate
                if (me.getClickCount() > 1) {
                    if (selectedIndex >= 0) {
                        Relationship selected = m_relsOutModel.getElementAt(selectedIndex);
                        if (selected != null) {
                            m_entityDisplay.showEntity(selected.getRelatedEntity());
                        }
                    }
                 
                // Right-Click, Context Menu
                } else if (SwingUtilities.isRightMouseButton(me)) {
                    if (selectedIndex >= 0) {
                        // Selected Relationship
                        final Relationship relationship = m_relsOutModel.getElementAt(m_relsOutList.getSelectedIndex());
                        
                        // Create Menu
                        JPopupMenu menu = new JPopupMenu();
                        
                        // Remove Action
                        menu.add(new AbstractAction("Delete") {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                //Removes from all models (for safety) and the original list
                                removeRelationship(relationship);
                            }
                        });
                        
                        // Show the context menu
                        menu.show(m_relsOutList, me.getX(), me.getY());
                    }
                }
            }
        });
        
        m_titleLabel = new JLabel("Relationships");
        
        //Assemble the full panels
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(3, 3, 3, 3);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0f;
        gbc.weighty = 1.0f;
        gbc.insets = insets;
        gbc.fill = GridBagConstraints.BOTH;
        
        m_controlBar = new JPanel(new GridBagLayout());
        m_controlBar.add(m_relationshipDropdown, gbc);
        gbc.gridx++;
        m_controlBar.add(m_plotEntityDropdown, gbc);
        gbc.gridx++;
        m_controlBar.add(m_addPlotConnectionButton, gbc);
        //Spacer for the right side... TODO feels like this shouldn't be necessary...
        gbc.gridx++;
        gbc.weightx = 1.0f;
        m_controlBar.add(new JLabel(), gbc);
    }
    
    public void setCurrentEntityType(EntityType type) {
        m_currentEntityType = type;
        Set<Relationship> oldRels = getData();
        
        
        refreshEntityList();
        //hfgjkl  TODO, make this swap the labels and somehow handle the case wherer current entity switches types...
    }
    
    public void refreshEntityList() {
        List<Entity> allEntities = m_accessor.getAllEntities();
        
        m_plotEntityDropdown.removeAllItems();
        for (Entity e : allEntities) {
            //Only add PLOT things as available, linkable entities
            if (e.getType() == EntityType.PLOT_LEAD && m_currentEntityType == EntityType.PLOT_POINT) {
                m_plotEntityDropdown.addItem(e);
            } if (e.getType() == EntityType.PLOT_POINT && m_currentEntityType == EntityType.PLOT_LEAD) {
                m_plotEntityDropdown.addItem(e);
            }
        }
    }

    public void setData(Set<Relationship> relationships) {
        m_relsInModel.clear();
        m_relsOutModel.clear();
        m_originalRelationshipSet.clear();
        
        for (Relationship rel : relationships) {
            addRelationship(rel);
        }
        
        refreshEntityList();
    }

    /**
     * Adds the supplied Relationship to this Editor, if it is "Leads To" or "Reveals" and
     * the current entity is either a plot point or lead type.  It will always be added to the backing store.
     *
     * @param rel the Relationship to add.
     */
    public void addRelationship(Relationship rel) {
        m_originalRelationshipSet.add(rel);
        
        // Need to compare display name to rel. string since Relationships don't actually have a static enum list
        // and the toString includes more info like the UUIDs etc.  Cheap way to compare "type"
        if (rel.getRelationshipText().equals(RelationshipType.LEADS_TO.getDisplayString())) {
            //If we're at a lead, then Leads to is pointing out to the node this leads leads to
            //Otherwise if we're at a point, then this must be a link pointing to here from a lead
            if (m_currentEntityType == EntityType.PLOT_LEAD) {
                m_relsOutModel.addElement(rel);
            } else if (m_currentEntityType == EntityType.PLOT_POINT) {
                m_relsInModel.addElement(rel);
            }
        } else if (rel.getRelationshipText().equals(RelationshipType.REVEALS.getDisplayString())) {
            //If we're at a plot point, then we must be pointing out to a lead we reveal here
            //Otherwise if we're at a lead, the reveals rel must be pointing in from the node we revealed it at
            if (m_currentEntityType == EntityType.PLOT_POINT) {
                m_relsOutModel.addElement(rel);
            } else if (m_currentEntityType == EntityType.PLOT_LEAD) {
                m_relsInModel.addElement(rel);
            }
        }
    }

    /** 
     * Removes the supplied Relationship from this Editor.
     * @param rel the Relationship to remove.
     */
    public void removeRelationship(Relationship rel) {
        m_originalRelationshipSet.remove(rel);
        m_relsInModel.removeElement(rel);
        m_relsOutModel.removeElement(rel);
    }

    /**
     * Returns the Relationship data displayed in this Editor, plus the original relationship set.
     * @return the Relationship data displayed in this Editor.
     */
    public Set<Relationship> getData() {
        return m_originalRelationshipSet;
    }

    /**
     * Returns the Title Component of this editor.
     * @return the Title Component of this editor.
     */
    public Component getTitle() {
        return m_titleLabel;
    }

    public Component getControlBarComponent() {
        return m_controlBar;
    }

    public Component getInListLabel() {
        return m_relsInLabel;
    }

    public Component getInListComponent() {
        return m_relsInList;
    }
    
    public Component getOutListLabel() {
        return m_relsOutLabel;
    }

    public Component getOutListComponent() {
        return m_relsOutList;
    }

    /**
     * Returns the add relationship button.
     * @return the add relationship button.
     */
    public Component getAddRelationshipButton() {
        return m_addPlotConnectionButton;
    }

    /** Clears the data from this display. */
    void clearData() {
        m_relsInModel.clear();
        m_relsOutModel.clear();
    }
}
