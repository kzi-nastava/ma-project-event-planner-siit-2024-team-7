package rs.ac.uns.eventplanner.team7.data.dto.user;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Location;

@Getter
@Setter
public class UpdateProviderRequestDTO {
    private Integer userId;
    // id is sent via path variable
    // cant change email
    // UserRole will always == EVENT_ORGANIZER
    // AccountStatus will always == ACTIVE
    private String oldPassword;
    private String newPassword1;
    private String newPassword2;
    private String photoURL;
    private String phone;
    private Location location;
    // cant change orgName
    private String orgDesc;
}
