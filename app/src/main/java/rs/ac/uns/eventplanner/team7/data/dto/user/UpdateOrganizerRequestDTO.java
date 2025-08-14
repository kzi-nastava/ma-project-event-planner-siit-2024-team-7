package rs.ac.uns.eventplanner.team7.data.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Location;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrganizerRequestDTO {

    // providerId is sent via path variable
    // cant change email
    // UserRole will always == EVENT_ORGANIZER
    // AccountStatus will always == ACTIVE
    private String oldPassword;
    private String newPassword1;
    private String newPassword2;
    private String photoURL;
    private String phone;
    private Location location;
    private String firstName;
    private String lastName;
}
