package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.Campaign;
import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.ColoredDisplayable;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.EntityType;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.data.RelationshipManager;
import campaignencyclopedia.data.TimelineEntry;
import campaignencyclopedia.display.EntityDisplayFilter;
import campaignencyclopedia.display.UserDisplay;
import campaignencyclopedia.display.swing.action.SaveHelper;
import campaignencyclopedia.display.NavigationPath;
import campaignencyclopedia.display.swing.action.DeleteEntityAction;
import campaignencyclopedia.display.swing.filtertree.CampaignTree;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import toolbox.display.DisplayUtilities;
import toolbox.display.EditListener;

/**
 * The top level display class of this application.
 * @author adam
 */
public class MainDisplay implements EditListener, UserDisplay {

    // TOP LEVEL WINDOW COMPONENTS, DATA.
    /** The top-level window of this application. */
    private JFrame m_frame;

    /** The starting dimensions of the top-level window. */
    private static final Dimension WINDOW_SIZE = new Dimension(1600, 900);

    /** A MenuManager for building menus as needed. */
    private MenuManager m_menuManager;

    /** The tree structure for managing the data in the "list" of all entities. */
    private CampaignTree m_campaignTree;
    
    /** The Component for display of all of the Entities in the campaign, stored to UI purposes e.g. context menu. */
    private Component m_entityTreeComponent;
    
    /** The split pane between the entity list and entity view/edit display. */
    private JSplitPane m_entitySplitPane;

    /** The campaign title label. */
    private JLabel m_campaignTitleLabel;

    /** The Quick Search box */
    private JTextField m_searchBox;

    /** The quick search check box. */
    private JCheckBox m_filterCheckBox;
    
    /** A ComboBox for selecting which types to filter by. */
    private JComboBox<ColoredDisplayable> m_entityTypeFilterComboBox;

    /** The nav forward button. */
    private JButton m_forwardButton;

    /** The nav backward button. */
    private JButton m_backButton;


    // COMPONENTS FOR THE ENTITY VIEW/EDIT DISPLAY
    /** The text field for entering the name of an Entity. */
    private JTextField m_entityNameField;

    /** The Entity Type combobox selector. */
    private JComboBox<EntityType> m_typeSelector;

    /** A button for creating a  new entity. */
    private JButton m_newEntityButton;

    /** A button for adding/updating the currently displayed entity. */
    private JButton m_commitEntityButton;

    /** The JCheckBox for making this Entity secret (or not). */
    private JCheckBox m_secretEntityCheckbox;

    /** A display for the rest of the entity info such as tags, descriptions, and relationships. */
    private EntityDetailsDisplay m_entityDetails;

    // BACKING DATA
    /** The ID of the currently displayed Entity, if it exists or the entity displayed has one.  If not, this value is null. */
    private UUID m_displayedEntityId;

    /** A campaign data manager, which keeps track of the current data. */
    private final CampaignDataManager m_cdm;

    /** The navigation path for this display. */
    private NavigationPath m_navPath;

    /** The blue Color used throughout this application. */
    public static final Color BLUE = new Color(96, 128, 192);

    /** The text Color used on the blue background. */
    public static final Color SILVER = new Color(248, 248, 248);

    /** The current release version number. */
    public static final String VERSION = "v1.6.0";

    /** The date this release was created. */
    public static final String DATE = "February 07, 2017";

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(MainDisplay.class.getName());

    /**
     * Creates a new Main Display instance.
     * @param cdm the campaign data manager.
     */
    public MainDisplay(CampaignDataManager cdm) {
        m_cdm = cdm;
        initialize();
    }

    /** {@inheritDoc} */
    @Override
    public void edited() {
        if (isEntityContentCommittable()) {
            m_commitEntityButton.setEnabled(true);
        } else {
            m_commitEntityButton.setEnabled(false);
        }
    }

    /**
     * Returns true if the content of the displayed Entity is valid to be added to the encyclopedia, false otherwise.
     * @return true if the content of the displayed Entity is valid to be added to the encyclopedia, false otherwise.
     */
    private boolean isEntityContentCommittable() {
        return !m_entityNameField.getText().trim().isEmpty();
    }

