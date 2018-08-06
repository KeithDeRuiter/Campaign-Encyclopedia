package campaignencyclopedia.display.swing.graphical;

import campaignencyclopedia.data.EntityType;
import java.awt.Color;

/**
 * The Graphical Color Pallette.
 * @author adam
 */
public class Colors {
    
    /** The color to render places in. */
    static final Color PLACE = new Color(64, 160, 64);

    /** The Color to render PCs. */
    static final Color PC = new Color(72, 72, 232);

    /** The Color to render NPCs. */
    static final Color NPC = new Color(180, 64, 255);

    /** The Color to render Items. */
    static final Color ITEM = new Color(255, 128, 64);

    /** The Color to render Events. */
    static final Color EVENT = new Color(160, 64, 64);

    /** The Color to render Organizations. */
    static final Color ORG = new Color(128, 128, 128);

    /** The Color to render Lines. */
    static final Color LINE = new Color(84, 84, 84);

    /** The Color to render Clues. */
    static final Color PLOT_CLUE = Color.YELLOW;

    /** The Color to render Conclusions. */
    static final Color PLOT_CONCLUSION = Color.YELLOW.darker();
    
    /** The Color to render Unknown things. */
    static final Color UNKNOWN = new Color(164, 164, 164);
    
 
    /**
     * Returns the color for a given Entity Type.
     * @param type the Entity Type you want the color for.
     * @return the color for a given Entity Type.
     */
    public static Color getColor(EntityType type) {
        switch (type) {
            case PLAYER_CHARACTER:
                return PC;
            case NON_PLAYER_CHARACTER:
                return NPC;
            case PLACE:
                return PLACE;
            case ITEM:
                return ITEM;
            case ORGANIZATION:
                return ORG;
            case EVENT:
                return EVENT;
            case PLOT_CLUE:
                return PLOT_CLUE;
            case PLOT_CONCLUSION:
                return PLOT_CONCLUSION;
        }
        return UNKNOWN;
    }
}
