package rs.ac.uns.eventplanner.team7.data.dto.item;

import java.util.ArrayList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Item;
import rs.ac.uns.eventplanner.team7.data.model.Product;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.WithImage;

@Getter
@Setter
@NoArgsConstructor
public class BasicItemDTO implements BasicCard, WithImage {

    protected Integer id;
    protected String type;
    protected String name;
    protected double price;
    protected String coverImage;

    public BasicItemDTO(Item item) {
        id = item.getId();
        type = item instanceof Product ? "products" : "services";
        name = item.getName();
        price = item.getPricing().getPrice();
        coverImage = item.getImages().isEmpty() ? null : new ArrayList<>(item.getImages()).get(0);
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSubtitle() {
        return String.valueOf(price);
    }
}