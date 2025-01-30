package rs.ac.uns.eventplanner.team7.fragments.services;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.dto.service.WorkDayDTO;
import rs.ac.uns.eventplanner.team7.fragments.MaterialDialogFragment;

public class WorkDayDialogFragment extends MaterialDialogFragment {

    private List<WorkDayDTO> currentWorkDays;
    private CardRecyclerViewAdapter<WorkDayDTO> adapter;
    private MaterialAutoCompleteTextView dayOfWeekDropdown, startTimeDropdown, endTimeDropdown;
    private MaterialButton btnSubmit, btnCancel;
    private final ArrayList<String> timeOptions;
    String selectedDay, selectedStartTime, selectedEndTime;

    public WorkDayDialogFragment() {
        // Required empty public constructor
        timeOptions = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            timeOptions.add(String.format("%02d:00", i));
        }
        selectedDay = selectedStartTime = selectedEndTime = "";
    }

    public static WorkDayDialogFragment newInstance(List<WorkDayDTO> currentWorkDays,
                                                    CardRecyclerViewAdapter<WorkDayDTO> adapter) {
        WorkDayDialogFragment fragment = new WorkDayDialogFragment();
        fragment.adapter = adapter;
        fragment.currentWorkDays = currentWorkDays;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_work_day_add, container, false);

        dayOfWeekDropdown = view.findViewById(R.id.dropdown_day_of_week);
        startTimeDropdown = view.findViewById(R.id.dropdown_start_time);
        endTimeDropdown = view.findViewById(R.id.dropdown_end_time);
        btnSubmit = view.findViewById(R.id.button_submit_work_day);
        btnCancel = view.findViewById(R.id.button_cancel_work_day);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Populate the day of the week spinner
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.days_of_week, android.R.layout.simple_list_item_1);
        dayOfWeekDropdown.setAdapter(dayAdapter);

        startTimeDropdown.setAdapter(new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_1, timeOptions));
        endTimeDropdown.setAdapter(new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_list_item_1, timeOptions));

        dayOfWeekDropdown.setOnItemClickListener((parent, view1, position, id) -> selectedDay = (String) parent.getItemAtPosition(position));
        startTimeDropdown.setOnItemClickListener((parent, view1, position, id) -> selectedStartTime = (String) parent.getItemAtPosition(position));
        endTimeDropdown.setOnItemClickListener((parent, view1, position, id) -> selectedEndTime = (String) parent.getItemAtPosition(position));

        // Handle submit button click
        btnSubmit.setOnClickListener(v -> submit());

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void showToast(String text) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void submit() {
        if (selectedDay.isEmpty() || selectedStartTime.isEmpty() || selectedEndTime.isEmpty()) {
            showToast("Please fill out all the fields");
            return;
        }

        try {
            if (LocalTime.parse(selectedStartTime).isAfter(LocalTime.parse(selectedEndTime)) || LocalTime.parse(selectedStartTime).equals(LocalTime.parse(selectedEndTime)))
                throw new IllegalArgumentException("Invalid start and end times");
            DayOfWeek day = DayOfWeek.valueOf(selectedDay.toUpperCase());
            WorkDayDTO existingWorkDay = null;
            // existing work day is replaced with new one, if present
            for (int i = 0; i < currentWorkDays.size(); i++) {
                WorkDayDTO currentWorkDay = currentWorkDays.get(i);
                if (currentWorkDay.getDay() == day) {
                    showToast("Replacing existing work day");
                    existingWorkDay = currentWorkDay;
                    break;
                }
            }

            WorkDayDTO workDay = new WorkDayDTO(day, selectedStartTime, selectedEndTime);
            if (existingWorkDay != null) {
                adapter.remove(existingWorkDay);
            }
            adapter.add(workDay);

            dismiss();
        }
        catch (IllegalArgumentException e) {
            Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
            showToast(e.getMessage());
        }
    }
}
