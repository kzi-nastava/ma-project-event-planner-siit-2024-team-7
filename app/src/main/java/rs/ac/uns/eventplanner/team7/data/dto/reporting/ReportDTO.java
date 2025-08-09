package rs.ac.uns.eventplanner.team7.data.dto.reporting;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReportDTO {

    private Integer id;
    private String reporterEmail;
    private String reason;
    private String reportDate;
    private String reportedUserEmail;
    private ReportedCommentDTO reportedComment;
    private ReportedRatingDTO reportedRating;
    private boolean decided;
}
