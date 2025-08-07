package rs.ac.uns.eventplanner.team7.ui.fragments.reservation;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static rs.ac.uns.eventplanner.team7.utils.ClientUtils.injectService;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.reservation.AvailableTimeSlotsDTO;
import rs.ac.uns.eventplanner.team7.data.model.TimeSlot;
import rs.ac.uns.eventplanner.team7.data.services.ReservationService;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;


public class ReservationTimestampStepFragment extends Fragment {

    private final ReservationService reservationService = injectService(ReservationService.class);

    private ReservationViewModel viewModel;

    private ArrayAdapter<TimeSlot> availableTimeslotsAdapter;
    private MaterialAutoCompleteTextView timeslotDropdown;
    private MaterialTextView fetchingTimeslotsView, noTimeslotsErrorView;
    private LinearLayout mainContentLayout;

    public ReservationTimestampStepFragment() {
        // Required empty public constructor
    }

    public static ReservationTimestampStepFragment newInstance() {
        return new ReservationTimestampStepFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reservation_timestamp_step, container, false);
        fetchingTimeslotsView = view.findViewById(R.id.fetching_timeslots);
        mainContentLayout = view.findViewById(R.id.timeslot_main_content);
        timeslotDropdown = view.findViewById(R.id.reservation_timeslot_dropdown);
        noTimeslotsErrorView = view.findViewById(R.id.error_no_timeslots);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservationViewModel.class);

        availableTimeslotsAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<>()
        );

        timeslotDropdown.setAdapter(availableTimeslotsAdapter);

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            var dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            getTimeslotsForSelectedDate(dateFormat.format(new Date(date)));
        });

        MaterialTextView errorView = view.findViewById(R.id.error_no_timeslot_selected);
        viewModel.getAvailableTimeslotValid().observe(getViewLifecycleOwner(), valid ->
                errorView.setVisibility(valid ? INVISIBLE : VISIBLE));

        timeslotDropdown.setOnItemClickListener(
                (parent, v, position, id) -> {
                    var timeslot = (TimeSlot) parent.getItemAtPosition(position);
                    viewModel.setSelectedAvailableTimeslot(timeslot);
                }
        );
    }

    private void getTimeslotsForSelectedDate(String formattedDate) {
        availableTimeslotsAdapter.clear();
        fetchingTimeslotsView.setVisibility(VISIBLE);
        mainContentLayout.setVisibility(GONE);
        timeslotDropdown.setText("", false);
        reservationService
                .getAvailableTimeSlotsForDate(
                        JwtUtil.getAuthorizationValue(requireContext()),
                        viewModel.getService().getId(),
                        formattedDate
                ).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<AvailableTimeSlotsDTO> call,
                            @NonNull Response<AvailableTimeSlotsDTO> response
                    ) {
                        fetchingTimeslotsView.setVisibility(GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            availableTimeslotsAdapter.addAll(response.body().getAvailableTimeSlots());
                            boolean noTimeslots = availableTimeslotsAdapter.isEmpty();
                            noTimeslotsErrorView.setVisibility(noTimeslots ? VISIBLE: GONE);
                            mainContentLayout.setVisibility(noTimeslots ? GONE: VISIBLE);
                        } else {
                            viewModel.setResponseError(response.errorBody());
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<AvailableTimeSlotsDTO> call, @NonNull Throwable t
                    ) {
                        viewModel.setResponseError(t.getMessage());
                        fetchingTimeslotsView.setVisibility(GONE);
                    }
                });
    }
}