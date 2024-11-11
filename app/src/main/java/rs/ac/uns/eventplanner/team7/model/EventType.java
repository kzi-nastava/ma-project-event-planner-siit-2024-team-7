package rs.ac.uns.eventplanner.team7.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class EventType implements Parcelable {

    private String name;
    private String description;
    private boolean isActive;
    private List<Category> recommendedCategories;

    public EventType() {}

    public EventType(String name, String description, boolean isActive) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.recommendedCategories = new ArrayList<>();
    }

    public EventType(String name, String description, boolean isActive,
                     List<Category> recommendedCategories) {
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.recommendedCategories = recommendedCategories;
    }

    protected EventType(Parcel in) {
        name = in.readString();
        description = in.readString();
        isActive = in.readByte() != 0;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeTypedList(recommendedCategories);
    }
}
