package rs.ac.uns.eventplanner.team7.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.enums.ItemStatus;

@Getter
@Setter
@NoArgsConstructor
public class Service extends Item {
    private String specifics;

    private Set<WorkDay> workDays;

    private int minDurationInMinutes;

    private int maxDurationInMinutes;

    private int reservationDeadlineInDays;

    private int cancellationDeadlineInDays;
    private boolean isAutomatedReservationConformation;

    public Service(Integer id, String name, String description, Set<String> images, boolean isVisible, ItemStatus status,
                   Location location, Pricing pricing, Category category, Set<EventType> appliesTo, boolean isAvailable,
                   boolean isCurrent, Integer version, Integer parentId, String specifics,
                   Set<WorkDay> workDays, int minDurationInMinutes, int maxDurationInMinutes,
                   int reservationDeadlineInDays, int cancellationDeadlineInDays, boolean isAutomatedReservationConformation) {
        super(id, name, description, images, isVisible, status, location, pricing, category, appliesTo, isAvailable, isCurrent, version, parentId);
        this.specifics = specifics;
        this.workDays = workDays;
        this.minDurationInMinutes = minDurationInMinutes;
        this.maxDurationInMinutes = maxDurationInMinutes;
        this.reservationDeadlineInDays = reservationDeadlineInDays;
        this.cancellationDeadlineInDays = cancellationDeadlineInDays;
        this.isAutomatedReservationConformation = isAutomatedReservationConformation;
    }

    public boolean canMakeReservation(LocalDate desiredDate) {
        WorkDay day = getWorkDay(desiredDate.getDayOfWeek());
        if (day == null) return false;
        return LocalDate.now().plusDays(reservationDeadlineInDays).isBefore(desiredDate);
    }

    public boolean canCancelReservation(LocalDateTime desiredStartDate) {
        return LocalDateTime.now().plusDays(cancellationDeadlineInDays).isBefore(desiredStartDate);
    }

    public boolean isNotWithinDurationLimits(TimeSlot timeSlot) {
        LocalDateTime start = timeSlot.getStartTime(), end = timeSlot.getEndTime();
        LocalDateTime minLimit = start.plusMinutes(minDurationInMinutes);
        LocalDateTime maxLimit = start.plusMinutes(maxDurationInMinutes);
        return (!minLimit.isBefore(end) && !minLimit.isEqual(end)) || (!maxLimit.isAfter(end) && !maxLimit.isEqual(end));
    }

    public boolean hasMinimumDuration(TimeSlot timeSlot) {
        LocalDateTime minimumEnd = timeSlot.getStartTime().plusMinutes(minDurationInMinutes);
        return minimumEnd.isBefore(timeSlot.getEndTime()) || minimumEnd.isEqual(timeSlot.getEndTime());
    }

    public WorkDay getWorkDay(DayOfWeek day) {
        return workDays.stream()
                .filter(workDay -> workDay.getDay() == day).findFirst().orElse(null);
    }
}
