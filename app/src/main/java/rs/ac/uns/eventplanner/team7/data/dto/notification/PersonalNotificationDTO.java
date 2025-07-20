package rs.ac.uns.eventplanner.team7.data.dto.notification;

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
        return timestamp;
    }
}
