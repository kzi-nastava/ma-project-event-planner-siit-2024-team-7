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
import rs.ac.uns.eventplanner.team7.utils.MaterialDatePickerBuilder;

public class EventFiltersFragment extends BottomSheetDialogFragment {

    private TextInputEditText startDateInput;
    private TextInputEditText endDateInput;
    private MaterialDatePicker<Long> beginDatePicker;
    private MaterialDatePicker<Long> endDatePicker;
    private long beginDate;
    private long endDate;


    public EventFiltersFragment() {
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        beginDate = today;
        endDate = today;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_filters, container, false);
        initDatePickers(view);
        return view;
    }

    private void initDatePickers(View view) {
        startDateInput = view.findViewById(R.id.event_begin_date_input);
        endDateInput = view.findViewById(R.id.event_end_date_input);

        TextInputLayout beginDateLayout = view.findViewById(R.id.event_begin_date_layout);
        TextInputLayout endDateLayout = view.findViewById(R.id.event_end_date_layout);

        setupDatePickers();

        beginDateLayout.setEndIconOnClickListener(v -> beginDatePicker.show(getChildFragmentManager(), "beginDate"));
        endDateLayout.setEndIconOnClickListener(v -> endDatePicker.show(getChildFragmentManager(), "endDate"));

        startDateInput.setOnClickListener(v -> beginDatePicker.show(getChildFragmentManager(), "beginDate"));
        endDateInput.setOnClickListener(v -> endDatePicker.show(getChildFragmentManager(), "endDate"));
    }

    private void setupDatePickers() {

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setStart(beginDate)
                .setValidator(DateValidatorPointForward.from(beginDate));

        beginDatePicker = MaterialDatePickerBuilder.build("Select Begin Date", constraintsBuilder);
        endDatePicker = MaterialDatePickerBuilder.build("Select End Date", constraintsBuilder);

        beginDatePicker.addOnPositiveButtonClickListener(selection -> {
            beginDate = selection;
            updateDateField(startDateInput, beginDate);

            if (endDate < beginDate) {
                endDate = beginDate;
                updateDateField(endDateInput, endDate);
            }
        });

        endDatePicker.addOnPositiveButtonClickListener(selection -> {
            endDate = selection;
            updateDateField(endDateInput, endDate);

            if (endDate < beginDate) {
                beginDate = endDate;
                updateDateField(startDateInput, beginDate);
            }
        });
    }

    private void updateDateField(TextInputEditText dateInput, long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        String selectedDate = sdf.format(calendar.getTime());
        dateInput.setText(selectedDate);
    }
}