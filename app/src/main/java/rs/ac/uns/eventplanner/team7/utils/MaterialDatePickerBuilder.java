package rs.ac.uns.eventplanner.team7.utils;

import androidx.annotation.NonNull;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

public class MaterialDatePickerBuilder {
    @NonNull
    public static MaterialDatePicker<Long> build(String title, CalendarConstraints.Builder constraintsBuilder) {
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        return MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(today)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();
    }
}
