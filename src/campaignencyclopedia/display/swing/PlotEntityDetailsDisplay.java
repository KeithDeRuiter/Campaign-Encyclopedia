package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.display.EntityDisplay;
import campaignencyclopedia.display.swing.graphical.PlotEntityCanvas;
import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import toolbox.display.EditListener;

/**
 * A display for the entity details of plot-domain entities.  This display contains
 * specific visualizations for entities in the Plot domain in addition to the regular editors.
 * In addition, this display only deals with public details on EntityData such as description 
 * and tags, and does not contain a display for secret data.  This data is maintained from the 
 * state an Entity was in when handed to the display for visualization.
 * @author Keith
 */
public class PlotEntityDetailsDisplay implements EntityDetailsDisplay {

    /** The component to return as the visualization of this display. */
    private final Component m_component;
    
    /** Parent frame (type dictated by library), used to center dialogs launched for relationships. */
    private final Frame m_frame;
    
    /** How we access and save all data. */
    private final CampaignDataManager m_cdm;
    
    /** Allows display of whole new entities, e.g. when navigating relationships. */
    private final EntityDisplay m_entityDisplay;
    
    /** The list of listeners for when this entity is fireEdited. */
    private final List<EditListener> m_listeners;
    
    /** An edit listener to add for any of the sub-components that just forwards the event. */
    private EditListener m_forwardingListener;

    /** The EntityData Display for public data. */
    private EntityDataEditor m_public;

    /** The EntityData Display for caching secret data on entities passed in.  This display is never shown */
    private EntityDataEditor m_secret;

    /** An editor for Entity Relationship data. */
    private EntityRelationshipEditor m_relationshipEditor;
    
    /** The graphical display of the plot entity and its plot-related relationships. */
    private PlotEntityCanvas m_visualization;


    /**
     * Constructs a new display and initializes the layout of the components.
     * @param frame The parent frame, used for centering spawned dialogs.
     * @param cdm The data manager, required for edits/refreshes of relationship data.
     * @param display A display to request display of entities, such as when navigating relationships.
     */
    public PlotEntityDetailsDisplay(Frame frame, CampaignDataManager cdm, EntityDisplay display) {
        m_frame = frame;
        m_cdm = cdm;
        m_entityDisplay = display;
        m_listeners = new ArrayList<>();
        m_component = createEntityDetailsDisplay();
    }
    
    /** {@inheritDoc} */
    @Override
    public Component getDisplayableComponent() {
        return m_component;
    }
    
    /** {@inheritDoc} */
    @Override
    public void clear() {
        m_public.clear();
        m_secret.clear();
        m_relationshipEditor.clearData();
        m_visualization.clearAllData();
    }
    
    /** 
     * {@inheritDoc}
     * This implementation forwards calls to the other appropriate setters as a convenience, and
     * triggers display of the entity in the visualization pane.
     */
    @Override
    public void displayEntityDetails(Entity entity, Set<Relationship> relationships) {
        setPublicData(entity.getPublicData());
        setSecretData(entity.getSecretData());
        setRelationships(relationships);
        m_visualization.show(entity);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setPublicData(EntityData publicData) {
        m_public.setEntityData(publicData);
    }
    
    /** {@inheritDoc} */
    @Override
    public EntityData getPublicData() {
        return m_public.getEntityData();
    }

    /** {@inheritDoc} */
    @Override
    public void setSecretData(EntityData secretData) {
        m_secret.setEntityData(secretData);
    }
    
    /** {@inheritDoc} */
    @Override
    public EntityData getSecretData() {
        return m_secret.getEntityData();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setRelationships(Set<Relationship> relationships) {
        m_relationshipEditor.setData(relationships);
    }
    
    /** {@inheritDoc} */
    @Override
    public Set<Relationship> getRelationships() {
        return m_relationshipEditor.getData();
    }
    
    /** 
     * Creates and initializes the display components, returning the resultant panel.
     */
    private JPanel createEntityDetailsDisplay() {
        //Prepare a listener for whatever needs it
        m_forwardingListener = new EditListener() {
            @Override
            public void edited() {
                fireEdited();
            }
        };
        
        // Init Components
        JPanel panel = new JPanel(new GridBagLayout());

        m_public = new EntityDataEditor(m_forwardingListener, false);
        m_secret = new EntityDataEditor(m_forwardingListener, true);
        m_relationshipEditor = new EntityRelationshipEditor(m_frame, m_cdm, m_entityDisplay, "Relationships", m_forwardingListener);

        Insets insets = new Insets(3, 3, 3, 3);

        // Layout display
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainGbc.weightx = 0.2f;
        mainGbc.insets = insets;
        mainGbc.fill = GridBagConstraints.BOTH;

        // FIRST COLUMN
        // --- Public Description Label
        mainGbc.weighty = 0.0f;
        mainGbc.anchor = GridBagConstraints.PAGE_END;
        panel.add(m_public.getDescriptionEditor().getTitle(), mainGbc);

        // --- Public Description Editor Component
        mainGbc.gridy = 1;
        mainGbc.weighty = 1.0f;
        JScrollPane publicDescriptionScrollPane = new JScrollPane(m_public.getDescriptionEditor().getDescriptionComponent());
        publicDescriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        publicDescriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(publicDescriptionScrollPane, mainGbc);

        // --- Public Tags Label
        mainGbc.gridy = 2;
        mainGbc.weighty = 0.0f;
        panel.add(m_public.getTagsEditor().getTitle(), mainGbc);

        // --- Public Tags Editor
        mainGbc.gridy = 3;
        mainGbc.weighty = 0.1f;
        JScrollPane pubTagScrollPane = new JScrollPane(m_public.getTagsEditor().getEditorComponent());
        pubTagScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pubTagScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(pubTagScrollPane, mainGbc);
        
        // --- Relationships Label
        mainGbc.gridy = 4;
        mainGbc.weighty = 0.0f;
        panel.add(m_relationshipEditor.getTitle(), mainGbc);

        // --- Public Editor Component
        mainGbc.gridy = 5;
        mainGbc.weighty = 1.0f;
        JScrollPane relationShipScrollPane = new JScrollPane(m_relationshipEditor.getEditorComponent());
        panel.add(relationShipScrollPane, mainGbc);

        // --- Add Relationship Button
        mainGbc.gridy = 1;
        mainGbc.gridy = 6;
        mainGbc.weighty = 0.0f;
        mainGbc.fill = GridBagConstraints.NONE;
        mainGbc.anchor = GridBagConstraints.LAST_LINE_START;
        panel.add(m_relationshipEditor.getAddRelationshipButton(), mainGbc);

        // SECOND COLUMN
        //TODO PLOT visualization
        mainGbc.gridx = 1;
        mainGbc.gridy = 0;
        mainGbc.weightx = 1.0f;
        mainGbc.gridheight = 7;
        mainGbc.fill = GridBagConstraints.BOTH;
        m_visualization = new PlotEntityCanvas(m_entityDisplay, m_cdm);
        panel.add(m_visualization.getComponent(), mainGbc);

        return panel;
    }

    /** {@inheritDoc} */
    @Override
    public void addEditListener(EditListener listener) {
        if (listener == null) {
            return;
        }
        m_listeners.add(listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeEditListener(EditListener listener) {
        if (listener == null) {
            return;
        }
        m_listeners.remove(listener);
    }
    
    /** {@inheritDoc} */
    protected void fireEdited() {
        for (EditListener e : m_listeners) {
            e.edited();
        }
    }    
}
