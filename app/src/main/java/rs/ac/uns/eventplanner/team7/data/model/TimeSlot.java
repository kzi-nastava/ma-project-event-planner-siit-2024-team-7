package rs.ac.uns.eventplanner.team7.data.model;

import androidx.annotation.NonNull;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
            endTime = LocalDateTime.of(endTime.toLocalDate(), LocalTime.parse("23:59"));
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalDate getDate() {
        return startTime.toLocalDate();
    }

    public Duration getSlotDuration() {
        return Duration.between(startTime, endTime.plusSeconds(1));
    }

    public boolean isWithin(TimeSlot other) {
        return (startTime.isEqual(other.getStartTime()) || startTime.isAfter(other.getStartTime()))
                && (endTime.isBefore(other.getEndTime()) || endTime.isEqual(other.getEndTime()));
    }

    public String getFormattedStartTime() {
        return startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getFormattedEndTime() {
        return endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    @Override
    public int compareTo(TimeSlot other) {
        if (this.endTime.isBefore(other.startTime)) {
            return -1;
        } else if (this.startTime.isAfter(other.endTime)) {
            return 1;
        } else {
            return 0;
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

    @NonNull
    @Override
    public String toString() {
        return String.format("%s - %s", getFormattedStartTime(), getFormattedEndTime());
    }
}
