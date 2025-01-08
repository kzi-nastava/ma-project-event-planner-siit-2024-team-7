package rs.ac.uns.eventplanner.team7.dto.service;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.CreatePricingRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.WorkDayDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.EventType;
import rs.ac.uns.eventplanner.team7.model.Pricing;
import rs.ac.uns.eventplanner.team7.model.Service;
import rs.ac.uns.eventplanner.team7.model.WorkDay;
import rs.ac.uns.eventplanner.team7.model.enums.ItemStatus;

@Getter
@Setter
public class CreateServiceRequestDTO {
    private String name;
    private String description;
    private Set<String> images;
    private boolean visible;
    private CreatePricingRequestDTO pricing;
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

    public Service toService() {
        Service service = new Service();
        service.setId(null);
        service.setName(name);
        service.setDescription(description);
        service.setImages(images);
        service.setVisible(visible);
        if (recommended) service.setStatus(ItemStatus.PENDING);
        else service.setStatus(ItemStatus.ACTIVE);
        // location is set later
        service.setPricing(pricing.toPricing());
        service.setCategory(category);
        service.setAppliesTo(appliesTo);
        service.setAvailable(available);
        service.setSpecifics(specifics);

        Set<WorkDay> workDays = new HashSet<>();
        for (WorkDayDTO workDayDTO : workDaysDTOs) {
            workDays.add(workDayDTO.toWorkDay());
        }
        service.setWorkDays(workDays);

        service.setMinDurationInMinutes(minDurationInMinutes);
        service.setMaxDurationInMinutes(maxDurationInMinutes);
        service.setReservationDeadlineInDays(reservationDeadlineInDays);
        service.setCancellationDeadlineInDays(cancellationDeadlineInDays);
        return service;
    }
}
