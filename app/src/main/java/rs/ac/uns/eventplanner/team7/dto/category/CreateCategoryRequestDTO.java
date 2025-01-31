package rs.ac.uns.eventplanner.team7.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateCategoryRequestDTO {
    private String name;
    private String description;
}
