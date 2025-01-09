package rs.ac.uns.eventplanner.team7.dto.service;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.pricing.PricingResponseDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.EventType;
import rs.ac.uns.eventplanner.team7.model.Pricing;
import rs.ac.uns.eventplanner.team7.model.Service;
import rs.ac.uns.eventplanner.team7.model.WorkDay;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetServiceResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private Set<String> images;
    private PricingResponseDTO pricing;
    private Category category;
    private String specifics;
    private Set<WorkDay> workDays;
    private int minDurationInMinutes;
    private int maxDurationInMinutes;
    private int reservationDeadlineInDays;
    private int cancellationDeadlineInDays;
    private Set<EventType> appliesTo;
    private boolean isOwn;

    public GetServiceResponseDTO(Service service) {
        this.id = service.getId();
        this.name = service.getName();
        this.description = service.getDescription();
        this.images = service.getImages();

        Pricing pricing = service.getPricing();
        this.pricing = new PricingResponseDTO(service.getName(), pricing);

        this.category = service.getCategory();
        this.specifics = service.getSpecifics();
        this.workDays = service.getWorkDays();
        this.minDurationInMinutes = service.getMinDurationInMinutes();
        this.maxDurationInMinutes = service.getMaxDurationInMinutes();
        this.reservationDeadlineInDays = service.getReservationDeadlineInDays();
        this.cancellationDeadlineInDays = service.getCancellationDeadlineInDays();
        this.appliesTo = service.getAppliesTo();
    }
}
