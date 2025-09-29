package rs.ac.uns.eventplanner.team7.data.dto.feedback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.enums.FeedbackStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeedbackRequestDTO {

    private FeedbackStatus decision;
}
