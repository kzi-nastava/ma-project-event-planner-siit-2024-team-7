package rs.ac.uns.eventplanner.team7.dto.event_type;

import androidx.annotation.NonNull;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.EventType;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetEventTypeResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private boolean active;
    private List<Category> recommendedCategories;

    public EventType toEventType() {
        return new EventType(id, name, description, active, recommendedCategories);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}