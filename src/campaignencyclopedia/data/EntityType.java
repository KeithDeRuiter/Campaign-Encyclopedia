package campaignencyclopedia.data;

import campaignencyclopedia.display.swing.graphical.Colors;
import java.awt.Color;

/**
 * An enumeration of the different types of entities.
 * @author adam
 */
public enum EntityType implements ColoredDisplayable {
    NON_PLAYER_CHARACTER("NPC", EntityDomain.WORLD),
    PLAYER_CHARACTER("PC", EntityDomain.WORLD),
    PLACE("Place", EntityDomain.WORLD),
    ITEM("Item", EntityDomain.WORLD),
    EVENT("Event", EntityDomain.WORLD),
    ORGANIZATION("Organization", EntityDomain.WORLD),
    PLOT_CLUE("Clue", EntityDomain.PLOT),
    PLOT_CONCLUSION("Conclusion", EntityDomain.PLOT);

    /** The display String for the EntityType. */
    private final String m_displayString;

    /** The which domain this entity type belongs to. */
    private final EntityDomain m_domain;

    /**
     * Creates a new EntityType.
     * @param display the display string.
     */
    private EntityType(String display, EntityDomain domain) {
        m_displayString = display;
        m_domain = domain;
    }

    /**
     * Gets the domain of this entity, suitable for separation of broad type groups.
     * @return The domain that this type of entity belongs to.
     */
    public EntityDomain getDomain() {
        return m_domain;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getDisplayString() {
        return m_displayString;
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor() {
        return Colors.getColor(this);
    }
    
    /**
     * A Categorization of the domains of entities, representing distinct separate purposes
     * such as types related to the world itself, or types related to management of the plot.
     */
    public enum EntityDomain {
        WORLD,
        PLOT;
    }
}