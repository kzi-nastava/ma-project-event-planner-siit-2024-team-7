package rs.ac.uns.eventplanner.team7.dto.pricing;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingResponseDTO implements Parcelable {
    private String itemName;
    private Integer pricingId;
    private double price;
    private double discount;
    private String activeFrom;
    private boolean deleted;

    protected PricingResponseDTO(Parcel in) {
        itemName = in.readString();
        if (in.readByte() == 0) {
            pricingId = null;
        } else {
            pricingId = in.readInt();
        }
        price = in.readDouble();
        discount = in.readDouble();
        activeFrom = in.readString();
        deleted = in.readByte() != 0;
    }

    public static final Creator<PricingResponseDTO> CREATOR = new Creator<>() {
        @Override
        public PricingResponseDTO createFromParcel(Parcel in) {
            return new PricingResponseDTO(in);
        }

        @Override
        public PricingResponseDTO[] newArray(int size) {
            return new PricingResponseDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(itemName);
        if (pricingId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(pricingId);
        }
        dest.writeDouble(price);
        dest.writeDouble(discount);
        dest.writeString(activeFrom);
        dest.writeByte((byte) (deleted ? 1 : 0));
    }
}
