package rs.ac.uns.eventplanner.team7.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Pricing;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingResponseDTO {
    private String itemName;
    private Integer pricingId;
    private double price;
    private double discount;
    private String activeFrom;
    private boolean deleted;

    public PricingResponseDTO(String name, Pricing pricing) {
        this.itemName = name;
        this.pricingId = pricing.getId();
        this.price = pricing.getPrice();
        this.discount = pricing.getDiscount();
        this.activeFrom = pricing.getActiveFrom().toString();
        this.deleted = pricing.isDeleted();
    }
}
