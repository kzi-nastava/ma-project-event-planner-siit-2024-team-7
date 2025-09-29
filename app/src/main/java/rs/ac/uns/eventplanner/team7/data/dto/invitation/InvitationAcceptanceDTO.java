package rs.ac.uns.eventplanner.team7.data.dto.invitation;

import android.os.Bundle;

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

    /// To be used ONLY when redirecting to home page with all required params
    public InvitationAcceptanceDTO(Bundle params) {
        email = params.getString("email");
        eventId = params.getInt("eventId");
        token = params.getString("token");
        if (email == null || eventId == null || token == null)
            throw new IllegalStateException("Not all arguments are passed");
    }
}
