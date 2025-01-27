package rs.ac.uns.eventplanner.team7.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavouriteItemResponseDTO {
    private Integer userId;
    private Integer itemId;
    private boolean favourite;
}
