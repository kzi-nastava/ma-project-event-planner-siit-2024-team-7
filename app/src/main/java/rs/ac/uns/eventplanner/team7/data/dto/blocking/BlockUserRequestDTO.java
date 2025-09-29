package rs.ac.uns.eventplanner.team7.data.dto.blocking;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlockUserRequestDTO {
    private String blockerEmail;
    private String blockedEmail;
}
