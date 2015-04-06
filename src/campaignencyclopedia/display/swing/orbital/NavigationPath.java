package campaignencyclopedia.display.swing.orbital;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

/**
 * A class that tracks orbital display Entity navigation history, much like a web browser's page history.
 * @author adam
 */
public class NavigationPath {
    
    /** The history data. */
    private final List<UUID> m_history;
    
    /** The current navigation path position. */
    private int m_cursor;
    
    /** The maximum number of elements to give back as recent history. */
    private static final int RECENT_HISTORY_SIZE = 5;
    
    public NavigationPath(UUID start) {
        if (start == null) {
            throw new IllegalArgumentException("Supplied starting UUID is null.");
        }
        m_history = new ArrayList<>();
        m_history.add(start);
        m_cursor = 0;
    }
    
    public void add(UUID navigateTo) {
        // Clear all after currently selected
        if (m_history.size() - 1 > m_cursor) {
            for (int i = m_cursor + 1; i < m_history.size(); i++) {
                m_history.remove(i);
            }
        }
        
        // Add new item to the history.
        m_history.add(navigateTo);
        
        // If we have more than a hundred entries and we're adding 
        // a new one, remove zero and don't increment index.
        if (m_history.size() > 100) {
            m_history.remove(0);
        } else {
            // Otherwise, Update index
            m_cursor++;            
        }
    }

    /**
     * Updates the cursor position forward one position and returns true.  If current data 
     * makes this impossible, returns false.
     * @return true after moving the cursor forward one position, false otherwise.
     */
    public boolean forward() {
        if (m_history.size() > 0 && m_cursor != (m_history.size() - 1)) {
            m_cursor++;
            return true;
        }
        
        return false;
    }
    
    /**
     * Updates the cursor position backwards one position and returns true.  If current data 
     * makes this impossible, returns false.
     * @return true after moving the cursor back one position, false otherwise.
     */
    public boolean back() {
        if (m_history.size() > 0 && m_cursor > 0) {
            m_cursor--;
            return true;
        }
        
        return false;
    }
    
    public UUID getCurrentId() {
        return m_history.get(m_cursor);
    }

    /**
     * Returns the recent history of navigation, as managed by this object.  If the recent history is empty, an empty 
     * history is returned with a cursor of -1.
     * 
     * @return the recent history of navigation.
     */
    RecentHistory getRecentHistory() {
        // If more than max recent history values...
        List<UUID> history = new ArrayList<>();
        int curentPosition = 0;
        
        // If we're empty, return an empty history.
        if (m_history.isEmpty()) {
            return new RecentHistory(new ArrayList<UUID>(), -1);
        }
        
        // Check forwards
        ListIterator<UUID> iter = m_history.listIterator(m_cursor);
        int forwardAdds = 0;
        while (iter.hasNext() && forwardAdds <= 2) {
            history.add(iter.next());
            forwardAdds++;
        }
        
        // Check backwards
        iter = m_history.listIterator(m_cursor);
        while (iter.hasPrevious() && history.size() <= 4) {
            history.add(0, iter.previous());
            curentPosition++;
        }
        
        return new RecentHistory(history, curentPosition);
    }
    
    /**
     * Returns an unmodifiable copy of the history.
     * @return an unmodifiable copy of the history.
     */
    public List<UUID> getHistory() {
        return Collections.unmodifiableList(m_history);
    }

    /**
     * Remove all instances of the supplied ID.
     * @param id the ID to expunge from the Navigation Path.
     */
    void removeAll(UUID id) {
        while (m_history.contains(id)) {
            int index = m_history.indexOf(id);
            if (index < m_cursor) {
                m_cursor = m_cursor - 1;
            }
            m_history.remove(id);
        }
    }
}