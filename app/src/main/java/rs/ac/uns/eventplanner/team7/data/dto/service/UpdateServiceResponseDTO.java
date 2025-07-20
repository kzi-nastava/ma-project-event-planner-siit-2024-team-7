package rs.ac.uns.eventplanner.team7.data.dto.service;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.dto.pricing.PricingResponseDTO;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.model.enums.ItemStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateServiceResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private Set<String> images;
    private boolean visible;
    private PricingResponseDTO pricing;
    private String specifics;
    private Set<WorkDayDTO> workDaysDTOs;
    private int minDurationInMinutes;
    private int maxDurationInMinutes;
    private int reservationDeadlineInDays;
    private int cancellationDeadlineInDays;
    private Set<EventType> appliesTo;
    private boolean available;
    private ItemStatus status;
    private String createdAt;
}
