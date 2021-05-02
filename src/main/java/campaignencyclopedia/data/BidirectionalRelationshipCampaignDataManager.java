package campaignencyclopedia.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


/**
 * A data management object that is used at run time to provide a mutable object containing the state of the entire
 * Campaign.  As changes are made, this manager is updated and when asked to save the data this data is dumped to file.
 * This implementation of a DataAccessor/CampaignDataManager tracks relationships bidirectionally, meaning that
 * requests for relationships will return those pointing "in" AND "out" from an entity.
 *
 * @author Keith
 */
public class BidirectionalRelationshipCampaignDataManager implements CampaignDataManager {


    /** A Logger. */
    private static final Logger LOGGER = Logger.getLogger(BidirectionalRelationshipCampaignDataManager.class.getName());

    /** The name of the campaign.  */
    private String m_campaignName;

    /** A map of UUIDs to their associated Entities. */
    private final Map<UUID, Entity> m_entities;

    /** A manager for ALL relationships in the campaign. */
    private final BidirectionalRelationshipManager m_relationships;

    /** A map of UUIDs to their associated Timeline Entries. */
    private final Map<UUID, TimelineEntry> m_timelineData;

    /** The path to the file where the current campaign is stored, or null if no path exists. */
    private String m_filename;

    /** The currently configured campaign calendar. */
    private CampaignCalendar m_cal;

    /** A Set of listeners on the CDM. */
    private final Set<CampaignDataManagerListener> m_listeners;

    public BidirectionalRelationshipCampaignDataManager() {
        m_campaignName = "New Campaign";
        m_filename = null;

        m_entities = new HashMap<>();
        m_relationships = new BidirectionalRelationshipManager();
        m_timelineData = new HashMap<>();
        m_cal = new CampaignCalendar();
        m_listeners = new HashSet<>();
    }

    /**
     * Adds a listener to this data manager.
     * @param listener the listener to be added.
     */
    @Override
    public void addListener(CampaignDataManagerListener listener) {
        if (listener != null) {
            m_listeners.add(listener);
        }
    }

    /**
     * Removes a listener from this data manager.
     * @param listener the listener to be removed.
     */
    @Override
    public void removeListener(CampaignDataManagerListener listener) {
        m_listeners.remove(listener);
    }

