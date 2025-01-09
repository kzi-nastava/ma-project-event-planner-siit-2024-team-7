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
public class UpdatePricingRequestDTO {
    private double price;
    private double discount;
    private String activeFrom;

    public UpdatePricingRequestDTO(Pricing pricing) {
        this.price = pricing.getPrice();
        this.discount = pricing.getDiscount();
        this.activeFrom = pricing.getActiveFrom().toString();
    }
}
