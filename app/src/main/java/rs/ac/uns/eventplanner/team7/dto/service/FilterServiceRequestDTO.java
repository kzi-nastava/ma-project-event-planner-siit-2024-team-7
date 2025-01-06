package rs.ac.uns.eventplanner.team7.dto.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FilterServiceRequestDTO {
    private String name;
    private String categoryName;
    private String eventTypeName;
    private String price;
    private String available;
}
