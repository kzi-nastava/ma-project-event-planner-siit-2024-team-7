package rs.ac.uns.eventplanner.team7.ui.fragments.reservation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import java.time.ZoneOffset;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;
import rs.ac.uns.eventplanner.team7.utils.DateConverter;


public class ReservationEventStepFragment extends Fragment {

    public ReservationEventStepFragment() {
        // Required empty public constructor
    }

    public static ReservationEventStepFragment newInstance() {
        return new ReservationEventStepFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reservation_event_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        var viewModel = new ViewModelProvider(requireParentFragment()).get(ReservationViewModel.class);

        MaterialAutoCompleteTextView eventsDropdown = view.findViewById(R.id.reservation_event_spinner);
        eventsDropdown.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                viewModel.getOrganizerEvents().getEvents())
        );

        eventsDropdown.setOnItemClickListener(
                (parent, v, position, id) -> {
                    var event = (GetEventResponseDTO) parent.getItemAtPosition(position);
                    viewModel.setSelectedEvent(event);
                    viewModel.setMaxDate(DateConverter.toLong(event.getDate().plusDays(1)));
                    viewModel.setSelectedDate(event.getDate().toInstant(ZoneOffset.UTC).toEpochMilli());
                }
        );

        MaterialTextView errorView = view.findViewById(R.id.error_event_not_selected);
        viewModel.getEventValid().observe(getViewLifecycleOwner(), valid ->
                errorView.setVisibility(valid ? View.INVISIBLE : View.VISIBLE));
    }
}