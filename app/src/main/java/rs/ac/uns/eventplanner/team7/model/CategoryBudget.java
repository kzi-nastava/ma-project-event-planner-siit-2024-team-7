package rs.ac.uns.eventplanner.team7.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBudget {
    private Integer id;
    private Category category;
    private Set<Item> items;
    private double budget;
    private double spent;
    private boolean isDeleted;
}
