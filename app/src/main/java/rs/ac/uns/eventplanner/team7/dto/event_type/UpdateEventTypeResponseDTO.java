package rs.ac.uns.eventplanner.team7.dto.event_type;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Category;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventTypeResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private List<Category> recommendedCategories;
    private boolean active;
}