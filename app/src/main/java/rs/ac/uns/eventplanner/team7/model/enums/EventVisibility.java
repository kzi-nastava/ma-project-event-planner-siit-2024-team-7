package rs.ac.uns.eventplanner.team7.model.enums;

public enum EventVisibility {
    PUBLIC,
    PRIVATE;

    public static EventVisibility fromInteger(int val) {
        switch(val) {
            case 0:
                return PUBLIC;
            case 1:
                return PRIVATE;
        }
        return null;
    }
}
