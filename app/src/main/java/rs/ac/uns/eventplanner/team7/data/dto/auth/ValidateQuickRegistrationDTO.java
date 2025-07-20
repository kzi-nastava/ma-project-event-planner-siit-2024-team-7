package rs.ac.uns.eventplanner.team7.data.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidateQuickRegistrationDTO {
    private String email;
    private String token;
}