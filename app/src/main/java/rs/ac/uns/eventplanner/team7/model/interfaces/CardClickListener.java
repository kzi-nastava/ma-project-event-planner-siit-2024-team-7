package rs.ac.uns.eventplanner.team7.model.interfaces;

/**
 * Interface used for reacting to clicking on more info button of a normal card
 * or clicking on the entire horizontal card
 */
public interface CardClickListener {
    void onCardClicked(BasicCard entity);
}