    /** Adds the currently displayed Entity to the CampaignDataManager and clears the display. */
    private void commitDisplayedDataToCdm() {
        // Get shown Entity
        Entity entity = getDisplayedEntity();
        //Store original relationships as the ones to remove (unless they are still displayed) in order to catch deletes.
        Set<Relationship> relsToRemove = new HashSet<>(m_cdm.getRelationshipsForEntity(entity.getId()).getAllRelationships());
        
        // Get Displayed Relationships and add them.  Simultaneously remove them from "orig" list to ctach deletes
        RelationshipManager relMgr = new RelationshipManager();
        for (Relationship rel : m_entityDetails.getRelationships()) {
            // If the entity is secret and it has any public relationships, they must now be secret, so update them.
            if (entity.isSecret() && !rel.isSecret()) {
                relMgr.addRelationship(new Relationship(rel.getEntityId(), rel.getRelatedEntity(), rel.getRelationshipText(), true));
            } else {
                relMgr.addRelationship(rel);
            }
            relsToRemove.remove(rel);
        }

        // If the entity is secret:
        //  - relationships owned by other entities pointing to it must be secret, so update them.
        //  - Timeline events pointing to it must be secret, so update them.
        if (entity.isSecret()) {
            for (Entity otherEntity : m_cdm.getAllEntities()) {
                RelationshipManager otherRelMgr = m_cdm.getRelationshipsForEntity(otherEntity.getId());
                
                Set<Relationship> requireUpdate = new HashSet<>();
                Set<Relationship> requireRemove = new HashSet<>();
                
                for (Relationship rel : new HashSet<>(otherRelMgr.getPublicRelationships())) {
                    if (!rel.isSecret() && rel.getRelatedEntity().equals(entity.getId())) {
                        requireRemove.add(rel);
                        requireUpdate.add(new Relationship(rel.getEntityId(), rel.getRelatedEntity(), rel.getRelationshipText(), true));
                    }
                }
                // Clear the public data from the relationship manager and add in the newly updated stuff.
                for (Relationship r : requireRemove) {
                    m_cdm.removeRelationship(r);
                }
                otherRelMgr.addAllRelationships(requireUpdate);
                m_cdm.addOrUpdateAllRelationships(otherEntity.getId(), otherRelMgr);
            }

            // Make secret any Timeline Entries that now must be.
            for (TimelineEntry tle : m_cdm.getTimelineData()) {
                if (tle.getAssociatedId().equals(entity.getId())) {
                    m_cdm.removeTimelineEntry(tle.getId());
                    m_cdm.addOrUpdateTimelineEntry(new TimelineEntry(tle.getTitle(), tle.getMonth(), tle.getYear(), true, tle.getAssociatedId(), tle.getId()));
                }
            }
        }

        // Check to see if the Entity is already in our data manager
        // If it is, remove it (old version) from the tree's model and re-add (new version).
        Entity previousState = m_cdm.getEntity(entity.getId());
        if (previousState != null) {
            m_campaignTree.removeEntity(previousState);
        }
        m_campaignTree.insertEntity(entity);
        m_campaignTree.selectEntity(entity);

        // Add the new or updated Entity to the CDM
        m_cdm.addOrUpdateEntity(entity);

        // Add/Update the Relationships
        for (Relationship r : relsToRemove) {
            m_cdm.removeRelationship(r);
        }
        m_cdm.addOrUpdateAllRelationships(entity.getId(), relMgr);
        m_displayedEntityId = entity.getId();

        // Force Update of display for relationship changes.
        m_entityDetails.setRelationships(relMgr.getAllRelationships());
    }

    /**
     * Returns the currently displayed entity.
     * @return the currently displayed entity.
     */
    private Entity getDisplayedEntity() {
        UUID id;
        String name = m_entityNameField.getText().trim();
        EntityType type = (EntityType)m_typeSelector.getSelectedItem();
        EntityData publicData = m_entityDetails.getPublicData();
        EntityData secretData = m_entityDetails.getSecretData();
        if (m_displayedEntityId == null) {
            id = UUID.randomUUID();
        } else {
            id = m_displayedEntityId;
        }
        boolean isSecret = m_secretEntityCheckbox.isSelected();

        return new Entity(id, name, type, publicData, secretData, isSecret);
    }


    /** {@inheritDoc} */
    @Override
    public UUID getShownEntity() {
        return m_displayedEntityId;
    }

