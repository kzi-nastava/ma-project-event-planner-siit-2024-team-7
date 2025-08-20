package rs.ac.uns.eventplanner.team7.data.dto.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Activity;
import rs.ac.uns.eventplanner.team7.data.model.Location;
import rs.ac.uns.eventplanner.team7.data.model.enums.EventVisibility;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventRequestDTO {
    private String name;
    private String description;
    private String coverImage;
    private int maxParticipants;
    private LocalDateTime date;
    private Location location = new Location();
    private EventVisibility visibility;
    private String eventTypeName;
    private List<Activity> activities;
    // invited ppl emails will be needed here

    public boolean areValidFields() {
        return name != null && description != null && date != null &&
                location.getCountry() != null && location.getCity() != null && location.getStreet()
                != null && location.getHouseNumber() != null && visibility != null &&
                eventTypeName != null && activities != null;
    }
}