package rs.ac.uns.eventplanner.team7.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Event;
import rs.ac.uns.eventplanner.team7.model.Item;
import rs.ac.uns.eventplanner.team7.model.Location;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrganizerResponseDTO {

    private Integer id;
    private String email;
    // UserRole will always == EVENT_ORGANIZER
    // AccountStatus will always == ACTIVE
    // !!! password is not sent back !!!
    private String photoURL;
    private String phone;
    private Location location;
    private String firstName;
    private String lastName;
    private Set<Item> favoriteItems;
    private Set<Event> createdEvents;
    // blockedAccount is not needed, since its not displayed on the profile page
}
