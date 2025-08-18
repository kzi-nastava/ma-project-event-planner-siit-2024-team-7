package rs.ac.uns.eventplanner.team7.data.dto.event;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FutureReservableEventsDTO implements Parcelable {

    private List<GetEventResponseDTO> events;

    protected FutureReservableEventsDTO(Parcel in) {
        events = in.createTypedArrayList(GetEventResponseDTO.CREATOR);
    }

    public static final Creator<FutureReservableEventsDTO> CREATOR = new Creator<>() {
        @Override
        public FutureReservableEventsDTO createFromParcel(Parcel in) {
            return new FutureReservableEventsDTO(in);
        }

        @Override
        public FutureReservableEventsDTO[] newArray(int size) {
            return new FutureReservableEventsDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(events);
    }
}