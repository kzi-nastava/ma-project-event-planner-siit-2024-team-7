package rs.ac.uns.eventplanner.team7.data.dto.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Activity;
import rs.ac.uns.eventplanner.team7.data.model.Location;
import rs.ac.uns.eventplanner.team7.data.model.enums.EventVisibility;

@Getter
@Setter
@NoArgsConstructor
public class UpdateEventRequestDTO {
    // name cant be changed
    private String description;
    private String coverImage;
    private int maxParticipants;
    private LocalDateTime date;
    private Location location;
    private EventVisibility visibility;
    private String eventTypeName;
    private List<Activity> activities;
}