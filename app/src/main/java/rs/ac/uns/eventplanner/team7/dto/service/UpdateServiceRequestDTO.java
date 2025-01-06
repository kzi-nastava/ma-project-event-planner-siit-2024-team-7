package rs.ac.uns.eventplanner.team7.dto.service;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.EventType;
import rs.ac.uns.eventplanner.team7.model.Pricing;
import rs.ac.uns.eventplanner.team7.model.Service;
import rs.ac.uns.eventplanner.team7.model.WorkDay;

@Getter
@Setter
public class UpdateServiceRequestDTO {
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

    public void setServiceFields(Service service) {
        service.setName(name);
        service.setDescription(description);
        service.setImages(images);
        service.setVisible(visible);
        service.setPricing(pricing);
        service.setSpecifics(specifics);
        service.setWorkDays(workDays);
        service.setMinDurationInMinutes(minDurationInMinutes);
        service.setMaxDurationInMinutes(maxDurationInMinutes);
        service.setReservationDeadlineInDays(reservationDeadlineInDays);
        service.setCancellationDeadlineInDays(cancellationDeadlineInDays);
        service.setAppliesTo(appliesTo);
        service.setAvailable(available);
    }
}
