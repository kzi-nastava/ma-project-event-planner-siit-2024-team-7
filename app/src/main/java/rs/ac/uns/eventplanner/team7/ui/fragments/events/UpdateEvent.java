package rs.ac.uns.eventplanner.team7.ui.fragments.events;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;

public class UpdateEvent extends Fragment {

    private GetEventResponseDTO eventDTO;
    private MaterialButton updateBudgetBtn;

    public UpdateEvent() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventDTO = getArguments().getParcelable("eventDTO", GetEventResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_event, container, false);
        updateBudgetBtn = view.findViewById(R.id.update_budget_btn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateBudgetBtn.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("eventBudget", eventDTO.getBudget());
            View view1 = getView();
            if (view1 == null) return;
            Navigation.findNavController(view1).navigate(R.id.navigate_to_budget_management, bundle);
        });
    }
}