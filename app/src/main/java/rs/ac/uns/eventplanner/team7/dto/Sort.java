package rs.ac.uns.eventplanner.team7.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Sort {

    private String by = "name";
    private String direction = "asc";


    /// default sorting is by name ascending
    public static Sort getDefault() {
        return new Sort("name", "asc");
    }

    public void resetToDefault() {
        Sort defaultSort = getDefault();
        by = defaultSort.getBy();
        direction = defaultSort.getDirection();
    }
}
