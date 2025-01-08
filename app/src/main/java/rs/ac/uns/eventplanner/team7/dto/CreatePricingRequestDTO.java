package rs.ac.uns.eventplanner.team7.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Pricing;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePricingRequestDTO {
    private double price;
    private double discount;
    private String activeFrom;

    public CreatePricingRequestDTO(Pricing pricing) {
        this.price = pricing.getPrice();
        this.discount = pricing.getDiscount();
        this.activeFrom = pricing.getActiveFrom().toString();
    }

    public Pricing toPricing() {
        Pricing pricing = new Pricing();
        pricing.setPrice(price);
        pricing.setDiscount(discount);
        pricing.setActiveFrom(LocalDate.parse(activeFrom));
        return pricing;
    }
}
