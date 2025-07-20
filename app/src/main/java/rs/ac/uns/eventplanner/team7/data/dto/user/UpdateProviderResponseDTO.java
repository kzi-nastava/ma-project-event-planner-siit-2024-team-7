package rs.ac.uns.eventplanner.team7.data.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Item;
import rs.ac.uns.eventplanner.team7.data.model.Location;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProviderResponseDTO {
    private Integer id;
    private String email;
    // UserRole will always == EVENT_ORGANIZER
    // AccountStatus will always == ACTIVE
    // !!! password is not sent back !!!
    private String photoURL;
    private String phone;
    private Location location;
    private String orgName;
    private String orgDesc;
    private Set<Item> items;
    // blockedAccount is not needed, since  it's not displayed on the profile page
}
