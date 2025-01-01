package rs.ac.uns.eventplanner.team7.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Location;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;

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
    private Location address;
    private String firstName;
    private String lastName;
    private String orgName;
    private String orgDesc;
    private String authToken;

    /// Constructor for auth
    public RegisterRequestDTO(String email, String password, String password2, String photoURL,
                              String phone, Location address, String authToken) {
        this.email = email;
        this.password = password;
        this.password2 = password2;
        this.role = UserRole.AUTH;
        this.photoURL = photoURL;
        this.phone = phone;
        this.address = address;
        this.authToken = authToken;
    }

    /// Constructor for spp and event_org
    public RegisterRequestDTO(String email, String password, String password2, UserRole role,
                              String photoURL, String phone, Location address, String name, String lastNameOrDesc) {
        this.email = email;
        this.password = password;
        this.password2 = password2;
        this.role = role;
        this.photoURL = photoURL;
        this.phone = phone;
        this.address = address;
        if (role == UserRole.EVENT_ORG) {
            orgName = name;
            orgDesc = lastNameOrDesc;
        } else if (role == UserRole.SPP) {
            firstName = name;
            lastName = lastNameOrDesc;
        }
    }
}