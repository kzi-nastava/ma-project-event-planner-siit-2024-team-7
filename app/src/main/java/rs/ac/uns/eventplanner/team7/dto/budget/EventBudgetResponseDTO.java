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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventBudgetResponseDTO implements Parcelable {
    private Integer eventBudgetId;
    private Set<CategoryBudgetResponseDTO> categoryBudgets;
    private double totalBudget;
    private double totalSpent;

    protected EventBudgetResponseDTO(Parcel in) {
        if (in.readByte() == 0) {
            eventBudgetId = null;
        } else {
            eventBudgetId = in.readInt();
        }
        categoryBudgets = new HashSet<>(in.createTypedArrayList(CategoryBudgetResponseDTO.CREATOR));
        totalBudget = in.readDouble();
        totalSpent = in.readDouble();
    }

    public static final Creator<EventBudgetResponseDTO> CREATOR = new Creator<>() {
        @Override
        public EventBudgetResponseDTO createFromParcel(Parcel in) {
            return new EventBudgetResponseDTO(in);
        }

        @Override
        public EventBudgetResponseDTO[] newArray(int size) {
            return new EventBudgetResponseDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (eventBudgetId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(eventBudgetId);
        }
        dest.writeTypedList(categoryBudgets != null ? new ArrayList<>(categoryBudgets) : null);
        dest.writeDouble(totalBudget);
        dest.writeDouble(totalSpent);
    }
}
