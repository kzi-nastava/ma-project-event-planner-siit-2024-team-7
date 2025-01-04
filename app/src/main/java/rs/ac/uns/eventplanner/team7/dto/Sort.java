package rs.ac.uns.eventplanner.team7.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Sort {

    private String by = "name";
    private String direction = "asc";


    public Sort by(String property) {
        by = property;
        return this;
    }

    public Sort ascending() {
        direction = "asc";
        return this;
    }

    public Sort descending() {
        direction = "desc";
        return this;
    }

    /// default sorting is by name ascending
    public static Sort getDefault() {
        return new Sort().by("name").ascending();
    }

    public void resetToDefault() {
        Sort defaultSort = getDefault();
        by = defaultSort.getBy();
        direction = defaultSort.getDirection();
    }
}
