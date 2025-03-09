package rs.ac.uns.eventplanner.team7.dto.budget;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.eventplanner.team7.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.model.Category;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBudgetResponseDTO implements Parcelable {
    private Integer categoryBudgetId;
    private Set<BasicItemDTO> items;
    private Category category;
    private double budget;
    private double spent;

    protected CategoryBudgetResponseDTO(Parcel in) {
        if (in.readByte() == 0) {
            categoryBudgetId = null;
        } else {
            categoryBudgetId = in.readInt();
        }
        items = new HashSet<>(in.createTypedArrayList(BasicItemDTO.CREATOR));
        category = in.readParcelable(Category.class.getClassLoader(), Category.class);
        budget = in.readDouble();
        spent = in.readDouble();
    }

    public static final Creator<CategoryBudgetResponseDTO> CREATOR = new Creator<>() {
        @Override
        public CategoryBudgetResponseDTO createFromParcel(Parcel in) {
            return new CategoryBudgetResponseDTO(in);
        }

        @Override
        public CategoryBudgetResponseDTO[] newArray(int size) {
            return new CategoryBudgetResponseDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (categoryBudgetId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(categoryBudgetId);
        }
        dest.writeTypedList(items != null ? new ArrayList<>(items) : null);
        dest.writeParcelable(category, flags);
        dest.writeDouble(budget);
        dest.writeDouble(spent);
    }
}
