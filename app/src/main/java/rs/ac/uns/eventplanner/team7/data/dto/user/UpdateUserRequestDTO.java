package rs.ac.uns.eventplanner.team7.data.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Location;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateUserRequestDTO {
    private UserRole role;
    private String oldPassword;
    private String newPassword1;
    private String newPassword2;
    private String photoURL;
    private String phone;
    private Location location;
}