package rs.ac.uns.eventplanner.team7.data.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponseDTO {
    private String token;
    private boolean activationLinkExpired;
    private String suspensionEnd; // UTC timestmap

}
