package rs.ac.uns.eventplanner.team7.dto.item;

import java.util.ArrayList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Item;
import rs.ac.uns.eventplanner.team7.model.Product;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.WithImage;
import rs.ac.uns.eventplanner.team7.model.interfaces.WithVersion;

@Getter
@Setter
@NoArgsConstructor
public class BasicItemDTO implements BasicCard, WithImage, WithVersion {

    protected Integer id;
    protected String type;
    protected String name;
    protected double price;
    protected String coverImage;
    protected boolean current;

    public BasicItemDTO(Item item) {
        id = item.getId();
        type = item instanceof Product ? "products" : "services";
        name = item.getName();
        price = item.getPricing().getPrice();
        coverImage = item.getImages().isEmpty() ? null : new ArrayList<>(item.getImages()).get(0);
        current = item.isCurrent();
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSubtitle() {
        return String.valueOf(price);
    }

    @Override
    public boolean isCurrent() {
        return current;
    }
}