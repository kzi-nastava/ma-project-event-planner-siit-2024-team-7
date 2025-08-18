package rs.ac.uns.eventplanner.team7.ui.fragments.reservation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalDateTime;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.model.TimeSlot;


public class ReservationTimesStepFragment extends Fragment {

    private ReservationViewModel viewModel;

    private TextInputEditText startTimeInput, endTimeInput;
    private MaterialTextView stepExplanationView, outOfTimeslotErrorView, invalidDurationView;

    public ReservationTimesStepFragment() {
        // Required empty public constructor
    }

    public static ReservationTimesStepFragment newInstance() {
        return new ReservationTimesStepFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation_times_step, container, false);
        stepExplanationView = view.findViewById(R.id.times_step_explanation);
        outOfTimeslotErrorView = view.findViewById(R.id.error_out_of_timeslot);
        invalidDurationView = view.findViewById(R.id.error_invalid_duration);
        startTimeInput = view.findViewById(R.id.reservation_timeslot_start_time);
        endTimeInput = view.findViewById(R.id.reservation_timeslot_end_time);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservationViewModel.class);
        initPickers(view);

        viewModel.getSelectedTimeslot().observe(getViewLifecycleOwner(), slot -> {
            if (slot != null) {
                startTimeInput.setText(slot.getFormattedStartTime());
                endTimeInput.setText(slot.getFormattedEndTime());
            } else {
                startTimeInput.setText("");
                endTimeInput.setText("");
            }
        });


        viewModel.getSelectedTimeslotValidSpecific().observe(getViewLifecycleOwner(), valid -> {
            outOfTimeslotErrorView.setVisibility(valid.first ? View.INVISIBLE : View.VISIBLE);
            invalidDurationView.setVisibility(valid.second ? View.INVISIBLE : View.VISIBLE);
        });

    }

    private void initPickers(View view) {
        TextInputLayout timeStartLayout = view.findViewById(R.id.reservation_timeslot_start_time_layout);
        timeStartLayout.setEndIconOnClickListener(v -> {
            TimeSlot available = viewModel.getSelectedAvailableTimeslot().getValue();
            if (available == null) return;
            TimeSlot selected = viewModel.getSelectedTimeslot().getValue();
            var initTime = selected != null ? selected.getStartTime() : available.getStartTime();
            var startTimePicker = getStartTimePicker(initTime);
            startTimePicker.show(getChildFragmentManager(), "startTimePicker");
        });

        TextInputLayout timeEndLayout = view.findViewById(R.id.reservation_timeslot_end_time_layout);
        var service = viewModel.getService();
        if (service.getMinDurationInMinutes() != service.getMaxDurationInMinutes()) {
            stepExplanationView.setText(getString(R.string.choose_both_times));
            timeEndLayout.setEndIconOnClickListener(v -> {
                TimeSlot available = viewModel.getSelectedAvailableTimeslot().getValue();
                if (available == null) return;
                TimeSlot selected = viewModel.getSelectedTimeslot().getValue();
                var initTime = selected != null ? selected.getEndTime() : available.getEndTime();
                var endTimePicker = getEndTimePicker(initTime);
                endTimePicker.show(getChildFragmentManager(), "endTimePicker");
            });
        } else {
            stepExplanationView.setText(getString(R.string.choose_start_time));
            timeEndLayout.setEndIconVisible(false);
        }
    }

    private MaterialTimePicker getStartTimePicker(LocalDateTime initTime) {
        var startTimePicker = buildTimePicker(R.string.select_reservation_start_time, initTime);
        startTimePicker.addOnPositiveButtonClickListener( v ->
                viewModel.setStartTime(startTimePicker.getHour(), startTimePicker.getMinute()));
        return startTimePicker;
    }

    private MaterialTimePicker getEndTimePicker(LocalDateTime initTime) {
        var endTimePicker = buildTimePicker(R.string.select_reservation_end_time, initTime);
        endTimePicker.addOnPositiveButtonClickListener(v ->
                viewModel.setEndTime(endTimePicker.getHour(), endTimePicker.getMinute())
        );
        return endTimePicker;
    }

    @NonNull
    private static MaterialTimePicker buildTimePicker(@StringRes int resId, LocalDateTime initTime) {
        return new MaterialTimePicker.Builder()
                .setHour(initTime.getHour())
                .setMinute(initTime.getMinute())
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText(resId)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .build();
    }
}