package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import rs.ac.uns.eventplanner.team7.R;

public class ItemFiltersFragment extends BottomSheetDialogFragment {

    public ItemFiltersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_filters, container, false);

        TextInputEditText startDateInput = view.findViewById(R.id.available_start_date_input);
        TextInputEditText endDateInput = view.findViewById(R.id.available_end_date_input);

        TextInputLayout beginDateLayout = view.findViewById(R.id.available_start_date_layout);
        TextInputLayout endDateLayout = view.findViewById(R.id.available_end_date_layout);

        initListener(beginDateLayout, startDateInput);
        initListener(endDateLayout, endDateInput);

        return view;
    }

    private void initListener(TextInputLayout dateInputLayout, TextInputEditText dateInput) {

        long today = MaterialDatePicker.todayInUtcMilliseconds();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setStart(today)
                .setValidator(DateValidatorPointForward.from(today));


        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(today)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        dateInputLayout.setEndIconOnClickListener(v -> {
            datePicker.show(getChildFragmentManager(), datePicker.getTag());
        });

        dateInput.setOnClickListener(v -> {
            datePicker.show(getChildFragmentManager(), datePicker.getTag());
        });

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            String selectedDate = sdf.format(calendar.getTime());
            dateInput.setText(selectedDate);
        });
    }
}