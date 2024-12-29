package rs.ac.uns.eventplanner.team7.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Location implements Parcelable {

    private double lat;
    private double lon;
    private String country;
    private String city;
    private String street;
    private String houseNumber;

    public Location() {}

    public Location(double lat, double lon, String country, String city, String street, String houseNumber) {
        this.lat = lat;
        this.lon = lon;
        this.city = city;
        this.country = country;
        this.street = street;
        this.houseNumber = houseNumber;
    }

    protected Location(Parcel in) {
        lat = in.readDouble();
        lon = in.readDouble();
    }

    public static final Creator<Location> CREATOR = new Creator<>() {
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
        dest.writeDouble(lat);
        dest.writeDouble(lon);
    }
}
