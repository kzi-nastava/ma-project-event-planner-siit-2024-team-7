package rs.ac.uns.eventplanner.team7.data.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventStatistics {
    private Integer eventId;
    private Integer currentParticipants;
    private Double attendanceRate; // from 0-1
    private Double avgRating; // from 0-5
}
