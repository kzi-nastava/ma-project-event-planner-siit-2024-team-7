package rs.ac.uns.eventplanner.team7.data.dto.reservation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.TimeSlot;
import rs.ac.uns.eventplanner.team7.data.model.enums.ReservationStatus;

@Getter
@Setter
@NoArgsConstructor
public class ReservationDTO {
    private Integer reservationId;
    private Integer eventId;
    private Integer serviceId;
    private TimeSlot timeSlot;
    private ReservationStatus status;
}
