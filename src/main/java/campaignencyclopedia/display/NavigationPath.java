package campaignencyclopedia.display;

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

    /** The maximum number of elements to give back as recent history navigating forward. */
    private static final int MAX_FORWARD_ELEMENTS = 3;

    /** The maximum number of elements to give back as recent history navigating backward. */
    private static final int MAX_BACKWARD_ELEMENTS = 6;

    /** The maximum number of items that can be in the navigation history. */
    private static final int MAX_HISTORY_SIZE = 100;

    /**
     * Constructs a new empty NavigationPath.
     */
    public NavigationPath() {
        this(null);
    }
    
    /**
     * Constructs a new NavigationPath, starting with the provided ID.
     * Passing null will create an empty path with the cursor set at -1, requiring an ID
     * to be added before the path can begin.
     * @param start The ID to start the path at.  {@code null} is allowed, resulting in an empty path.
     */
    public NavigationPath(UUID start) {
        m_history = new ArrayList<>();
        if (start == null) {
            m_cursor = -1;
        } else {
            m_history.add(start);
            m_cursor = 0;
        }
    }

    public void add(UUID navigateTo) {
        // Clear all after currently selected
        if (m_history.size() > m_cursor + 1) {
            List<UUID> trimmedHistory = new ArrayList<>(m_history.subList(0, m_cursor + 1));
            m_history.clear();
            m_history.addAll(trimmedHistory);
        }

        // Add new item to the history.
        m_history.add(navigateTo);

        // If we have more than the max number of entries and we're adding
        // a new one, remove zero and don't increment index.
        if (m_history.size() > MAX_HISTORY_SIZE) {
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
        if (isForwardPossible()) {
            m_cursor++;
            return true;
        }
        return false;
    }

    /**
     * Returns true if it is possible to navigate forward.
     * @return true if it is possible to navigate forward.
     */
    public boolean isForwardPossible() {
        return (m_history.size() > 0 && m_cursor != (m_history.size() - 1));
    }

    /**
     * Updates the cursor position backwards one position and returns true.  If current data
     * makes this impossible, returns false.
     * @return true after moving the cursor back one position, false otherwise.
     */
    public boolean back() {
        if (isBackPossible()) {
            m_cursor--;
            return true;
        }
        return false;
    }

    /**
     * Returns true if it is possible to navigate backwards.
     * @return true if it is possible to navigate backwards.
     */
    public boolean isBackPossible() {
        return (m_history.size() > 0 && m_cursor > 0);
    }

    /**
     * Returns the current ID that the cursor is pointing at.
     * @return the current ID that the cursor is pointing at, or {@code null} if there is no cursor position (e.g. empty).
     */
    public UUID getCurrentId() {
        if (m_history.isEmpty()) {
            return null;
        }
        try {
            return m_history.get(m_cursor);
        } catch (IndexOutOfBoundsException e) {
            //We somehow got out of the range of the list, so treat is the same as if we were empty.
            //Unlikely, since the tracking is internal and isEmpty is already covered.
            return null;
        }
    }

    /**
     * Returns the recent history of navigation, as managed by this object.  If the recent history is empty, an empty
     * history is returned with a cursor of -1.
     *
     * @return the recent history of navigation.
     */
    public RecentHistory getRecentHistory() {
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
        //Start at element specified by cursor, and then cursor +1 etc.
        while (iter.hasNext() && forwardAdds <= MAX_FORWARD_ELEMENTS) {
            history.add(iter.next());
            forwardAdds++;
        }

        // Check backwards
        iter = m_history.listIterator(m_cursor);
        //Start at element specified by cursor -1, and then cursor -2 etc.
        while (iter.hasPrevious() && history.size() <= MAX_BACKWARD_ELEMENTS) {
            history.add(0, iter.previous());
            curentPosition++;
        }

        return new RecentHistory(history, curentPosition);
    }

    /**
     * Returns an unmodifiable copy of the complete history list.
     * @return an unmodifiable copy of the complete history list.
     */
    public List<UUID> getHistory() {
        return Collections.unmodifiableList(m_history);
    }

    /**
     * Remove all instances of the supplied ID.
     * @param id the ID to expunge from the Navigation Path.
     */
    public void removeAll(UUID id) {
        while (m_history.contains(id)) {
            int index = m_history.indexOf(id);
            //If what we found was where the cursor was, or before it, decrement cursor (unless cursor would go below 0)
            if (index <= m_cursor && m_cursor > 0) {
                m_cursor = m_cursor - 1;
            }
            m_history.remove(id);
        }
    }
    
    /**
     * Clears the data in this path, removing all history and elements.
     */
    public void clear() {
        m_cursor = -1;
        m_history.clear();
    }
}