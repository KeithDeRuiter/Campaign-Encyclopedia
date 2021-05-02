package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.CampaignDataManager;
import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.EntityType.EntityDomain;
import campaignencyclopedia.data.Relationship;
import campaignencyclopedia.display.EntityDisplay;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Frame;
import java.util.Set;
import javax.swing.JPanel;
import toolbox.display.EditListener;

/**
 * A display capable of showing different details components for world vs plot entities, and
 * switching between the two based on the content being displayed.
 * @author Keith
 */
public class SwitchableEntityDetailsDisplay implements EntityDetailsDisplay {
    
    /** The display switched to when showing a world-domain entity. */
    private final EntityDetailsDisplay m_worldDetails;
    
    /** The display switched to when showing a plot-domain entity. */
    private final EntityDetailsDisplay m_plotDetails;
    
    /** The currently switched display, set back and forth as the display shows world vs plot entities. */
    private EntityDetailsDisplay m_currentDisplay;

    /** The main component to display. */
    private final JPanel m_component;
    
    /** The card layout to switch between world and plot components. */
    private final CardLayout m_cards;
    
    /** The card key value for the world entity display. */
    private static final String WORLD_CARD = "WORLD_CARD";
    
    /** The card key value for the plot entity display. */
    private static final String PLOT_CARD = "PLOT_CARD";
    
    
    /**
     * Constructs a new instance of the display.
     * @param frame The frame to use for centering of spawned dialogs.
     * @param cdm The data manager for retrieval and storage of campaign data, such as modifying relationships.
     * @param display A display to trigger showing entities upon request, such as when you navigate a relationship.
     */
    public SwitchableEntityDetailsDisplay(Frame frame, CampaignDataManager cdm, EntityDisplay display) {
        //Create the two internal displays to switch between, choosing default as the current one
        m_worldDetails = new DefaultEntityDetailsDisplay(frame, cdm, display);
        m_plotDetails = new PlotEntityDetailsDisplay(frame, cdm, display);
        m_currentDisplay = m_worldDetails;
        
        //Create the UI panel itself and add the internal displays as cards
        m_cards = new CardLayout();
        m_component = new JPanel(m_cards);
        m_component.add(m_worldDetails.getDisplayableComponent(), WORLD_CARD);
        m_component.add(m_plotDetails.getDisplayableComponent(), PLOT_CARD);
    }
    
    /**
     * This clears both the plot and world internal displays, regardless of which is currently visible.
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        m_worldDetails.clear();
        m_plotDetails.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getDisplayableComponent() {
        return m_component;
    }
    
    /**
     * When this entity is displayed, the current view will update to reflect the domain of the entity provided.
     * If the entity's type is in the plot domain, a {@link PlotEntityDetailsDisplay} will be used,
     * otherwise the {@link DefaultEntityDetailsDisplay} will be used.  This selection of the
     * "current display" affects what data will be returned from the getters on this class, which
     * may differ when the currently visible display is edited by the user.
     * {@inheritDoc}
     */
    @Override
    public void displayEntityDetails(Entity entity, Set<Relationship> relationships) {
        //Set both, just for consistency's sake
        m_worldDetails.displayEntityDetails(entity, relationships);
        m_plotDetails.displayEntityDetails(entity, relationships);
        
        if (entity.getType().getDomain().equals(EntityDomain.PLOT)) {
//        if (true) {
            m_currentDisplay = m_plotDetails;
            m_cards.show(m_component, PLOT_CARD);
        } else {
            m_currentDisplay = m_worldDetails;
            m_cards.show(m_component, WORLD_CARD);
        }
    }

    /**
     * This implementation sets the public data on both the world and plot internal displays.
     * {@inheritDoc}
     */
    @Override
    public void setPublicData(EntityData publicData) {
        m_worldDetails.setPublicData(publicData);
        m_plotDetails.setPublicData(publicData);
    }

    /**
     * This implementation returns the public data of the currently visible display (plot vs world)
     * {@inheritDoc} 
     */
    @Override
    public EntityData getPublicData() {
        return m_currentDisplay.getPublicData();
    }

    /** 
     * This implementation sets the secret data on both the world and plot internal displays.
     * {@inheritDoc} 
     */
    @Override
    public void setSecretData(EntityData secretData) {
        m_worldDetails.setSecretData(secretData);
        m_plotDetails.setSecretData(secretData);
    }

    /**
     * This implementation returns the secret data of the currently visible display (plot vs world)
     * {@inheritDoc}
     */
    @Override
    public EntityData getSecretData() {
        return m_currentDisplay.getSecretData();
    }

    /**
     * This implementation returns the relationships of the currently visible display (plot vs world)
     * {@inheritDoc}
     */
    @Override
    public Set<Relationship> getRelationships() {
        return m_currentDisplay.getRelationships();
    }

    /**
     * This implementations sets the relationships on both the world and plot internal displays.
     * {@inheritDoc)
     */
    @Override
    public void setRelationships(Set<Relationship> relationships) {
        m_worldDetails.setRelationships(relationships);
        m_plotDetails.setRelationships(relationships);
    }

    /**
     * The listener is added to both the plot and world internal displays.
     * {@inheritDoc}
     */
    @Override
    public void addEditListener(EditListener listener) {
        m_worldDetails.addEditListener(listener);
        m_plotDetails.addEditListener(listener);
    }

    /**
     * The listener is removed from both the plot and world internal displays.
     * {@inheritDoc}
     */
    @Override
    public void removeEditListener(EditListener listener) {
        m_worldDetails.removeEditListener(listener);
        m_plotDetails.removeEditListener(listener);
    }
    
}
