package rs.ac.uns.eventplanner.team7.dto.service;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.pricing.PricingResponseDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.EventType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetServiceResponseDTO implements Parcelable {
    private Integer id;
    private String name;
    private String description;
    private List<String> images;
    private PricingResponseDTO pricing;
    private Category category;
    private String specifics;
    private List<WorkDayDTO> workDaysDTOs;
    private int minDurationInMinutes;
    private int maxDurationInMinutes;
    private int reservationDeadlineInDays;
    private int cancellationDeadlineInDays;
    private List<EventType> appliesTo;
    private boolean own;
    private boolean visible, available, favourite, reserved;

    protected GetServiceResponseDTO(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        description = in.readString();
        images = in.createStringArrayList();
        pricing = in.readParcelable(PricingResponseDTO.class.getClassLoader());
        category = in.readParcelable(Category.class.getClassLoader());
        specifics = in.readString();
        workDaysDTOs = in.createTypedArrayList(WorkDayDTO.CREATOR);
        minDurationInMinutes = in.readInt();
        maxDurationInMinutes = in.readInt();
        reservationDeadlineInDays = in.readInt();
        cancellationDeadlineInDays = in.readInt();
        appliesTo = in.createTypedArrayList(EventType.CREATOR);
        own = in.readByte() != 0;
        visible = in.readByte() != 0;
        available = in.readByte() != 0;
        favourite = in.readByte() != 0;
        reserved = in.readByte() != 0;
    }

    public static final Creator<GetServiceResponseDTO> CREATOR = new Creator<GetServiceResponseDTO>() {
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
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(name);
        dest.writeString(description);
        dest.writeStringList(images);
        dest.writeParcelable(pricing, flags);
        dest.writeParcelable(category, flags);
        dest.writeString(specifics);
        dest.writeTypedList(workDaysDTOs);
        dest.writeInt(minDurationInMinutes);
        dest.writeInt(maxDurationInMinutes);
        dest.writeInt(reservationDeadlineInDays);
        dest.writeInt(cancellationDeadlineInDays);
        dest.writeTypedList(appliesTo);
        dest.writeByte((byte) (own ? 1 : 0));
        dest.writeByte((byte) (visible ? 1 : 0));
        dest.writeByte((byte) (available ? 1 : 0));
        dest.writeByte((byte) (favourite ? 1 : 0));
        dest.writeByte((byte) (reserved ? 1 : 0));
    }
}
