package rs.ac.uns.eventplanner.team7.fragments.budgets;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.budget.EventBudgetResponseDTO;

public class BudgetManagement extends Fragment {

    private EventBudgetResponseDTO eventBudgetDTO;
    private TextView titleTextView;

    public BudgetManagement() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventBudgetDTO = getArguments().getParcelable("eventBudget", EventBudgetResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget_management, container, false);
        titleTextView = view.findViewById(R.id.budget_title);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleTextView.setText(eventBudgetDTO.getEventBudgetId().toString());
    }
}