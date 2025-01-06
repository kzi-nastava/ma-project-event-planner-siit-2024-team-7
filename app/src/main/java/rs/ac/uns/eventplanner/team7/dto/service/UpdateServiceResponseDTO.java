package rs.ac.uns.eventplanner.team7.dto.service;

import java.time.Instant;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.EventType;
import rs.ac.uns.eventplanner.team7.model.Pricing;
import rs.ac.uns.eventplanner.team7.model.Service;
import rs.ac.uns.eventplanner.team7.model.WorkDay;
import rs.ac.uns.eventplanner.team7.model.enums.ItemStatus;

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
    private Pricing pricing;
    private String specifics;
    private Set<WorkDay> workDays;
    private int minDurationInMinutes;
    private int maxDurationInMinutes;
    private int reservationDeadlineInDays;
    private int cancellationDeadlineInDays;
    private Set<EventType> appliesTo;
    private boolean available;
    private ItemStatus status;
    private Instant createdAt;

    public UpdateServiceResponseDTO(Service service) {
        this.id = service.getId();
        this.name = service.getName();
        this.description = service.getDescription();
        this.images = service.getImages();
        this.visible = service.isVisible();
        this.pricing = service.getPricing();
        this.specifics = service.getSpecifics();
        this.workDays = service.getWorkDays();
        this.minDurationInMinutes = service.getMinDurationInMinutes();
        this.maxDurationInMinutes = service.getMaxDurationInMinutes();
        this.reservationDeadlineInDays = service.getReservationDeadlineInDays();
        this.cancellationDeadlineInDays = service.getCancellationDeadlineInDays();
        this.appliesTo = service.getAppliesTo();
        this.available = service.isAvailable();
        this.status = service.getStatus();
        this.createdAt = Instant.now();
    }
}
