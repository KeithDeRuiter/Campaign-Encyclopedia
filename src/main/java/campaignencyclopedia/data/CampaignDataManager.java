/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package campaignencyclopedia.data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Interface describing campaign data management functions.
 * @author Keith
 */
public interface CampaignDataManager extends DataAccessor {

    /**
     * Adds a listener to this data manager.
     * @param listener the listener to be added.
     */
    void addListener(CampaignDataManagerListener listener);

    /** {@inheritDoc} */
    void addOrUpdateAllRelationships(UUID entity, RelationshipManager relMgr);

    /** {@inheritDoc} */
    void addOrUpdateEntity(Entity entity);

    /** {@inheritDoc} */
    void addOrUpdateTimelineEntry(TimelineEntry entry);

    /** {@inheritDoc} */
    void addRelationship(Relationship rel);

    /** {@inheritDoc} */
    List<Entity> getAllEntities();

    /** {@inheritDoc} */
    CampaignCalendar getCalendar();

    /**
     * Creates and returns a Campaign that is represented by all of the data in the CampaignDataManager.  The CDM is
     * not modified in any way.  Each time this method is called, a new Campaign object is instantiated and returned.
     *
     * @return a Campaign that contains all of the data in the CampaignDataManager.
     */
    Campaign getData();

    /** {@inheritDoc} */
    Entity getEntity(UUID id);

    /** {@inheritDoc} */
    RelationshipManager getRelationshipsForEntity(UUID entity);

    /**
     * Returns the save file name.
     * @return the save file name.
     */
    String getSaveFileName();

    /**
     * Returns the timeline data.
     * @return the timeline data.
     */
    Set<TimelineEntry> getTimelineData();

    /** {@inheritDoc} */
    void removeEntity(UUID id);

    /**
     * Removes a listener from this data manager.
     * @param listener the listener to be removed.
     */
    void removeListener(CampaignDataManagerListener listener);

    /** {@inheritDoc} */
    void removeRelationship(Relationship toRemove);

    /** {@inheritDoc} */
    void removeTimelineEntry(UUID id);

    /**
     * Clears all old data and sets the supplied campaign data on this display.
     * @param campaign the new data to set.
     */
    void setData(Campaign campaign);

    /**
     * Sets the save file name.
     * @param filename the file name of the campaign.
     */
    void setFileName(String filename);

    /**
     * Updates the calendar in this CDM using the supplied one.
     * @param cal the new calendar.
     */
    void updateCalendar(CampaignCalendar cal);
    
}
