package rs.ac.uns.eventplanner.team7.dto.service;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.product.GetProductResponseDTO;

@Getter
@Setter
@NoArgsConstructor
public class GetServiceResponseDTO extends GetProductResponseDTO implements Parcelable {
    private String specifics;
    private List<WorkDayDTO> workDaysDTOs;
    private int minDurationInMinutes;
    private int maxDurationInMinutes;
    private int reservationDeadlineInDays;
    private int cancellationDeadlineInDays;
    private boolean reserved;

    protected GetServiceResponseDTO(Parcel in) {
        super(in);
        specifics = in.readString();
        workDaysDTOs = in.createTypedArrayList(WorkDayDTO.CREATOR);
        minDurationInMinutes = in.readInt();
        maxDurationInMinutes = in.readInt();
        reservationDeadlineInDays = in.readInt();
        cancellationDeadlineInDays = in.readInt();
        reserved = in.readByte() != 0;
    }

    public static final Creator<GetServiceResponseDTO> CREATOR = new Creator<>() {
        @Override
        public GetServiceResponseDTO createFromParcel(Parcel in) {
            return new GetServiceResponseDTO(in);
        }

        @Override
        public GetServiceResponseDTO[] newArray(int size) {
            return new GetServiceResponseDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(specifics);
        dest.writeTypedList(workDaysDTOs);
        dest.writeInt(minDurationInMinutes);
        dest.writeInt(maxDurationInMinutes);
        dest.writeInt(reservationDeadlineInDays);
        dest.writeInt(cancellationDeadlineInDays);
        dest.writeByte((byte) (reserved ? 1 : 0));
    }
}
