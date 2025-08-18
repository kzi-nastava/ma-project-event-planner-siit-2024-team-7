package rs.ac.uns.eventplanner.team7.data.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.enums.ReservationStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationRequestDTO {

    private ReservationStatus status;
}
