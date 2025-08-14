package rs.ac.uns.eventplanner.team7.data.dto.service;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.dto.pricing.PricingRequestDTO;
import rs.ac.uns.eventplanner.team7.data.model.Category;
import rs.ac.uns.eventplanner.team7.data.model.EventType;

@Getter
@Setter
public class CreateServiceRequestDTO {
    private String name;
    private String description;
//    private Set<String> images;
    private boolean visible;
    private PricingRequestDTO pricing;
    private Category category;
    private String specifics;
    private Set<WorkDayDTO> workDaysDTOs;
    private int minDurationInMinutes;
    private int maxDurationInMinutes;
    private int reservationDeadlineInDays;
    private int cancellationDeadlineInDays;
    private Set<EventType> appliesTo;
    private boolean available;
    private boolean recommended;
    private boolean automatedReservationConformation;
}
