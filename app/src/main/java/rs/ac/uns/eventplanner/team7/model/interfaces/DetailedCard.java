package rs.ac.uns.eventplanner.team7.model.interfaces;

/**
 * Interface used for detailed DTO classes that can be displayed as cards,
 *  like DetailedEventDTO and DetailedItemDTO
 */
public interface DetailedCard extends BasicCard {
    String getDescription();
}
