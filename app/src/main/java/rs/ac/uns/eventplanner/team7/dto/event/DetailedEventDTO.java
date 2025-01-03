package rs.ac.uns.eventplanner.team7.dto.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.DetailedCard;
import rs.ac.uns.eventplanner.team7.model.Event;
import rs.ac.uns.eventplanner.team7.model.Location;

@Getter
@Setter
@NoArgsConstructor
public class DetailedEventDTO extends BasicEventDTO implements DetailedCard {

    private String description;
    private int maxParticipants;
    private int currentParticipants;
    private Location place;
    private String typeName;

    public DetailedEventDTO(Event event) {
        super(event);
        description = event.getDescription();
        maxParticipants = event.getMaxParticipants();
        currentParticipants = event.getCurrentParticipants();
        place = event.getPlace();
        typeName = event.getType().getName();
    }

    @Override
    public String getSubtitle() {
        return String.format("%s\n%s", super.getSubtitle(), place.toAddressString());
    }
}
