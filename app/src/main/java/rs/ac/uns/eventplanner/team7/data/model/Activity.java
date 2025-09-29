package rs.ac.uns.eventplanner.team7.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.utils.DateConverter;

@Getter @Setter
public class Activity implements Parcelable {

    private String name;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;

    public Activity() {}

    public Activity(String name, String description, LocalTime start,
                    LocalTime end, String location) {
        this.name = name;
        this.description = description;
        this.startTime = start;
        this.endTime = end;
        this.location = location;
    }

    protected Activity(Parcel in) {
        name = in.readString();
        description = in.readString();
        startTime = DateConverter.toLocalTime(in.readLong());
        endTime = DateConverter.toLocalTime(in.readLong());
        location = in.readString();
    }

    public static final Creator<Activity> CREATOR = new Creator<>() {
        @Override
        public Activity createFromParcel(Parcel in) {
            return new Activity(in);
        }

        @Override
        public Activity[] newArray(int size) {
            return new Activity[size];
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
        dest.writeLong(DateConverter.toLong(startTime));
        dest.writeLong(DateConverter.toLong(endTime));
        dest.writeString(location);
    }
}
