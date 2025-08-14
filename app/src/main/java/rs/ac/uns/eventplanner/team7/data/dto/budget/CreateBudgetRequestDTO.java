package rs.ac.uns.eventplanner.team7.data.dto.budget;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBudgetRequestDTO {
    private Integer eventId;
    private Map<String, Double> categoryBudgets;
}
