package rs.ac.uns.eventplanner.team7.dto.service;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.pricing.UpdatePricingRequestDTO;
import rs.ac.uns.eventplanner.team7.model.EventType;

@Getter
@Setter
public class UpdateServiceRequestDTO {
    private String name;
    private String description;
    private Set<String> images;
    private boolean visible;
    private UpdatePricingRequestDTO pricing;
    private String specifics;
    private Set<WorkDayDTO> workDaysDTOs;
    private int minDurationInMinutes;
    private int maxDurationInMinutes;
    private int reservationDeadlineInDays;
    private int cancellationDeadlineInDays;
    private Set<EventType> appliesTo;
    private boolean available;
}
