package rs.ac.uns.eventplanner.team7.data.dto.reporting;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.enums.ReportDecision;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReportRequestDTO {
    private ReportDecision decision;
}
