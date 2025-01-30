package rs.ac.uns.eventplanner.team7.dto.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Event;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardWithImage;

@Getter
@Setter
@NoArgsConstructor
public class BasicEventDTO implements BasicCard, CardWithImage {

    protected Integer id;
    protected String name;
    protected String coverImage;
    protected LocalDateTime date;

    public BasicEventDTO(Event event) {
        id = event.getId();
        name = event.getName();
        coverImage = event.getCoverImage();
        date = event.getDate();
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSubtitle() {
        return date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }
}

