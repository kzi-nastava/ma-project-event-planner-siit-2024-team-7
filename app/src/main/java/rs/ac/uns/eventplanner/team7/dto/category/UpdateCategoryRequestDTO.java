package rs.ac.uns.eventplanner.team7.dto.category;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;

@Getter
@Setter
public class UpdateCategoryRequestDTO {
    private String name;
    private String description;
    private CategoryStatus status;
}
