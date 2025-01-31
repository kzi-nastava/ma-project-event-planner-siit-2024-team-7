package rs.ac.uns.eventplanner.team7.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WorkDay implements Comparable<WorkDay>, Parcelable {

    private DayOfWeek day;
    private LocalTime workTimeStart;
    private LocalTime workTimeEnd;

    public WorkDay(DayOfWeek day, LocalTime workTimeStart, LocalTime workTimeEnd) {
        if (workTimeStart.equals(workTimeEnd) || workTimeStart.isAfter(workTimeEnd))
            throw new IllegalArgumentException("Start and end time must differ");
        this.day = day;
        this.workTimeStart = workTimeStart;
        this.workTimeEnd = workTimeEnd;
    }

    protected WorkDay(Parcel in) {
        day = DayOfWeek.valueOf(in.readString());
        workTimeStart = LocalTime.parse(in.readString());
        workTimeEnd = LocalTime.parse(in.readString());
    }

    public static final Creator<WorkDay> CREATOR = new Creator<>() {
        @Override
        public WorkDay createFromParcel(Parcel in) {
            return new WorkDay(in);
        }

        @Override
        public WorkDay[] newArray(int size) {
            return new WorkDay[size];
        }
    };

    @Override
    public int compareTo(WorkDay other) {
        int dayComparison = this.day.compareTo(other.day);
        if (dayComparison != 0) return dayComparison;

        int startComparison = this.workTimeStart.compareTo(other.workTimeStart);
        return startComparison != 0 ? startComparison : this.workTimeEnd.compareTo(other.workTimeEnd);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkDay workDay = (WorkDay) o;
        return day == workDay.day
                && Objects.equals(workTimeStart, workDay.workTimeStart)
                && Objects.equals(workTimeEnd, workDay.workTimeEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, workTimeStart, workTimeEnd);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(day.toString());
        dest.writeString(workTimeStart.toString());
        dest.writeString(workTimeEnd.toString());
    }
}
