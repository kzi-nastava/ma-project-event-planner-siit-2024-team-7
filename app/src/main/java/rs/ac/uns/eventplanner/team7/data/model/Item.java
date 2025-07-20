package rs.ac.uns.eventplanner.team7.data.model;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.enums.ItemStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class Item {
    protected Integer id;
    protected String name;
    protected String description;
    protected Set<String> images;
    protected boolean isVisible;
    protected ItemStatus status;
    protected Location location;
    protected Pricing pricing;
    protected Category category;
    protected Set<EventType> appliesTo;
    private boolean isAvailable;
}
