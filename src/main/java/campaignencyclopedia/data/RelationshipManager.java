package campaignencyclopedia.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A manager for relationship data.
 * @author adam
 */
public class RelationshipManager {

    protected final Set<Relationship> m_secret;
    protected final Set<Relationship> m_public;

    /** Creates a new RelationshipManager. */
    public RelationshipManager() {
        m_secret = new HashSet<>();
        m_public = new HashSet<>();
    }

    
    /**
     * Copy Constructor, create new relationship sets with the ones provided by the argument passed in.
     * @param rm Manager to copy.
     */
    public RelationshipManager(RelationshipManager rm) {
        this.m_secret = new HashSet<>(rm.m_secret);
        this.m_public = new HashSet<>(rm.m_public);
    }

    
    
    /**
     * Adds the relationship to the manager.
     * @param rel the Relationship to add.
     */
    public void addRelationship(Relationship rel) {
        if (rel != null) {
            if (rel.isSecret()) {
                m_secret.add(rel);
            } else {
                m_public.add(rel);
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
        m_secret.remove(rel);
        m_public.remove(rel);
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
     * @return the public relationships in the manager.
     */
    public Set<Relationship> getPublicRelationships() {
        return Collections.unmodifiableSet(m_public);
    }

    /**
     * Returns the private relationships in the manager.
     * @return the private relationships in the manager.
     */
    public Set<Relationship> getSecretRelationships() {
        return Collections.unmodifiableSet(m_secret);
    }

    /**
     * Returns all the Relationships in the manager.
     * @return all the Relationships in this manager.
     */
    public Set<Relationship> getAllRelationships() {
        Set<Relationship> all = new HashSet<>(m_public);
        all.addAll(m_secret);
        return Collections.unmodifiableSet(all);
    }

    /**
     * Returns all the affected entity UUIDs.
     * @return all the UUIDs from any Relationships in this manager.
     */
    public Set<UUID> getAllAffectedIds() {
        Set<UUID> allIds = new HashSet<>();
        
        for (Relationship r : m_public) {
            allIds.add(r.getEntityId());
            allIds.add(r.getRelatedEntity());
        }
        for (Relationship r : m_secret) {
            allIds.add(r.getEntityId());
            allIds.add(r.getRelatedEntity());
        }
        return Collections.unmodifiableSet(allIds);
    }

    /** Clears all of the data from this manager. */
    public void clear() {
        m_public.clear();
        m_secret.clear();
    }
    
    /** Clears all of the public data from this manager. */
    public void clearPublicRelationships() {
        m_public.clear();
    }
    
    /** Clears all of the secret from this manager. */
    public void clearSecretRelationships() {
        m_secret.clear();
    }
}