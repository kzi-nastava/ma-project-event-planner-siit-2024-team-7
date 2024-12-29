package rs.ac.uns.eventplanner.team7.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TimeSlot implements Comparable<TimeSlot> {
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.toLocalDate().equals(endTime.toLocalDate()))
            throw new IllegalArgumentException("Start and end dates must be the same day");
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isOverlapping(TimeSlot other) {
        return equals(other)
                || (startTime.isBefore(other.getEndTime()) && startTime.isAfter(other.getStartTime()))
                || (endTime.isAfter(other.getStartTime()) && endTime.isBefore(other.getEndTime()));
    }

    public boolean isOnTheSameDate(LocalDate date) {
        return startTime.toLocalDate().equals(date);
    }

    @Override
    public int compareTo(TimeSlot other) {
        if (this.endTime.isBefore(other.startTime)) {
            return -1;
        } else if (this.startTime.isAfter(other.endTime)) {
            return 1;
        } else {
            throw new IllegalArgumentException("Overlapping TimeSlots are not allowed in a collection");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(startTime, timeSlot.startTime) &&
                Objects.equals(endTime, timeSlot.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }

    @Override
    public String toString() {
        String date = startTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String from  = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
        String to  = endTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
        return String.format("Date: %s From %s Until %s", date, from, to);
    }
}
