package rs.ac.uns.eventplanner.team7.data.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpgradeAuthUserRequestDTO {
    private String firstName;
    private String lastName;
    private String orgName;
    private String orgDesc;
    private UserRole upgradedRole;
    private String password;
}
