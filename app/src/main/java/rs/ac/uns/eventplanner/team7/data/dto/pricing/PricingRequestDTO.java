package rs.ac.uns.eventplanner.team7.data.dto.pricing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingRequestDTO {
    private double price;
    private double discount;
    private String activeFrom;
}
