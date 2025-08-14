package rs.ac.uns.eventplanner.team7.data.dto.item;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.WithImage;
import rs.ac.uns.eventplanner.team7.data.interfaces.WithVersion;
import rs.ac.uns.eventplanner.team7.data.model.Item;
import rs.ac.uns.eventplanner.team7.data.model.Product;

@Getter
@Setter
@NoArgsConstructor
public class BasicItemDTO implements BasicCard, WithImage, WithVersion, Parcelable {

    protected Integer id;
    protected String type;
    protected String name;
    protected double price;
    protected String coverImage;
    protected boolean current;

    public BasicItemDTO(Item item) {
        id = item.getId();
        type = item instanceof Product ? "products" : "services";
        name = item.getName();
        price = item.getPricing().getPrice();
        coverImage = item.getImages().isEmpty() ? null : new ArrayList<>(item.getImages()).get(0);
        current = item.isCurrent();
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
        current = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
        dest.writeByte((byte) (current ? 1 : 0));
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

    @Override
    public boolean isCurrent() {
        return current;
    }
}