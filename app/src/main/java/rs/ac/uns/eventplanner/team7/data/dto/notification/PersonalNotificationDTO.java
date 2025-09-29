package rs.ac.uns.eventplanner.team7.data.dto.notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalNotificationDTO implements BasicCard {
    private Integer id;
    private String title;
    private String message;
    private String timestamp;
    private boolean read;

    @Override
    public String getSubtitle() {
        int currentYear = LocalDateTime.now().getYear();
        LocalDateTime parsedTimestamp = LocalDateTime.parse(timestamp);
        String pattern = String.format("EEEE, MMMM dd %s 'at' HH:mm", parsedTimestamp.getYear() == currentYear ? "" : "yyyy");
        return parsedTimestamp.format(DateTimeFormatter.ofPattern(pattern).withLocale(Locale.US));
    }
}
