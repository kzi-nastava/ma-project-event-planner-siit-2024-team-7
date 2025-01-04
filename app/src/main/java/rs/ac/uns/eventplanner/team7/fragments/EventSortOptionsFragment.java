package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import lombok.Getter;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.Sort;
import rs.ac.uns.eventplanner.team7.model.interfaces.SearchActionsListener;

@Getter
public class EventSortOptionsFragment extends BottomSheetDialogFragment {

    private final Sort sort;
    private RadioGroup sortOptions;
    private RadioGroup sortDirection;
    private SearchActionsListener listener;
    private boolean isCreated, pendingReset;

    public EventSortOptionsFragment() {
        sort = Sort.getDefault();
    }

    public EventSortOptionsFragment(SearchActionsListener listener) {
        this();
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_sort_options, container, false);
        sortOptions = view.findViewById(R.id.event_sort_options_group);
        sortDirection = view.findViewById(R.id.event_sort_direction_group);
        isCreated = true;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton closeButton = view.findViewById(R.id.event_sort_options_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        setupRadioButtons();

        MaterialButton applySort = view.findViewById(R.id.apply_sorting_events_button);
        applySort.setOnClickListener(v -> {
            listener.onSortApplied();
            dismiss();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!pendingReset) return;
        sortOptions.check(R.id.sort_event_name);
        sortDirection.check(R.id.sort_ascending_event);
        pendingReset = false;
    }

    private void setupRadioButtons() {
        sortOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.sort_event_name) sort.by("name");
            else if (checkedId == R.id.sort_event_participants) sort.by("maxParticipants");
            else if (checkedId == R.id.sort_event_type) sort.by("type.name");
            else if (checkedId == R.id.sort_event_location) sort.by("place.city");
            else sort.by("date");
        });
        sortDirection.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.sort_ascending_event) sort.ascending();
            else sort.descending();
        });
    }

    public void resetSort() {
        if (!isCreated) return; // Can cause an exception if the fragment hasn't been created yet
        pendingReset = true;
    }

}
