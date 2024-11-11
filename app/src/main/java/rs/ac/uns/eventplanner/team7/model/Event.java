package rs.ac.uns.eventplanner.team7.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.enums.EventVisibility;
import rs.ac.uns.eventplanner.team7.utils.DateConverter;

@Getter @Setter
public class Event implements Parcelable {

    private String name;
    private String description;
    private int maxParticipants;
    private LocalDateTime date;
    private Location place;
    private EventVisibility visibility;
    private EventType type;
    private List<Activity> activities;

    public Event() {}

    public Event(String name, String description, int maxParticipants, LocalDateTime date,
                 Location place, EventVisibility visibility,
                 EventType type, List<Activity> activities) {
        this.name = name;
        this.description = description;
        this.maxParticipants = maxParticipants;
        this.date = date;
        this.place = place;
        this.visibility = visibility;
        this.type = type;
        this.activities = activities;
    }

    protected Event(Parcel in) {
        name = in.readString();
        description = in.readString();
        maxParticipants = in.readInt();
        date = DateConverter.toLocalDateTime(in.readLong());
        place = in.readParcelable(Location.class.getClassLoader());
        visibility = EventVisibility.fromInteger(in.readInt());
        type = in.readParcelable(EventType.class.getClassLoader());
        activities = in.createTypedArrayList(Activity.CREATOR);
    }

    public static final Creator<Event> CREATOR = new Creator<>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(maxParticipants);
        dest.writeLong(DateConverter.toLong(date));
        dest.writeParcelable(place, flags);
        dest.writeInt(visibility.ordinal());
        dest.writeParcelable(type, flags);
        dest.writeTypedList(activities);
    }
}
