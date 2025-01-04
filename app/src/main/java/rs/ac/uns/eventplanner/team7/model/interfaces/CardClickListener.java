package rs.ac.uns.eventplanner.team7.model.interfaces;

/**
 * Interface used for reacting to clicking on more info button of a normal card
 * or clicking on the entire horizontal card
 */
public interface CardClickListener {

    /**
     * @param entityId id of entity
     * @param type type of entity, it can be either 'events', 'products' or 'services',
     *             it is needed to distinguish item type
     */
    void onCardClicked(Integer entityId, String type);
}
