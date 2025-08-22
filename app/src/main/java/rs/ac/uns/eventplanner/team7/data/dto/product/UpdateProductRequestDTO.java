package rs.ac.uns.eventplanner.team7.data.dto.product;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.dto.pricing.PricingRequestDTO;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateProductRequestDTO {
    private String name;
    private String description;
    private Set<String> images;
    private boolean visible;
    private boolean available;
    private PricingRequestDTO pricing;
    private Set<String> appliesTo;
}
