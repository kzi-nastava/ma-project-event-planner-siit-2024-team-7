package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

import rs.ac.uns.eventplanner.team7.R;

public class AllEventsFragment extends Fragment {

    public AllEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        MaterialButton filtersButton = view.findViewById(R.id.event_filters_button);
        MaterialButton sortButton = view.findViewById(R.id.event_sort_button);

        filtersButton.setOnClickListener(v -> {
            EventFiltersFragment fragment = new EventFiltersFragment();
            fragment.show(getChildFragmentManager(), fragment.getTag());
        });

        sortButton.setOnClickListener(v -> {
            EventSortOptionsFragment fragment = new EventSortOptionsFragment();
            fragment.show(getChildFragmentManager(), fragment.getTag());
        });
    }
}