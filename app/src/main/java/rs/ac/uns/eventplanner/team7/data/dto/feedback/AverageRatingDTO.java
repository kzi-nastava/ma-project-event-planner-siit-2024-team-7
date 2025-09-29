package rs.ac.uns.eventplanner.team7.data.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AverageRatingDTO {
    private Integer id;     // can be itemId, eventId or providerId
    private double rating;
    private int feedbackCount;
}
