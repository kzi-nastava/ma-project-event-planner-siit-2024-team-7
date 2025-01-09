package rs.ac.uns.eventplanner.team7.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.WorkDay;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkDayDTO {
    private DayOfWeek day;
    private String workTimeStart;
    private String workTimeEnd;

    public WorkDayDTO(WorkDay workDay) {
        this.day = workDay.getDay();
        this.workTimeStart = workDay.getWorkTimeStart().toString();
        this.workTimeEnd = workDay.getWorkTimeEnd().toString();
    }

    public WorkDay toWorkDay() {
        WorkDay workDay = new WorkDay();
        workDay.setDay(day);
        workDay.setWorkTimeStart(LocalTime.parse(workTimeStart));
        workDay.setWorkTimeEnd(LocalTime.parse(workTimeEnd));
        return workDay;
    }
}
