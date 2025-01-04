package rs.ac.uns.eventplanner.team7.model.interfaces;

/**
 * Interface used for reacting to events of applying and resetting filters
 */
public interface FilterActionsListener {
    void onFiltersApplied();
    void onFiltersReset();
}
