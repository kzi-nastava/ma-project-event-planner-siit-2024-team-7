package rs.ac.uns.eventplanner.team7.dto.product;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.pricing.PricingResponseDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.EventType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetProductResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private Set<String> images;
    private PricingResponseDTO pricing;
    private Category category;
    private Set<EventType> appliesTo;
    private boolean own;
    private boolean visible;
    private boolean available;
    private boolean favourite;
}
