package rs.ac.uns.eventplanner.team7.data.dto.event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.WithImage;
import rs.ac.uns.eventplanner.team7.data.model.Event;

@Getter
@Setter
@NoArgsConstructor
public class BasicEventDTO implements BasicCard, WithImage {

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
        int currentYear = LocalDateTime.now().getYear();
        String pattern = String.format("EEEE, MMMM dd %s 'at' HH:mm", date.getYear() == currentYear ? "" : "yyyy");
        return date.format(DateTimeFormatter.ofPattern(pattern).withLocale(Locale.US));
    }
}

