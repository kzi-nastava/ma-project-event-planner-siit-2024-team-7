package rs.ac.uns.eventplanner.team7.data.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Location;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetUserDetailsResponseDTO {
    private Integer id;
    private String email;
    private String photoURL;
    private String phone;
    private UserRole role;
    private Location location;
    private String firstName; // organizer
    private String lastName; // organizer
    private String orgName; // provider
    private String orgDesc; // provider
}
