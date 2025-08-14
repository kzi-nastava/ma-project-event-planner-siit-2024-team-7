package rs.ac.uns.eventplanner.team7.data.dto.event_type;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Category;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateEventTypeResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private List<Category> recommendedCategories;
    private boolean isActive;
}
