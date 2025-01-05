package rs.ac.uns.eventplanner.team7.dto;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;

@Getter
@Setter
public class CategoryResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private CategoryStatus status;
}