    /** {@inheritDoc} */
    @Override
    public Entity getEntity(UUID id) {
        if (id != null) {
            return m_entities.get(id);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<Entity> getAllEntities() {
        List<Entity> list = new ArrayList<>(m_entities.values());
        Collections.sort(list);
        return list;
    }

    /** {@inheritDoc} */
    @Override
    public void addOrUpdateEntity(Entity entity) {
        if (entity != null) {
            m_entities.put(entity.getId(), entity);
        }

        // Alert Listeners
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.dataAddedOrUpdated(entity);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void addOrUpdateTimelineEntry(TimelineEntry entry) {
        if (entry != null) {
            m_timelineData.put(entry.getId(), entry);
        }
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.timelineEntryAddedOrUpdated(entry);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeTimelineEntry(UUID id) {
        if (id != null) {
            m_timelineData.remove(id);
        }
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.timelineEntryRemoved(id);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeEntity(UUID id) {
        if (id != null) {
            // Remove the Entity
            m_entities.remove(id);

            // Remove relationships for the removed Entity (in or out!)
            m_relationships.clearSingleEntityRelationships(id);
        }

        // Alert Listeners
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.dataRemoved(id);
        }
    }

    /**
     * Updates the calendar in this CDM using the supplied one.
     * @param cal the new calendar.
     */
    @Override
    public void updateCalendar(CampaignCalendar cal) {
        m_cal.updateMonths(cal.getMonths());
        for (UUID id : m_timelineData.keySet()) {
            TimelineEntry entry = m_timelineData.get(id);
            if (!m_cal.hasMonth(entry.getMonth())) {
                TimelineEntry updated = new TimelineEntry(entry.getTitle(), m_cal.getMonthForIndex(0), entry.getYear(), entry.isSecret(), entry.getAssociatedId(), entry.getId());
                m_timelineData.put(id, updated);
            }
        }
    }

    /**
     * Creates and returns a Campaign that is represented by all of the data in the CampaignDataManager.  The CDM is
     * not modified in any way.  Each time this method is called, a new Campaign object is instantiated and returned.
     *
     * @return a Campaign that contains all of the data in the CampaignDataManager.
     */
    @Override
    public Campaign getData() {
        return new Campaign(m_campaignName, new HashSet<>(m_entities.values()), m_relationships.getAllRelationships(), new HashSet<>(m_timelineData.values()), m_cal);
    }

    /**
     * Clears all old data and sets the supplied campaign data on this display.
     * @param campaign the new data to set.
     */
    @Override
    public void setData(Campaign campaign) {
        m_entities.clear();
        m_timelineData.clear();
        m_relationships.clear();
        
        // Alert listeners of cleared data.
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.clearAllData();
        }
        
        m_campaignName = campaign.getName();
        m_cal = campaign.getCalendar();


        // Set to collect all of the previously saved relationships.  This is used later to ensure that all established
        // Relationships are in the RelationshipOptionManager.
        Set<String> relationships = new HashSet<>();

        // Add all of the Entities.
        for (Entity e : campaign.getEntities()) {
            UUID entityId = e.getId();
            m_entities.put(entityId, e);

            // Collect all of the previously saved relationships and add them to our Set above.
            RelationshipManager entityRelMgr = campaign.getRelationships(entityId);
            if (entityRelMgr != null) {
                for (Relationship r : entityRelMgr.getAllRelationships()) {
                    //Store text for options
                    relationships.add(r.getRelationshipText());
                    //Add the relationship itself
                    m_relationships.addRelationship(r);
                }
            }
        }

        // Ensure that all of the relationships previously saved are in the local
        // relationships file, and indeed the Relationship Data Manager as well.
        RelationshipOptionManager.addRelationships(new ArrayList<>(relationships));

        // Roll through each of the timeline entries for this campaign and ensure that the months all exist in the
        // campaign.  If any are missing, add them to the Calendar and alert the user with a popup message.
        boolean monthsAdded = false;
        for (TimelineEntry tle : campaign.getTimelineEntries()) {
            m_timelineData.put(tle.getId(), tle);
            if (!m_cal.hasMonth(tle.getMonth())) {
                m_cal.addMonth(tle.getMonth());
                monthsAdded = true;
            }
        }
        if (monthsAdded) {
            JOptionPane.showMessageDialog(null,
                                          "One or more 'months' were added to your campaign\n"
                                        + "calendar based on stored campaign timeline data.\n"
                                        + "You may review this change to your calendar in its\n"
                                        + "configuration dialog.",
                                          "Missing Months",
                                          JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * Returns the save file name.
     * @return the save file name.
     */
    @Override
    public String getSaveFileName() {
        return m_filename;
    }

    /**
     * Sets the save file name.
     * @param filename the file name of the campaign.
     */
    @Override
    public void setFileName(String filename) {
        if (filename != null && !filename.endsWith(".campaign")) {
            filename += ".campaign";
        }
        m_filename = filename;
    }

    /**
     * Returns the timeline data.
     * @return the timeline data.
     */
    @Override
    public Set<TimelineEntry> getTimelineData() {
        return new HashSet<>(m_timelineData.values());
    }

    /** {@inheritDoc} */
    @Override
    public CampaignCalendar getCalendar() {
        return m_cal;
    }

    /** {@inheritDoc} */
    @Override
    public void addRelationship(Relationship rel) {
        //The BiDirectional Relationship Mnager handles all the dirty work in this capmaign data manager
        m_relationships.addRelationship(rel);
        
        // Alert Listeners, data UPDATED because relationship added
        UUID entity = rel.getEntityId();
        UUID otherEntity = rel.getRelatedEntity();
        Entity actualEntity = getEntity(entity);
        Entity actualOtherEntity = getEntity(otherEntity);
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.dataAddedOrUpdated(actualEntity);
            cdml.dataAddedOrUpdated(actualOtherEntity);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeRelationship(Relationship toRemove) {
        m_relationships.remove(toRemove);
        
        // Alert Listeners, data UPDATED because relationship removed
        UUID entity = toRemove.getEntityId();
        UUID otherEntity = toRemove.getRelatedEntity();
        Entity actualEntity = getEntity(entity);
        Entity actualOtherEntity = getEntity(otherEntity);
        for (CampaignDataManagerListener cdml : m_listeners) {
            cdml.dataAddedOrUpdated(actualEntity);
            cdml.dataAddedOrUpdated(actualOtherEntity);
        }
    }

    /** {@inheritDoc} */
    @Override
    public RelationshipManager getRelationshipsForEntity(UUID entity) {
        return  m_relationships.getRelationshipManagerForEntity(entity);
    }
    

    /** {@inheritDoc} */
    @Override
    public void addOrUpdateAllRelationships(UUID entity, RelationshipManager relMgr) {
        if (entity != null && relMgr != null) {
            m_relationships.addAllRelationships(relMgr.getAllRelationships());
            
            // Alert Listeners, data updated because relationship added
            for (UUID i : relMgr.getAllAffectedIds()) {
                Entity actualEntity = getEntity(i);
                for (CampaignDataManagerListener cdml : m_listeners) {
                    cdml.dataAddedOrUpdated(actualEntity);
                }
            }
            
        } else {
            LOGGER.warning("Attempted to store a null Entity or RelationshipManager.  Entity (unused) was:  " +
                    entity + ", RelationshipManager was:  " + relMgr);
        }
    }
    
    
}
