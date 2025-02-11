package rs.ac.uns.eventplanner.team7.model.interfaces;

/**
 * Interface used for reacting to events of applying and resetting filters, sorting and changing a page
 */
public interface SearchActionsListener extends FilterActionsListener {

    void onSortApplied();
    void onNextPage();
}
