package rs.ac.uns.eventplanner.team7.model;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkDay implements Comparable<WorkDay> {

    private DayOfWeek day;
    private LocalTime workTimeStart;
    private LocalTime workTimeEnd;

    public WorkDay(DayOfWeek day, LocalTime workTimeStart, LocalTime workTimeEnd) {
        if (workTimeStart.equals(workTimeEnd) || workTimeStart.isAfter(workTimeEnd))
            throw new IllegalArgumentException("Start and end time must differ");
        this.day = day;
        this.workTimeStart = workTimeStart;
        this.workTimeEnd = workTimeEnd;
    }

    public boolean isWithinWorkingHours(LocalDateTime timeSlot) {
        if (timeSlot.getDayOfWeek() != this.day) return false;
        LocalTime time = timeSlot.toLocalTime();
        return (this.workTimeStart.isBefore(time) || this.workTimeStart.equals(time))
                && (this.workTimeEnd.isAfter(time) || this.workTimeEnd.equals(time)) ;
    }

    @Override
    public int compareTo(WorkDay other) {
        int dayComparison = this.day.compareTo(other.day);
        if (dayComparison != 0) return dayComparison;

        int startComparison = this.workTimeStart.compareTo(other.workTimeStart);
        return startComparison != 0 ? startComparison : this.workTimeEnd.compareTo(other.workTimeEnd);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkDay workDay = (WorkDay) o;
        return day == workDay.day
                && Objects.equals(workTimeStart, workDay.workTimeStart)
                && Objects.equals(workTimeEnd, workDay.workTimeEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, workTimeStart, workTimeEnd);
    }
}
