package rs.ac.uns.eventplanner.team7.dto.service;

import java.time.DayOfWeek;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkDayDTO {
    private DayOfWeek day;
    private String workTimeStart;
    private String workTimeEnd;
}
