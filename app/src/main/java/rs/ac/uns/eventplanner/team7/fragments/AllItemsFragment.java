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

public class AllItemsFragment extends Fragment {

    public AllItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_items, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        MaterialButton filtersButton = view.findViewById(R.id.srv_prd_filters_button);
        MaterialButton sortButton = view.findViewById(R.id.srv_prd_sort_button);

        filtersButton.setOnClickListener(v -> {
            ItemFiltersFragment fragment = new ItemFiltersFragment();
            fragment.show(getChildFragmentManager(), fragment.getTag());
        });

        sortButton.setOnClickListener(v -> {
            ItemSortOptionsFragment fragment = new ItemSortOptionsFragment();
            fragment.show(getChildFragmentManager(), fragment.getTag());
        });
    }
}