package rs.ac.uns.eventplanner.team7.dto.budget;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Category;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddCategoryBudgetRequestDTO {
    private Category category;
    private double budget;
}
