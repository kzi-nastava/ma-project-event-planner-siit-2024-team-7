package rs.ac.uns.eventplanner.team7.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
