package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.CategoryResponseDTO;


public class UpdateEventTypeFragment extends Fragment {

    private List<CategoryResponseDTO> selectedCategories; // Categories selected in the child fragment
    private Integer eventTypeId;

    public UpdateEventTypeFragment() {
        // Required empty public constructor
    }

    public static UpdateEventTypeFragment newInstance(String param1, String param2) {
        UpdateEventTypeFragment fragment = new UpdateEventTypeFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (getArguments() != null) {
            eventTypeId = getArguments().getInt("eventTypeId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_event_type, container, false);

        EventTypeCategoryManipulationFragment categoryFragment = EventTypeCategoryManipulationFragment.newInstance();
        categoryFragment.setCategorySelectionListener(selectedCategories -> {
            this.selectedCategories = selectedCategories;
        });

        getChildFragmentManager().beginTransaction()
                .replace(R.id.category_fragment_container, categoryFragment, "EventTypeCategoryManipulationFragmentTag")
                .commit();

        return view;
    }
}