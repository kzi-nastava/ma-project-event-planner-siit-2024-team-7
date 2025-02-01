package rs.ac.uns.eventplanner.team7.model;

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
public class Location implements Parcelable {
    private String country;
    private String city;
    private String street;
    private String houseNumber;

    protected Location(Parcel in) {
        country = in.readString();
        city = in.readString();
        street = in.readString();
        houseNumber = in.readString();
    }

    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(country);
        dest.writeString(city);
        dest.writeString(street);
        dest.writeString(houseNumber);
    }

    public String toAddressString() {
        return String.format("%s %s, %s", street, houseNumber, city);
    }
}
