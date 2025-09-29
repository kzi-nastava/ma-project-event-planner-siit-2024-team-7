package rs.ac.uns.eventplanner.team7.data.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequestDTO {
    private String name;
    private String description;
}
