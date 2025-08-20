package rs.ac.uns.eventplanner.team7.data.dto.event;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Activity;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.model.Location;
import rs.ac.uns.eventplanner.team7.data.model.enums.EventVisibility;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private String coverImage;
    private int maxParticipants;
    private LocalDateTime date;
    private Location place;
    private EventVisibility visibility;
    private EventType eventType;
    private List<Activity> activities;
}
