package campaignencyclopedia.display.swing;

import campaignencyclopedia.data.Entity;
import campaignencyclopedia.data.EntityData;
import campaignencyclopedia.data.Relationship;
import java.awt.Component;
import java.util.Set;
import toolbox.display.EditListener;

/**
 * Interface for defining the display of entity details, such as relationships, tags, and descriptions.
 * @author Keith
 */
public interface EntityDetailsDisplay {

    /**
     * Clears all data from this display, emptying the public data, secret data, and relationships.
     */
    public void clear();

    /**
     * Gets the component visualization of this display.
     * @return A displayable component.
     */
    public Component getDisplayableComponent();

    /**
     * Change the entity data displayed to reflect the data and relationships for the entity provided.
     * @param entity The entity whose EntityData should be displayed.
     * @param relationships The relationship details of the entity.
     */
    public void displayEntityDetails(Entity entity, Set<Relationship> relationships);

    /**
     * Sets the public data currently displayed.
     * @param publicData The EntityData to set.
     */
    public void setPublicData(EntityData publicData);

    /**
     * Returns the public data currently displayed.
     * @return the public data currently displayed.
     */
    public EntityData getPublicData();
    
    /**
     * Sets the secret data currently displayed.
     * @param secretData The EntityData to set.
     */
    public void setSecretData(EntityData secretData);
    
    /**
     * Returns the secret data currently displayed.
     * @return the secret data currently displayed.
     */
    public EntityData getSecretData();
    
    /**
     * Returns the currently displayed relationships in the relationship editor.
     * @return the currently displayed relationships in the relationship editor.
     */
    public Set<Relationship> getRelationships();
    
    /**
     * Sets the relationships to display in the relationship editor.
     * @param relationships The relationships to display.
     */
    public void setRelationships(Set<Relationship> relationships);
    
    /**
     * Adds a listener to be notified when this display is edited.  Ignored if listener is null
     * @param listener The listener to add.
     */
    public void addEditListener(EditListener listener);
    
    /**
     * Removes a listener for display edits, ignored if the listener had not been added.
     * @param listener The listener to remove.
     */
    public void removeEditListener(EditListener listener);
}
