package rs.ac.uns.eventplanner.team7.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusynessDTO {
    private Integer id;
    private String date;
    private String name;
}
