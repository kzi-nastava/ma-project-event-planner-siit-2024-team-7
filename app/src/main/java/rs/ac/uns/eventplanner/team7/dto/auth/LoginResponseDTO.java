package rs.ac.uns.eventplanner.team7.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;

@AllArgsConstructor
@Getter
public class LoginResponseDTO {
    private String token;
    private Integer id; // helps when trying to retrieve id for My Account
    private UserRole role; // helps dynamically change UI
    private String city;


}
