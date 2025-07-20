package rs.ac.uns.eventplanner.team7.data.interfaces;

/**
 * Interface used for DTO classes that can be displayed as cards, like BasicEventDTO and BasicItemDTO
 */
public interface BasicCard {
    Integer getId();
    String getTitle();
    String getSubtitle();
}
