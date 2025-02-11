package rs.ac.uns.eventplanner.team7.dto.service;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.DayOfWeek;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkDayDTO implements BasicCard, Parcelable {
    private DayOfWeek day;
    private String workTimeStart;
    private String workTimeEnd;

    protected WorkDayDTO(Parcel in) {
        day = DayOfWeek.valueOf(in.readString());
        workTimeStart = in.readString();
        workTimeEnd = in.readString();
    }

    public static final Creator<WorkDayDTO> CREATOR = new Creator<>() {
        @Override
        public WorkDayDTO createFromParcel(Parcel in) {
            return new WorkDayDTO(in);
        }

        @Override
        public WorkDayDTO[] newArray(int size) {
            return new WorkDayDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(workTimeStart);
        dest.writeString(workTimeEnd);
    }

    @Override
    public Integer getId() {
        return -1;
    }

    @Override
    public String getTitle() {
        return day.toString();
    }

    @Override
    public String getSubtitle() {
        return String.format("%s - %s", workTimeStart, workTimeEnd);
    }
}
