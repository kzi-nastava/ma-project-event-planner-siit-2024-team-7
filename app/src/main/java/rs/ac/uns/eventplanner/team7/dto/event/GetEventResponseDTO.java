package rs.ac.uns.eventplanner.team7.dto.event;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.budget.EventBudgetResponseDTO;
import rs.ac.uns.eventplanner.team7.model.Activity;
import rs.ac.uns.eventplanner.team7.model.EventType;
import rs.ac.uns.eventplanner.team7.model.Location;
import rs.ac.uns.eventplanner.team7.model.enums.EventVisibility;
import rs.ac.uns.eventplanner.team7.utils.DateConverter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetEventResponseDTO implements Parcelable {
    private Integer id;
    private String name;
    private String description;
    private String coverImage;
    private int maxParticipants;
    private int currentParticipants;
    private LocalDateTime date;
    private Location location;
    private EventVisibility visibility;
    private EventType eventType;
    private List<Activity> activities;
    private boolean isFav;
    private EventBudgetResponseDTO budget;

    protected GetEventResponseDTO(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        description = in.readString();
        coverImage = in.readString();
        maxParticipants = in.readInt();
        currentParticipants = in.readInt();
        date = DateConverter.toLocalDateTime(in.readLong());
        location = in.readParcelable(Location.class.getClassLoader(), Location.class);
        visibility = EventVisibility.fromInteger(in.readInt());
        eventType = in.readParcelable(EventType.class.getClassLoader(), EventType.class);
        activities = in.createTypedArrayList(Activity.CREATOR);
        isFav = in.readByte() != 0;
        budget = in.readParcelable(EventBudgetResponseDTO.class.getClassLoader(), EventBudgetResponseDTO.class);
    }

    public static final Creator<GetEventResponseDTO> CREATOR = new Creator<>() {
        @Override
        public GetEventResponseDTO createFromParcel(Parcel in) {
            return new GetEventResponseDTO(in);
        }

        @Override
        public GetEventResponseDTO[] newArray(int size) {
            return new GetEventResponseDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(coverImage);
        dest.writeInt(maxParticipants);
        dest.writeInt(currentParticipants);
        dest.writeLong(DateConverter.toLong(date));
        dest.writeParcelable(location, flags);
        dest.writeInt(visibility.ordinal());
        dest.writeParcelable(eventType, flags);
        dest.writeTypedList(activities);
        dest.writeByte((byte) (isFav ? 1 : 0));
        dest.writeParcelable(budget, flags);
    }
}
