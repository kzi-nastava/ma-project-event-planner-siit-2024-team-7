package rs.ac.uns.eventplanner.team7.data.dto.event_type;


import java.util.List;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Category;

@Getter
@Setter
public class GetEventTypeResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private boolean active;
    private List<Category> recommendedCategories;
}
