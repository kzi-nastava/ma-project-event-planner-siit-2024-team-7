package rs.ac.uns.eventplanner.team7.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.interfaces.DetailedCard;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventType implements DetailedCard, Parcelable {

    private Integer id;
    private String name;
    private String description;
    private boolean active;
    private List<Category> recommendedCategories;

    public EventType(EventType eventType) {
        this.id = eventType.getId();
        this.name = eventType.getName();
        this.description = eventType.getDescription();
        this.active = eventType.isActive();
        this.recommendedCategories = eventType.getRecommendedCategories();
    }

    protected EventType(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        description = in.readString();
        active = in.readByte() != 0;
        recommendedCategories = in.createTypedArrayList(Category.CREATOR);
    }

    public static final Creator<EventType> CREATOR = new Creator<>() {
        @Override
        public EventType createFromParcel(Parcel in) {
            return new EventType(in);
        }

        @Override
        public EventType[] newArray(int size) {
            return new EventType[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return name;
    }

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
        dest.writeByte((byte) (active ? 1 : 0));
        dest.writeTypedList(recommendedCategories);
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSubtitle() {
        return active ? "Status: Active" : "Status: Inactive";
    }
}
