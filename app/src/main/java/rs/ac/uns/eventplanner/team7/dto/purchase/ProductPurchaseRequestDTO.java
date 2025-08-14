package rs.ac.uns.eventplanner.team7.dto.purchase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductPurchaseRequestDTO {
    private Integer eventId;
    private Integer productId;
}