    /** {@inheritDoc} */
    @Override
    public void removeEntity(Entity entity) {
        if (entity.getId().equals(m_displayedEntityId)) {
            clearDisplayedEntity();
        }
        m_campaignTree.removeEntity(entity);
        if (m_navPath != null) {
            m_navPath.removeAll(entity.getId());
            updateNavButtons();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearDisplayedEntity() {
        m_displayedEntityId = null;
        m_entityNameField.setText("");
        m_secretEntityCheckbox.setSelected(false);
        m_entityDetails.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void clearAllData() {
        clearDisplayedEntity();
        m_campaignTree.clear();
        m_campaignTitleLabel.setText("");
        //Reset nav path to null, which is how the app initializes it
        m_navPath = null;
        updateNavButtons();
    }

    /** {@inheritDoc} */
    @Override
    public void displayCampaign(Campaign campaign) {
        clearAllData();
        m_campaignTitleLabel.setText(campaign.getName());
        m_campaignTree.insertEntities(campaign.getEntities());
    }

    /** {@inheritDoc} */
    @Override
    public void showEntity(UUID id) {
        Entity toShow = m_cdm.getEntity(id);
        displayEntity(toShow);
    }

    /**
     * Displays the supplied Entity.
     * @param entity the Entity to display.
     */
    private void displayEntity(Entity entity) {
        if (!isCurrentDataSaved()){
            int response = isSaveDesired();
            if (response == JOptionPane.YES_OPTION) {
                commitDisplayedDataToCdm();
                SaveHelper.autosave(m_frame, m_cdm, true);
            } else if (response == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        // Clear out the old data first
        clearDisplayedEntity();

        // If valid data was set, display it.
        if (entity != null) {
            m_displayedEntityId = entity.getId();
            m_entityNameField.setText(entity.getName());
            m_entityDetails.displayEntityDetails(entity, m_cdm.getRelationshipsForEntity(m_displayedEntityId).getAllRelationships());
            // Need to reset type selector after setting the entity details itself since type change "repopulates" with "current" data to 
            // catch a switch of entity/plot panels.  "Current" display therefore needs to be updated first to avoid mismatch.
            m_typeSelector.setSelectedItem(entity.getType());
            m_secretEntityCheckbox.setSelected(entity.isSecret());

            // Update the nav history.
            updateNavHistory(entity.getId());
            updateNavButtons();
            
            //Select the Entity in the tree
            m_campaignTree.selectEntity(entity);
        }
    }

    /** Launches the display window of this application. */
    public void launch() {
        m_frame.pack();
        DisplayUtilities.positionWindowInDisplayCenter(m_frame, WINDOW_SIZE);
        m_searchBox.requestFocus();
        updateNavButtons();
        m_frame.setVisible(true);
        
        //Now that the frame is visible, set the split divider location to a reasonable size
        m_entitySplitPane.setDividerLocation(0.23);
    }

    /** Initialize this display's components. */
    private void initialize() {
        m_frame = new JFrame("Campaign Encyclopedia - " + VERSION);
        try {
            m_frame.setIconImage(ImageIO.read(new File("./assets/app.png")));
        } catch (IOException ex) {
            LOGGER.log(Level.CONFIG, "Unable to load application icon.", ex);
        }
        m_frame.setPreferredSize(WINDOW_SIZE);
        m_frame.setLayout(new BorderLayout());
        m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Creating the containing panel (have to use this instead of just the JFrame
        // directly in order to support the input map since JFrame is not a JComponent.
        JPanel panel = new JPanel(new BorderLayout());

        // Set up input map action for putting the cursor in the find text box.
        String findHotKey = "find";
        AbstractAction find = new AbstractAction(findHotKey) {
            @Override
            public void actionPerformed(ActionEvent ae) {
                m_searchBox.requestFocus();
                m_searchBox.selectAll();
            }
        };
        InputMap inputMap = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK), findHotKey);
        panel.getActionMap().put(findHotKey, find);

        // Add Title/Search bar
        panel.add(createTitleBar(), BorderLayout.NORTH);

        // Create entity list.
        Component entityList = createEntityList();
        // Create entity editor
        Component entityDisplay = createEntityDisplay();
        
        // Add entity components in a split pane and create resize listener for the tree/list
        m_entitySplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, entityList, entityDisplay);
        m_entitySplitPane.setDividerSize(7);
        panel.add(m_entitySplitPane, BorderLayout.CENTER);
        
        // Create and set main menu
        m_menuManager = new MenuManager(m_frame, this, m_cdm);
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(m_menuManager.getFileMenu());
        menuBar.add(m_menuManager.getExportMenu());
        menuBar.add(m_menuManager.getCampaignMenu());
        menuBar.add(m_menuManager.getViewMenu());
        menuBar.add(m_menuManager.getHelpMenu());

        // Add Components to the Frame.
        m_frame.setJMenuBar(menuBar);
        m_frame.add(panel, BorderLayout.CENTER);

    }

    /** Returns true if the currently displayed data is saved. */
    private boolean isCurrentDataSaved() {
        // If a valid entity is shown...
        if (isEntityContentCommittable()) {
            // And if the entity displayed has an ID, get the Entity from from the CDM, and compare the two.
            if (m_displayedEntityId != null) {
                Entity cdmEntity = m_cdm.getEntity(m_displayedEntityId);
                Entity displayedEntity = getDisplayedEntity();

                // If the two are not equal, changes have been made...
                if (!displayedEntity.equals(cdmEntity)) {
                    return false;
                }

                // Or if the Relationship Data has changed, return false...
                RelationshipManager rm = m_cdm.getRelationshipsForEntity(m_displayedEntityId);
                if (!rm.getAllRelationships().equals(m_entityDetails.getRelationships())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Prompts the user to determine if they would like to save any changes that have been made
     * and returns the user's choice (true if they desire to save, false otherwise).
     * @return true if save is desired, false otherwise.
     */
    private int isSaveDesired() {
        return JOptionPane.showConfirmDialog(m_frame,
                                                     "The displayed data has changed, do\n" +
                                                     "you want to keep these changes?",
                                                     "Save Current Changes",
                                                     JOptionPane.YES_NO_CANCEL_OPTION);
    }

    /**
     * Creates the Entity display.
     * @return a JPanel which contains an Entity display.
     */
    private JPanel createEntityDisplay() {
        // Init Components
        m_entityNameField = new JTextField(20);
        m_entityNameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                edited();
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                edited();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                edited();
            }
        });

        m_secretEntityCheckbox = new JCheckBox("Secret");
        m_secretEntityCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                edited();
            }
        });

        m_commitEntityButton = new JButton();
        AbstractAction save = new AbstractAction("Save Item") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                commitDisplayedDataToCdm();
                // Always includes secret data.
                SaveHelper.autosave(m_frame, m_cdm, true);
            }
        };
        m_commitEntityButton.setAction(save);
        m_commitEntityButton.setToolTipText("Save this item, (CTRL+S)");
        m_commitEntityButton.setEnabled(false);
        String saveKey = "Save";
        InputMap saveInputMap = m_commitEntityButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        saveInputMap.put(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK), saveKey);
        m_commitEntityButton.getActionMap().put(saveKey, save);

        m_newEntityButton = new JButton();
        AbstractAction clear = new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!isCurrentDataSaved()) {
                    int response = isSaveDesired();
                    if (response == JOptionPane.YES_OPTION) {
                        commitDisplayedDataToCdm();
                    } else if (response == JOptionPane.CANCEL_OPTION) {
                        // Do Nothing.
                        return;
                    }
                }
                // Finally, clear the displayed contents.
                clearDisplayedEntity();
                m_entityNameField.requestFocus();
            }
        };
        m_newEntityButton.setAction(clear);
        m_newEntityButton.setToolTipText("Clear data for a new item, (CTRL+N)");
        String clearKey = "clearKey";
        InputMap clearInputMap = m_newEntityButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        clearInputMap.put(KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK), clearKey);
        m_newEntityButton.getActionMap().put(clearKey, clear);

        m_typeSelector = new JComboBox<>();
        for (EntityType type : EntityType.values()) {
            m_typeSelector.addItem(type);
        }
        m_typeSelector.setEditable(true);  //Must be marked as editable in order for the editor component to work below
        m_typeSelector.setRenderer(new ColoredDisplayableCellRenderer());
        m_typeSelector.setEditor(new ColoredDisplayableComboBoxEditor());
        m_typeSelector.setBorder(BorderFactory.createLineBorder(MetalLookAndFeel.getTextHighlightColor()));
        m_typeSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //Ignore deselect, only do work on selection
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                
                //Type has been changed/reselected, so see if we need to swap out the details view
                Entity currentEntity = getDisplayedEntity();
                Set<Relationship> currentRelationships = m_entityDetails.getRelationships();
                //Easiest way is to reset the current entity on it to update domain (using currently displayed data)
                m_entityDetails.displayEntityDetails(currentEntity, currentRelationships);
            }
        });
        
        m_entityDetails = new SwitchableEntityDetailsDisplay(m_frame, m_cdm, this);
        m_entityDetails.addEditListener(this);


        // Layout display
        Insets insets = new Insets(3, 3, 3, 3);
        // Create Top Row Panel --> Name / Is Secret / Type / Clear Btn / Add Btn
        JPanel topRow = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = insets;
        topRow.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0f;
        topRow.add(m_entityNameField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0f;
        topRow.add(m_secretEntityCheckbox, gbc);

        gbc.gridx = 3;
        topRow.add(m_typeSelector, gbc);

        gbc.gridx = 4;
        topRow.add(m_newEntityButton, gbc);

        gbc.gridx = 5;
        topRow.add(m_commitEntityButton, gbc);

        
        //Add top row
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(topRow, BorderLayout.NORTH);
        
        //Add details for relationships etc.
        panel.add(m_entityDetails.getDisplayableComponent(), BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Creates and returns the listing of entities.
     * @return the Entity list component.
     */
    private Component createEntityList() {
        //Create the entity tree/list and add the current entities
        m_campaignTree = new CampaignTree();
        m_entityTreeComponent = m_campaignTree.getComponent();
        m_campaignTree.insertEntities(m_cdm.getAllEntities());
        
        // Setup Mouse Listener
        m_entityTreeComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                Entity selectedEntity = m_campaignTree.getSelectedEntity();
                if (me.getClickCount() > 1 && selectedEntity != null) {
                    displayEntity(selectedEntity);
                } else if (SwingUtilities.isRightMouseButton(me) && selectedEntity != null) {
                    //Create a context menu for right click      
                    JPopupMenu contextMenu = m_menuManager.getEntityContextMenu(selectedEntity);
                    contextMenu.show(m_campaignTree.getComponent(), me.getX(), me.getY());
                }
            }

        });

        // Setup Key Listener
        m_entityTreeComponent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ke) {
                //Get the thing currently selected
                Entity selectedEntity = m_campaignTree.getSelectedEntity();
                if (ke.getKeyChar() == KeyEvent.VK_ENTER && selectedEntity != null) {
                    displayEntity(selectedEntity);
                } else if (ke.getKeyChar() == KeyEvent.VK_DELETE && selectedEntity != null) {
                    DeleteEntityAction dea = new DeleteEntityAction(m_frame, selectedEntity, m_cdm, MainDisplay.this);  //MainDisplay.this accesses the instance of containing class
                    dea.actionPerformed(new ActionEvent(ke.getSource(), ke.getID(), ""));
                }
            }
        });
        
        return new JScrollPane(m_entityTreeComponent);
    }

    
    /**
     * Create title bar and filter controls.
     * @return a JPanel containing the title bar and filter controls.
     */
    private JPanel createTitleBar() {
        // Init
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(BLUE);
        m_campaignTitleLabel = new JLabel(m_cdm.getData().getName());
        m_campaignTitleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        m_campaignTitleLabel.setForeground(SILVER);

        m_filterCheckBox = new JCheckBox("Hide Secret Items");
        m_filterCheckBox.setOpaque(true);
        m_filterCheckBox.setBackground(BLUE);
        m_filterCheckBox.setForeground(SILVER);
        m_filterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                updateEntityFilter();
            }
        });
        
        m_entityTypeFilterComboBox = new JComboBox<>();
        m_entityTypeFilterComboBox.addItem(new ColoredDisplayable() {
            @Override
            public Color getColor() {
                return Color.BLACK;
            }
            @Override
            public String getDisplayString() {
                return "All";
            }
        });
        for (EntityType type : EntityType.values()) {
            m_entityTypeFilterComboBox.addItem(type);
        }
        m_entityTypeFilterComboBox.setEditable(true);
        m_entityTypeFilterComboBox.setRenderer(new ColoredDisplayableCellRenderer());
        m_entityTypeFilterComboBox.setEditor(new ColoredDisplayableComboBoxEditor());
        m_entityTypeFilterComboBox.setBorder(BorderFactory.createLineBorder(MetalLookAndFeel.getTextHighlightColor()));
        m_entityTypeFilterComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (ie.getStateChange() == ItemEvent.SELECTED) {
                    updateEntityFilter();
                }
            }
        });
        
        m_searchBox = new JTextField(18);
        m_searchBox.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {
                updateEntityFilter();
            }
            @Override
            public void removeUpdate(DocumentEvent de) {
                updateEntityFilter();
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                updateEntityFilter();
            }
        });
        m_searchBox.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent ke) {
            }
            @Override
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyChar() == KeyEvent.VK_ENTER) {
                    //Select the first thing when we hit enter  //TODO enter select on search for tree
//                    if (m_entityTreeModel.getSize() > 0) {
//                        m_entityList.setSelectedIndex(0);
//                    }
                    m_entityTreeComponent.requestFocus();
                }
            }
            @Override
            public void keyReleased(KeyEvent ke) {
            }

        });

        // NAVIGATE BACKWARD BUTTON
        m_backButton = new JButton("Last");
        m_backButton.setOpaque(false);
        m_backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                navigateBackward();
            }
        });
        m_backButton.setToolTipText("Click to go back");

        // NAVIGATE FORWARD BUTTON
        m_forwardButton = new JButton("Next");
        m_forwardButton.setOpaque(false);
        m_forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                navigateForward();
            }
        });
        m_forwardButton.setToolTipText("Click to go forward");

        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 0.0f;
        panel.add(m_campaignTitleLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0f;
        panel.add(new JLabel(), gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0f;
        panel.add(m_filterCheckBox, gbc);

        gbc.gridx = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0f;
        panel.add(m_entityTypeFilterComboBox, gbc);
        
        gbc.gridx = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0f;
        panel.add(m_searchBox, gbc);

        gbc.gridx = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0f;
        panel.add(m_backButton, gbc);

        gbc.gridx = 6;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0f;
        panel.add(m_forwardButton, gbc);

        return panel;
    }

    /**
     * Returns a new EntityDisplayFilter or null if no valid filter is set.
     * @return a new EntityDisplayFilter or null if no valid filter is set.s
     */
    private void updateEntityFilter() {
        String searchString = m_searchBox.getText().trim();
        Object type = m_entityTypeFilterComboBox.getSelectedItem();
        boolean showSecrets = !m_filterCheckBox.isSelected();
        if (type instanceof EntityType) {
            m_campaignTree.filterTree(new EntityDisplayFilter(searchString, (EntityType)type, showSecrets));
        } else if (!"".equals(searchString) || !showSecrets){
            //the "ALL" category was snuck into the combobox, and so is not an entity type.
            //This is the ALL category as long as there is search text or we are hiding secrets
            m_campaignTree.filterTree(new EntityDisplayFilter(searchString, null, showSecrets));
        } else {
            //No search string, ALL category, and don't hide secrets.  Use null for no filtering/
            m_campaignTree.filterTree(null);
        }
    }

    /**
     * Given the supplied UUID, update the navigation history.
     * @param id the ID to update.  If null, navigation history is cleared.
     */
    private void updateNavHistory(UUID id) {
        if (id != null) {
            if (m_navPath == null) {
                m_navPath = new NavigationPath(id);
            } else {
                UUID currentId = m_navPath.getCurrentId();
                //Protect against empty list, if there was nothing already or current is different
                if (currentId == null || !currentId.equals(id)) {
                    m_navPath.add(id);
                }
            }
        }
    }

    /** Called to update the navigation buttons. */
    private void updateNavButtons() {
        if (m_navPath != null) {
            m_backButton.setEnabled(m_navPath.isBackPossible());
            m_forwardButton.setEnabled(m_navPath.isForwardPossible());
        } else {
            m_backButton.setEnabled(false);
            m_forwardButton.setEnabled(false);
        }
    }

    @Override
    public void navigateForward() {
        if (m_navPath != null) {
            if (m_navPath.forward()){
               showEntity(m_navPath.getCurrentId());
               updateNavButtons();
            }
        }
    }

    @Override
    public void navigateBackward() {
        if (m_navPath != null) {
            if (m_navPath.back()){
               showEntity(m_navPath.getCurrentId());
               updateNavButtons();
            }
        }
    }
}