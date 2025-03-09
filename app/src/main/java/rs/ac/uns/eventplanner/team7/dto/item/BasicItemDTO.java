package rs.ac.uns.eventplanner.team7.dto.item;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.Item;
import rs.ac.uns.eventplanner.team7.model.Product;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.WithImage;

@Getter
@Setter
@NoArgsConstructor
public class BasicItemDTO implements BasicCard, WithImage, Parcelable {

    protected Integer id;
    protected String type;
    protected String name;
    protected double price;
    protected String coverImage;

    public BasicItemDTO(Item item) {
        id = item.getId();
        type = item instanceof Product ? "products" : "services";
        name = item.getName();
        price = item.getPricing().getPrice();
        coverImage = item.getImages().isEmpty() ? null : new ArrayList<>(item.getImages()).get(0);
    }

    protected BasicItemDTO(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        type = in.readString();
        name = in.readString();
        price = in.readDouble();
        coverImage = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(type);
        dest.writeString(name);
        dest.writeDouble(price);
        dest.writeString(coverImage);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BasicItemDTO> CREATOR = new Creator<>() {
        @Override
        public BasicItemDTO createFromParcel(Parcel in) {
            return new BasicItemDTO(in);
        }

        @Override
        public BasicItemDTO[] newArray(int size) {
            return new BasicItemDTO[size];
        }
    };


    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSubtitle() {
        return String.valueOf(price);
    }
}