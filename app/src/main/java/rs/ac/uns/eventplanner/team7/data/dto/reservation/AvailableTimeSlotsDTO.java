package rs.ac.uns.eventplanner.team7.data.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.TimeSlot;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AvailableTimeSlotsDTO {
    List<TimeSlot> availableTimeSlots;
}
