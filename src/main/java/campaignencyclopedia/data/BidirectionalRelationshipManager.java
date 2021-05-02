/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package campaignencyclopedia.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author Keith
 */
public class BidirectionalRelationshipManager {
    
    private final Map<UUID, RelationshipManager> m_edgeMap;
    

    /** Creates a new RelationshipManager. */
    public BidirectionalRelationshipManager() {
        m_edgeMap = new HashMap<>();
    }
    
    
    /**
     * Adds the relationship to the manager.  This stores it in the list of "in or outbound relationships"
     * for both ends in question.
     * @param rel the Relationship to add.
     */
    public void addRelationship(Relationship rel) {
        if (rel != null) {
            UUID sideA = rel.getEntityId();
            UUID sideB = rel.getRelatedEntity();
            
            guaranteeRelationshipManager(rel);
            
            for (Map.Entry<UUID, RelationshipManager> e : m_edgeMap.entrySet()) {
                UUID v = e.getKey();
                RelationshipManager rm = e.getValue();
                if (v.equals(sideA) || v.equals(sideB)) {
                    rm.addRelationship(rel);
                }
            }
        }
    }

    /**
     * Adds all of the Relationships supplied to the manager.
     * @param rels the Relationships to add.
     */
    public void addAllRelationships(Collection<Relationship> rels) {
        for (Relationship r : rels) {
            addRelationship(r);
        }
    }

    /**
     * Removes the supplied entry from the manager.
     * @param rel the Relationship to remove.
     */
    public void remove(Relationship rel) {
        if (rel != null) {
            UUID sideA = rel.getEntityId();
            UUID sideB = rel.getRelatedEntity();
            
            guaranteeRelationshipManager(rel);  //Still check even though it is a remove, just for safety
            
            for (Map.Entry<UUID, RelationshipManager> e : m_edgeMap.entrySet()) {
                UUID v = e.getKey();
                RelationshipManager rm = e.getValue();
                if (v.equals(sideA) || v.equals(sideB)) {
                    rm.remove(rel);
                }
            }
        }
    }

    /**
     * Removes all of the relationships in the supplied collection.
     * @param rels the relationships to remove.
     */
    public void removeAll(Collection<Relationship> rels) {
        for (Relationship r : rels) {
            remove(r);
        }
    }

    /**
     * Returns the public relationships in the manager.
     * @param id The entity to find relationships for.
     * @return the public relationships in the manager.
     */
    public Set<Relationship> getPublicRelationshipsForEntity(UUID id) {
        guaranteeRelationshipManager(id);
        RelationshipManager rm = m_edgeMap.get(id);
        return rm.getPublicRelationships();
    }

    /**
     * Returns the private relationships in the manager.
     * @param id The entity to find relationships for.
     * @return the private relationships in the manager.
     */
    public Set<Relationship> getSecretRelationshipsForEntity(UUID id) {
        guaranteeRelationshipManager(id);
        RelationshipManager rm = m_edgeMap.get(id);
        return rm.getSecretRelationships();
    }

    /**
     * Returns the RelationshipManager for the given entity, for all inbound or outbound.
     * @param id The entity to find relationships for.
     * @return all the RelationshipManager.
     */
    public RelationshipManager getRelationshipManagerForEntity(UUID id) {
        guaranteeRelationshipManager(id);
        return m_edgeMap.get(id);
    }

    /**
     * Returns all the Relationships in the manager for the given entity, inbound or outbound.
     * @param id The entity to find relationships for.
     * @return all the Relationships in this manager.
     */
    public Set<Relationship> getAllRelationshipsForEntity(UUID id) {
        guaranteeRelationshipManager(id);
        return m_edgeMap.get(id).getAllRelationships();
    }
    
    /**
     * Returns all the Relationships in the manager.
     * @return all the Relationships in this manager.
     */
    public Map<UUID, RelationshipManager> getAllRelationships() {
        return Collections.unmodifiableMap(m_edgeMap);
    }
    
    /**
     * Clears all the Relationships in the manager for this single entity.
     * @param id The entity to find clear.
     */
    public void clearSingleEntityRelationships(UUID id) {
        if (m_edgeMap.containsKey(id)) {
            m_edgeMap.get(id).clear();
            m_edgeMap.remove(id);
        }
    }

    /** Clears all of the data from this manager, including all relationships for ALL entities. */
    public void clear() {
        //Just in case there were any other references to the RelationshipManagers themselves, ensure that those are cleared out.
        for (RelationshipManager r : m_edgeMap.values()) {
            r.clear();
        }
        m_edgeMap.clear();
    }
    
    
    
    
    /**
     * Utility method to ensure RelationshipManagers aren't null.
     * @param rel The relationship to prepare for
     */
    private void guaranteeRelationshipManager(Relationship r) {
        if (m_edgeMap.get(r.getEntityId()) == null) {
            m_edgeMap.put(r.getEntityId(), new RelationshipManager());
        }
        if (m_edgeMap.get(r.getRelatedEntity()) == null) {
            m_edgeMap.put(r.getRelatedEntity(), new RelationshipManager());
        }
    }
    
    /**
     * Utility method to ensure RelationshipManagers aren't null.
     * @param id The entity to prepare for
     */
    private void guaranteeRelationshipManager(UUID id) {
        if (!m_edgeMap.containsKey(id)) {
            m_edgeMap.put(id, new RelationshipManager());
        }
    }
}
