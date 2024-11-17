package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import rs.ac.uns.eventplanner.team7.R;

public class ServiceManagementFragment extends Fragment {

    public ServiceManagementFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_service_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialTextView title = view.findViewById(R.id.welcomeMessage);

        Bundle args = getArguments();
        if (args != null) {
            title.setText(args.getString("message_key"));
        }

        if (title.getText() == "SERVICE CREATION") {
            view.findViewById(R.id.delete_service_button).setVisibility(View.GONE);
        }
        else if (title.getText() == "SERVICE UDPATE") {
            view.findViewById(R.id.service_category).setEnabled(false);
        }

        MaterialButton categoryRecommendationButton = view.findViewById(R.id.button_recommend_category);
        categoryRecommendationButton.setOnClickListener(v -> {
            CategoryRecommendationFragment fragment = new CategoryRecommendationFragment();
            fragment.show(requireActivity().getSupportFragmentManager(), "RecommendationDialog");
        });

        MaterialButton eventTypesButton = view.findViewById(R.id.event_types_button);
        eventTypesButton.setOnClickListener(v -> {
            SelectableEventTypesFragment fragment = new SelectableEventTypesFragment();
            fragment.show(getChildFragmentManager(), fragment.getTag());
        });
    }
}