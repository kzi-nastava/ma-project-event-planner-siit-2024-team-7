package rs.ac.uns.eventplanner.team7.data.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.TimeSlot;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequestDTO {
    private String organizerEmail;
    private String providerEmail;
    private Integer eventId;
    private Integer serviceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public CreateReservationRequestDTO(
            String organizerEmail,
            String providerEmail,
            Integer eventId,
            Integer serviceId,
            TimeSlot timeSlot
    ) {
        this.organizerEmail = organizerEmail;
        this.providerEmail = providerEmail;
        this.eventId = eventId;
        this.serviceId = serviceId;
        startTime = timeSlot.getStartTime();
        endTime = timeSlot.getEndTime();
    }
}


