package rs.ac.uns.eventplanner.team7.dto.event;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Event;

@Getter
@Setter
@NoArgsConstructor
public class BasicEventDTO {

    protected Integer id;
    protected String name;
    protected String coverImage;
    protected String date;

    public BasicEventDTO(Event event) {
        id = event.getId();
        name = event.getName();
        coverImage = event.getCoverImage();
        date = event.getDate().toString();
    }
}

