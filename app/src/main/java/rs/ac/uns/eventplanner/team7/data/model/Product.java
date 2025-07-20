package rs.ac.uns.eventplanner.team7.data.model;

import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.enums.ItemStatus;

@Getter
@Setter
@NoArgsConstructor
public class Product extends Item {

    public Product(Integer id, String name, String description, Set<String> images, boolean isVisible, ItemStatus status,
                   Location location, Pricing pricing, Category category, Set<EventType> appliesTo, boolean isAvailable) {
        super(id, name, description, images, isVisible, status, location, pricing, category, appliesTo, isAvailable);
    }
}
