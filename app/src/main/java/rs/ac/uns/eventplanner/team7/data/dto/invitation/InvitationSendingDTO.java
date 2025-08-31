package rs.ac.uns.eventplanner.team7.data.dto.invitation;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvitationSendingDTO {
    private String organizerEmail;
    private List<String> recipientEmails;
    private Integer eventId;
}
