package rs.ac.uns.eventplanner.team7.data.dto.user;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.model.Location;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetUserDetailsResponseDTO implements Parcelable {
    private Integer id;
    private String email;
    private String photoURL;
    private String phone;
    private UserRole role;
    private Location location;
    private String firstName; // organizer
    private String lastName; // organizer
    private String orgName; // provider
    private String orgDesc; // provider

    protected GetUserDetailsResponseDTO(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        email = in.readString();
        photoURL = in.readString();
        phone = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
        firstName = in.readString();
        lastName = in.readString();
        orgName = in.readString();
        orgDesc = in.readString();
    }

    public static final Creator<GetUserDetailsResponseDTO> CREATOR = new Creator<>() {
        @Override
        public GetUserDetailsResponseDTO createFromParcel(Parcel in) {
            return new GetUserDetailsResponseDTO(in);
        }

        @Override
        public GetUserDetailsResponseDTO[] newArray(int size) {
            return new GetUserDetailsResponseDTO[size];
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
        dest.writeString(email);
        dest.writeString(photoURL);
        dest.writeString(phone);
        dest.writeParcelable(location, flags);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(orgName);
        dest.writeString(orgDesc);
    }

}
