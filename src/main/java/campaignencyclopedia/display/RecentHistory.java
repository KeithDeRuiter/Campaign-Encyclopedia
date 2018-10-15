package campaignencyclopedia.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A class to contain the state of a history list of IDs, and the current position/index into that 
 * list.  The 'cursor' position is clamped to within the bounds of the size of the list, or set 
 * to -1 for empty lists upon construction.
 * @author adam
 */
public class RecentHistory {
    
    /** The list of history being stored. */
    private List<UUID> m_recent;
    
    /** The stored position/index into the history list. */
    private int m_current;
    
    /**
     * Constructs a new RecentHistory object.  If an empty list is provided, then the cursor will 
     * automatically be set to -1 regardless of the value passed in.  Cursor values less than 0 are 
     * forced to 0, and cursor values greater than .
     * @param recent The list of recent items, cannot be null.
     * @param current The current position in the history list provided.
     * 
     */
    public RecentHistory(List<UUID> recent, int current) {
        if (recent == null) {
            throw new IllegalArgumentException("Param 'recent' cannot be null.");
        }
        
        if (recent.isEmpty()) {
            //List empty, so set cursor to -1
            m_current = -1;
        } else if (current < 0) {
            //List was not empty, but you provided a negative current cursor position, so clamp to 0.
            m_current = 0;
        } else if (current > recent.size() - 1) {
            //List was not empty, but you provided a current cursor position larger than the list, so clamp to the last value.
            m_current = recent.size() - 1;
        } else {
            m_current = current;
        }
        m_recent = new ArrayList<>(recent);
    }
    
    /**
     * Get the list of all recent history, as provided to this object at construction.
     * @return An unmodifiable list of the recent history.
     */
    public List<UUID> getRecentHistory() {
        return Collections.unmodifiableList(m_recent);
    }
    
    /**
     * Get the current index into the recent history provided to this object at construction.
     * Note the restrictions 
     * @return 
     */
    public int getCurrentIndex() {
        return m_current;
    }
}