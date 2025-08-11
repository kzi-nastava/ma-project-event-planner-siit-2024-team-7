package rs.ac.uns.eventplanner.team7.data.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemFeedbackRequestDTO {
    private String userEmail;
    private Integer rating;
    private String comment;
    private Integer reservationId;
    private Integer purchaseId;
}
