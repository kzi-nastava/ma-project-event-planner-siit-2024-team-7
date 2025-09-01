package rs.ac.uns.eventplanner.team7.data.dto.user;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.data.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.data.model.Event;
import rs.ac.uns.eventplanner.team7.data.model.Location;

@Getter
@Setter
public class GetUserResponseDTO {
    private Integer id;
    private String email;
    private String photoURL;
    private String phone;
    private Location location;
    private String firstName;
    private String lastName;
    private Set<BasicItemDTO> favoriteItems;
    private Set<BasicEventDTO> createdEvents;
    private Set<BasicEventDTO> favoriteEvents;
    private Set<BasicEventDTO> acceptedEvents;
    private Set<BasicItemDTO> items;
    private String orgName;
    private String orgDesc;
}
