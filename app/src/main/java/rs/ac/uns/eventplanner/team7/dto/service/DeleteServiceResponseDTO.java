package rs.ac.uns.eventplanner.team7.dto.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.enums.ItemStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteServiceResponseDTO {
    private ItemStatus itemStatus;
}
