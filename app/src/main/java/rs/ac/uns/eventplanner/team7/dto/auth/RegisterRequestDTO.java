package rs.ac.uns.eventplanner.team7.dto.auth;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import rs.ac.uns.eventplanner.team7.model.Location;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    private String email;
    private String password;
    private String password2;
    private UserRole role;
    // AccountStatus not necessary, it will be set internally
    private String photoURL;
    private String phone;
    private Location address;
    private String firstName;
    private String lastName;
    private String orgName;
    private String orgDesc;

}