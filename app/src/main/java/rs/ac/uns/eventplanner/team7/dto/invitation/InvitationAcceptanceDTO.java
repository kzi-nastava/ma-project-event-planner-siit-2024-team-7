package rs.ac.uns.eventplanner.team7.dto.invitation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvitationAcceptanceDTO {
    private String email;
    private Integer eventId;
    private String token;
}
