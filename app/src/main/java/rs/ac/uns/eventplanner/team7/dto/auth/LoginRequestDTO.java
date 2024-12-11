package rs.ac.uns.eventplanner.team7.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDTO {
    @NonNull
    private String email;
    @NonNull
    private String password;
}