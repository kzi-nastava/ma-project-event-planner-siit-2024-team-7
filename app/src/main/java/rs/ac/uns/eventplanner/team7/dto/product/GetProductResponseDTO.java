package rs.ac.uns.eventplanner.team7.dto.product;

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
public class GetProductResponseDTO implements Parcelable {
    protected Integer id;
    protected String name;
    protected String description;
    protected List<String> images;
    protected PricingResponseDTO pricing;
    protected Category category;
    protected List<EventType> appliesTo;
    protected boolean own;
    protected boolean visible;
    protected boolean available;
    protected boolean favourite;
    private boolean purchased;
    protected boolean current;

    protected GetProductResponseDTO(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        description = in.readString();
        images = in.createStringArrayList();
        pricing = in.readParcelable(PricingResponseDTO.class.getClassLoader(), PricingResponseDTO.class);
        category = in.readParcelable(Category.class.getClassLoader(), Category.class);
        appliesTo = in.createTypedArrayList(EventType.CREATOR);
        own = in.readByte() != 0;
        visible = in.readByte() != 0;
        available = in.readByte() != 0;
        favourite = in.readByte() != 0;
        current = in.readByte() != 0;
    }

    public static final Creator<GetProductResponseDTO> CREATOR = new Creator<>() {
        @Override
        public GetProductResponseDTO createFromParcel(Parcel in) {
            return new GetProductResponseDTO(in);
        }

        @Override
        public GetProductResponseDTO[] newArray(int size) {
            return new GetProductResponseDTO[size];
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
        dest.writeTypedList(appliesTo);
        dest.writeByte((byte) (own ? 1 : 0));
        dest.writeByte((byte) (visible ? 1 : 0));
        dest.writeByte((byte) (available ? 1 : 0));
        dest.writeByte((byte) (favourite ? 1 : 0));
        dest.writeByte((byte) (current ? 1 : 0));
    }
}
