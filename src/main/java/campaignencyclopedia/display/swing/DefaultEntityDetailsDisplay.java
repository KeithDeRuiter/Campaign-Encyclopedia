package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import toolbox.display.EditListener;

/**
 * A display for the entity detail information: relationships, tags, and descriptions.
 * This display allows display and editing of public and secret detail data.
 * @author Keith
 */
public class DefaultEntityDetailsDisplay implements EntityDetailsDisplay {

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

    /** An EntityDataDisplay for secret data. */
    private EntityDataEditor m_secret;

    /** An editor for Entity Relationship data. */
    private EntityRelationshipEditor m_relationshipEditor;


    /**
     * Constructs a new display and initializes the layout of the components.
     * @param frame The parent frame, used for centering spawned dialogs.
     * @param cdm The data manager, required for edits/refreshes of relationship data.
     * @param display A display to request display of entities, such as when navigating relationships.
     */
    public DefaultEntityDetailsDisplay(Frame frame, CampaignDataManager cdm, EntityDisplay display) {
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
    }
    
    /** 
     * {@inheritDoc}
     * This implementation just forwards calls to the other appropriate setters as a convenience.
     */
    @Override
    public void displayEntityDetails(Entity entity, Set<Relationship> relationships) {
        setPublicData(entity.getPublicData());
        setSecretData(entity.getSecretData());
        setRelationships(relationships);
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
        //Unmodifiable AND copied for safety to prevent setRelationships(getRelationships()) from clearing 
        // out its own relationships it is trying to set on itself, thus deleting them unintentionally.
        // Important for a "reset the same data on yourself" case, namely type selector dropdown
        return Collections.unmodifiableSet(new HashSet<>(m_relationshipEditor.getData()));
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
        mainGbc.insets = insets;

        // FIRST COLUMN
        // --- Public Description Label
        mainGbc.gridwidth = 2;
        mainGbc.weighty = 0.0f;
        mainGbc.weightx = 1.0f;
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.anchor = GridBagConstraints.PAGE_END;
        panel.add(m_public.getDescriptionEditor().getTitle(), mainGbc);
        mainGbc.weightx = 0.0f;

        // --- Public Description Editor Component
        mainGbc.gridy = 1;
        mainGbc.weighty = 1.0f;
        JScrollPane publicDescriptionScrollPane = new JScrollPane(m_public.getDescriptionEditor().getDescriptionComponent());
        publicDescriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        publicDescriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        publicDescriptionScrollPane.setPreferredSize(new Dimension(2, 2));
        panel.add(publicDescriptionScrollPane, mainGbc);

        // --- Relationships Label
        mainGbc.gridy = 2;
        mainGbc.weighty = 0.0f;
        panel.add(m_relationshipEditor.getTitle(), mainGbc);

        // --- Public Editor Component
        mainGbc.gridy = 3;
        mainGbc.gridheight = 4;
        mainGbc.weighty = 1.0f;
        mainGbc.fill = GridBagConstraints.BOTH;
        JScrollPane relationShipScrollPane = new JScrollPane(m_relationshipEditor.getEditorComponent());
        relationShipScrollPane.setPreferredSize(new Dimension(2, 2));
        panel.add(relationShipScrollPane, mainGbc);

        // --- Add Relationship Button
        mainGbc.gridy = 1;
        mainGbc.gridy = 7;
        mainGbc.weighty = 0.0f;
        mainGbc.gridwidth = 1;
        mainGbc.gridheight = 1;
        mainGbc.fill = GridBagConstraints.NONE;
        mainGbc.anchor = GridBagConstraints.LAST_LINE_START;
        panel.add(m_relationshipEditor.getAddRelationshipButton(), mainGbc);

        // SECOND COLUMN
        // --- Secret Description Label
        mainGbc.gridx = 2;
        mainGbc.gridy = 0;
        mainGbc.gridwidth = 2;
        mainGbc.weighty = 0.0f;
        mainGbc.weightx = 1.0f;
        mainGbc.fill = GridBagConstraints.BOTH;
        panel.add(m_secret.getDescriptionEditor().getTitle(), mainGbc);
        mainGbc.weightx = 0.0f;

        // --- Secret Description Editor Component
        mainGbc.gridy = 1;
        mainGbc.weighty = 1.0f;
        JScrollPane secretDescriptionScrollPane = new JScrollPane(m_secret.getDescriptionEditor().getDescriptionComponent());
        secretDescriptionScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        secretDescriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        secretDescriptionScrollPane.setPreferredSize(new Dimension(2, 2));
        panel.add(secretDescriptionScrollPane, mainGbc);

        // --- Public Tags Label
        mainGbc.gridy = 2;
        mainGbc.weighty = 0.0f;
        panel.add(m_public.getTagsEditor().getTitle(), mainGbc);

        // --- Public Tags Editor
        mainGbc.gridy = 3;
        mainGbc.gridheight = 1;
        mainGbc.weighty = 0.1f;
        mainGbc.fill = GridBagConstraints.BOTH;
        JScrollPane pubTagScrollPane = new JScrollPane(m_public.getTagsEditor().getEditorComponent());
        pubTagScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pubTagScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pubTagScrollPane.setPreferredSize(new Dimension(2, 2));
        panel.add(pubTagScrollPane, mainGbc);

        // --- Secret Tags Label
        mainGbc.gridy = 4;
        mainGbc.gridheight = 1;
        mainGbc.weighty = 0.0f;
        panel.add(m_secret.getTagsEditor().getTitle(), mainGbc);

        // --- Secret Tags Editor
        mainGbc.gridy = 5;
        mainGbc.gridheight = 2;
        mainGbc.weighty = 0.1f;
        JScrollPane secretTagScrollPane = new JScrollPane(m_secret.getTagsEditor().getEditorComponent());
        secretTagScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        secretTagScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        secretTagScrollPane.setPreferredSize(new Dimension(2, 2));
        panel.add(secretTagScrollPane, mainGbc);

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
    
    /**
     * Notifies listeners when the contents of this display have been edited.
     * @see EditListener
     */
    protected void fireEdited() {
        for (EditListener e : m_listeners) {
            e.edited();
        }
    }    

}
