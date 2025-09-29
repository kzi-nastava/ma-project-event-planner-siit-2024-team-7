package rs.ac.uns.eventplanner.team7.data.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Location;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequestDTO {
    private String email;
    private String password;
    private String password2;
    private UserRole role;
    // AccountStatus not necessary, it will be set internally
    private String photoURL;
    private String phone;
    private Location location = new Location();
    private String firstName;
    private String lastName;
    private String orgName;
    private String orgDesc;
    private String authToken;

    public boolean areValidFields() {
        return email != null && password != null && password2 != null && phone != null &&
                location.getCountry() != null && location.getCity() != null && location.getStreet()
                != null && location.getHouseNumber() != null;
    }
}