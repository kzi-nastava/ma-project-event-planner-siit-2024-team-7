package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.WorkDayAdapter;
import rs.ac.uns.eventplanner.team7.dto.service.WorkDayDTO;

public class WorkDayDialogFragment extends DialogFragment {
    private final List<WorkDayDTO> workDayList;
    private final WorkDayAdapter adapter;

    public WorkDayDialogFragment(List<WorkDayDTO> workDaysList, WorkDayAdapter adapter) {
        this.workDayList = workDaysList;
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_work_day_add, container, false);

        Spinner spinnerDayOfWeek = view.findViewById(R.id.spinner_day_of_week);
        Spinner spinnerStartTime = view.findViewById(R.id.spinner_start_time);
        Spinner spinnerEndTime = view.findViewById(R.id.spinner_end_time);
        MaterialButton btnSubmit = view.findViewById(R.id.button_submit_work_day);
        MaterialButton btnCancel = view.findViewById(R.id.button_cancel_work_day);

        // Populate the day of the week spinner
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayAdapter);

        // Populate time spinners (24-hour format)
        List<String> timeOptions = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            timeOptions.add(String.format("%02d:00", i));
        }
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, timeOptions);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartTime.setAdapter(timeAdapter);
        spinnerEndTime.setAdapter(timeAdapter);

        // Handle submit button click
        btnSubmit.setOnClickListener(v -> {
            String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString().toUpperCase();
            String startTime = spinnerStartTime.getSelectedItem().toString();
            String endTime = spinnerEndTime.getSelectedItem().toString();

            try {
                if (LocalTime.parse(startTime).isAfter(LocalTime.parse(endTime)) || LocalTime.parse(startTime).equals(LocalTime.parse(endTime)))
                    throw new IllegalArgumentException("Invalid start and end times");
                WorkDayDTO workDay = new WorkDayDTO(DayOfWeek.valueOf(dayOfWeek), startTime, endTime);
                workDayList.add(workDay);
                adapter.notifyDataSetChanged();
                dismiss();
            }
            catch (IllegalArgumentException e) {
                Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                Snackbar.make(view, e.getMessage(), BaseTransientBottomBar.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> {
            dismiss();
        });

        return view;
    }
}
