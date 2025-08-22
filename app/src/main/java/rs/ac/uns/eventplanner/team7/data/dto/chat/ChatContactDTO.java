package rs.ac.uns.eventplanner.team7.data.dto.chat;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.WithImage;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatContactDTO implements BasicCard, WithImage, Parcelable {
    private Integer userId;
    private String userEmail;
    private String photoUrl;
    private boolean read;

    protected ChatContactDTO(Parcel in) {
        userId = in.readInt();
        userEmail = in.readString();
        photoUrl = in.readString();
        read = in.readByte() != 0;
    }

    public static final Creator<ChatContactDTO> CREATOR = new Creator<>() {
        @Override
        public ChatContactDTO createFromParcel(Parcel in) {
            return new ChatContactDTO(in);
        }

        @Override
        public ChatContactDTO[] newArray(int size) {
            return new ChatContactDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(userEmail);
        dest.writeString(photoUrl);
        dest.writeByte((byte) (read ? 1 : 0));
    }

    @Override
    public Integer getId() {
        return userId;
    }

    @Override
    public String getTitle() {
        return userEmail;
    }

    @Override
    public String getSubtitle() {
        return "";
    }

    @Override
    public String getCoverImage() {
        return photoUrl;
    }
}
