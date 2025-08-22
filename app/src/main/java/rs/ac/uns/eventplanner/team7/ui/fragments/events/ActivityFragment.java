package rs.ac.uns.eventplanner.team7.ui.fragments.events;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.model.Activity;


public class ActivityFragment extends Fragment {

    private static final String ARG_INDEX = "index";
    private static final String ACTIVITY = "activity";

    private TextView activityTitle;

    private TextInputEditText nameInput, descInput, startTimeInput, endTimeInput, locationInput;
    private TextInputLayout nameLayout, descLayout, startTimeLayout, endTimeLayout, locationLayout;

    private OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(ActivityFragment fragment);
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        activityTitle = view.findViewById(R.id.activity_title);

        nameInput = view.findViewById(R.id.activity_name_input);
        descInput = view.findViewById(R.id.activity_desc_input);
        startTimeInput = view.findViewById(R.id.activity_start_time_input);
        endTimeInput = view.findViewById(R.id.activity_end_time_input);
        locationInput = view.findViewById(R.id.activity_location_input);

        nameLayout = view.findViewById(R.id.activity_name_layout);
        descLayout = view.findViewById(R.id.activity_desc_layout);
        startTimeLayout = view.findViewById(R.id.activity_start_time_layout);
        endTimeLayout = view.findViewById(R.id.activity_end_time_layout);
        locationLayout = view.findViewById(R.id.activity_location_layout);

        initTimePickers();

        if (getArguments() != null) {
            int index = getArguments().getInt(ARG_INDEX, -1);
            Activity act = getArguments().getParcelable(ACTIVITY, Activity.class);
            if (act != null) {
                // Prefill fields
                nameInput.setText(act.getName());
                descInput.setText(act.getDescription());
                startTimeInput.setText(act.getStartTime().toString()); // format if needed
                endTimeInput.setText(act.getEndTime().toString());     // format if needed
                locationInput.setText(act.getLocation());
            }
            activityTitle.setText("Activity " + index + ":");
        }

        ImageButton deleteButton = view.findViewById(R.id.delete_activity_button);
        deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(this);
            }
        });

        return view;
    }

    public void updateIndex(int newIndex) {
        if (activityTitle != null) {
            activityTitle.setText("Activity " + newIndex + ":");
        }
    }

    private void initTimePickers() {
        startTimeInput.setOnClickListener(v -> showTimePicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showTimePicker(endTimeInput));
    }

    private void showTimePicker(TextInputEditText input) {
        input.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTitleText("Select time")
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .build();

            timePicker.show(getParentFragmentManager(), "TIME_PICKER");

            timePicker.addOnPositiveButtonClickListener(tp -> {
                String formattedTime = String.format(Locale.getDefault(), "%02d:%02d",
                        timePicker.getHour(), timePicker.getMinute());
                input.setText(formattedTime);
            });
        });
    }

    private String validateField(TextInputEditText input, TextInputLayout layout, String error) {
        String text = Objects.requireNonNull(input.getText()).toString().trim();
        if (text.isEmpty()) {
            layout.setError(error);
            return null;
        }
        layout.setError(null);
        return text;
    }


    public Activity extractActivity() {
        String name = validateField(nameInput, nameLayout, "Activity name is required");
        String desc = validateField(descInput, descLayout, "Description is required");
        String start = validateField(startTimeInput, startTimeLayout, "Start time is required");
        String end = validateField(endTimeInput, endTimeLayout, "End time is required");
        String location = validateField(locationInput, locationLayout, "Location is required");

        if (start != null && end != null) {
            LocalTime s = LocalTime.parse(start);
            LocalTime e = LocalTime.parse(end);

            if (s.isAfter(e)) {
                return null;
            }
        }

        if (name == null || desc == null || start == null || end == null || location == null) {
            return null; // Validation failed
        }

        Activity activity = new Activity();
        activity.setName(name);
        activity.setDescription(desc);
        activity.setStartTime(LocalTime.parse(start)); // or parse to LocalTime if needed
        activity.setEndTime(LocalTime.parse(end));
        activity.setLocation(location);

        return activity;
    }
}
