package rs.ac.uns.eventplanner.team7.dto.product;

import java.util.Set;

import rs.ac.uns.eventplanner.team7.dto.pricing.PricingResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.WorkDayDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.EventType;

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
