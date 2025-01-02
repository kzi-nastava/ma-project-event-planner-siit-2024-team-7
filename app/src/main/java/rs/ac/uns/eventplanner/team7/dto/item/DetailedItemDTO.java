package rs.ac.uns.eventplanner.team7.dto.item;

import java.util.Locale;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.DetailedCard;
import rs.ac.uns.eventplanner.team7.model.Item;
import rs.ac.uns.eventplanner.team7.model.Location;

@Getter
@Setter
@NoArgsConstructor
public class DetailedItemDTO extends BasicItemDTO implements DetailedCard {

    private String description;
    private Location location;
    private String categoryName;

    public DetailedItemDTO(Item item) {
        super(item);
        description = item.getDescription();
        location = item.getLocation();
        categoryName = item.getCategory().getName();
    }

    @Override
    public String getSubtitle() {
        return String.format(Locale.getDefault(), "%s\n%.2f", categoryName, price);
    }
}
