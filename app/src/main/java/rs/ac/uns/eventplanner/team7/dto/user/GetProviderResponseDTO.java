package rs.ac.uns.eventplanner.team7.dto.user;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.model.Item;
import rs.ac.uns.eventplanner.team7.model.Location;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetProviderResponseDTO {
    private Integer id;
    private String email;
    private String photoURL;
    private String phone;
    private Location location;
    private String orgName;
    private String orgDesc;
    private Set<BasicItemDTO> items;
    private Set<BasicItemDTO> favoriteItems;
    private Set<BasicEventDTO> favoriteEvents;
}
