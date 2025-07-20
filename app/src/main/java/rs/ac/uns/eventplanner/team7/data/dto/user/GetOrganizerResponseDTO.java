package rs.ac.uns.eventplanner.team7.data.dto.user;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.data.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.data.model.Location;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetOrganizerResponseDTO {
    private Integer id; // it's needed for future use, for example to send it with the put request in case of an update
    private String email;
    private String photoURL;
    private String phone;
    private Location location;
    private String firstName;
    private String lastName;
    private Set<BasicItemDTO> favoriteItems;
    private Set<BasicEventDTO> createdEvents;
    private Set<BasicEventDTO> favoriteEvents;
}
