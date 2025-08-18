package rs.ac.uns.eventplanner.team7.data.dto.feedback;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.enums.FeedbackStatus;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackDTO {
    private Integer id;
    private String userEmail;
    private String comment;
    private int rating;
    private FeedbackStatus status;
    private LocalDateTime createdAt;
    private String eventName;
    private String itemName;
    private String providerOrganization;
}
