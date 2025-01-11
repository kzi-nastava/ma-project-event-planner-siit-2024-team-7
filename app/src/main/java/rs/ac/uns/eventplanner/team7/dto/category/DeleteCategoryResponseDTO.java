package rs.ac.uns.eventplanner.team7.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCategoryResponseDTO {
    private CategoryStatus status;
}
