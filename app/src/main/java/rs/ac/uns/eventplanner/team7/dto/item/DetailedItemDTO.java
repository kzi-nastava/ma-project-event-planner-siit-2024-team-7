package rs.ac.uns.eventplanner.team7.dto.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Item;
import rs.ac.uns.eventplanner.team7.model.Location;

@Getter
@Setter
@NoArgsConstructor
public class DetailedItemDTO extends BasicItemDTO {

    private String description;
    private Location location;
    private String categoryName;

    public DetailedItemDTO(Item item) {
        super(item);
        description = item.getDescription();
        location = item.getLocation();
        categoryName = item.getCategory().getName();
    }
}
