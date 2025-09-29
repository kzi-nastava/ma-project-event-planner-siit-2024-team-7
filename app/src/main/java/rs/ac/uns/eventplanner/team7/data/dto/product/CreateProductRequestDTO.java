package rs.ac.uns.eventplanner.team7.data.dto.product;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.dto.pricing.PricingRequestDTO;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CreateProductRequestDTO {
    private String name;
    private String description;
    //    private Set<String> images;
    private boolean visible;
    private PricingRequestDTO pricing;
    private Integer categoryId;
    private Set<String> appliesTo;
    private boolean available;
    private boolean recommended;
}
