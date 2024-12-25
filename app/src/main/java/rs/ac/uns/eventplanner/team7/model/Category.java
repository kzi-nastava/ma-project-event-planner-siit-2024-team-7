package rs.ac.uns.eventplanner.team7.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;


@Getter @Setter
@AllArgsConstructor
public class Category implements Parcelable {

    private Integer id;
    private String name;
    private String description;
    protected CategoryStatus status;

    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    protected Category(Parcel in) {
        name = in.readString();
        description = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(description);
    }
}
