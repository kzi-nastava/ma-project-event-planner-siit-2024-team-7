package rs.ac.uns.eventplanner.team7.data.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportRequestDTO {

    private String reporterEmail;
    private String reportedEmail;
    private Integer reportedCommentId;
    private Integer reportedRatingId;
    private String reason;

}
