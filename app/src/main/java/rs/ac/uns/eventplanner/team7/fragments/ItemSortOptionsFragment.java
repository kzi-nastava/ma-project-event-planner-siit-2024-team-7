package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import lombok.Getter;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.Sort;
import rs.ac.uns.eventplanner.team7.model.interfaces.SearchActionsListener;

public class ItemSortOptionsFragment extends BottomSheetDialogFragment {
    @Getter
    private final Sort sort;
    @IdRes
    private final int ascendingOrder, descendingOrder;
    private RadioGroup sortOptions, sortDirection;
    private SearchActionsListener listener;
    private boolean pendingReset;
    @IdRes
    private int pendingSort, pendingDirection;

    private ItemSortOptionsFragment(SearchActionsListener listener) {
        this();
        this.listener = listener;
    }


    public ItemSortOptionsFragment() {
        sort = Sort.getDefault();
        ascendingOrder = R.id.sort_ascending_item;
        descendingOrder = R.id.sort_descending_item;
    }

    public static ItemSortOptionsFragment newInstance(SearchActionsListener listener) {
        return new ItemSortOptionsFragment(listener);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_sort_options, container, false);
        sortOptions = view.findViewById(R.id.item_sort_options_group);
        sortDirection = view.findViewById(R.id.item_sort_direction_group);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton closeButton = view.findViewById(R.id.item_sort_options_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        setupRadioButtons();

        if (listener == null) return;
        MaterialButton applySort = view.findViewById(R.id.apply_sorting_items_button);
        applySort.setOnClickListener(v -> {
            listener.onSortApplied();
            dismiss();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!pendingReset) return;
        doReset();
        pendingReset = false;
    }

    private void doReset() {
        sortOptions.check(pendingSort);
        sortDirection.check(pendingDirection);
        pendingSort = pendingDirection = -1;
    }

    public void doShake() {
        pendingSort = R.id.sort_item_price;
        if (!sort.getBy().equals("pricing.price")) {
            sort.by("pricing.price").ascending();
            pendingDirection = ascendingOrder;
        } else {
            pendingDirection = sort.reverseOrder().isAscending()
                    ? ascendingOrder : descendingOrder;
        }
        // Can't call listener.onSortApplied() as it is null if the fragment hasn't been opened yet
        if (isVisible()) {
            doReset();
            dismiss();
        }
        else scheduleReset(false);
    }

    public void scheduleReset(boolean useDefaultSort) {
        pendingReset = true;
        if (!useDefaultSort) return;
        pendingSort = R.id.sort_item_name;
        pendingDirection = ascendingOrder;
    }

    private void setupRadioButtons() {
        sortOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.sort_item_name) sort.by("name");
            else if (checkedId == R.id.sort_item_price) sort.by("pricing.price");
            else if (checkedId == R.id.sort_item_category) sort.by("category.name");
            else sort.by("location.city");
        });
        sortDirection.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == ascendingOrder) sort.ascending();
            else sort.descending();
        });
    }
}