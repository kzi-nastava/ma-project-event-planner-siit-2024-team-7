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
public class EventBudget {
    private Integer id;
    private Set<CategoryBudget> categoryBudgets;
    private double totalBudget;
    private double totalSpent;
    private boolean isDeleted;
}
