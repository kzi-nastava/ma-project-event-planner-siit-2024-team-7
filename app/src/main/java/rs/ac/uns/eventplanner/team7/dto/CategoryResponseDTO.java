package rs.ac.uns.eventplanner.team7.dto;

import androidx.annotation.NonNull;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;

@Getter
@Setter
public class CategoryResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private CategoryStatus status;

    public Category toCategory() {
        return new Category(id, name, description, status);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}

