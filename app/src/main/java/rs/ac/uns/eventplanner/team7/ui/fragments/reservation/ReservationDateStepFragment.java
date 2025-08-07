package rs.ac.uns.eventplanner.team7.ui.fragments.reservation;

import android.os.Bundle;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import rs.ac.uns.eventplanner.team7.R;


public class ReservationDateStepFragment extends Fragment {

    private ReservationViewModel viewModel;

    public ReservationDateStepFragment() {
        // Required empty public constructor
    }

    public static ReservationDateStepFragment newInstance() {
        return new ReservationDateStepFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservation_date_step, container, false);
        initPicker(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireParentFragment()).get(ReservationViewModel.class);
        // TODO ako se promeni datum next se disabluje i ne pise nikakva greska
        TextInputEditText dateInput = view.findViewById(R.id.reservation_date);

        viewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            Instant instant = Instant.ofEpochMilli(date);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            dateInput.setText(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        });

        MaterialTextView errorView = view.findViewById(R.id.error_invalid_date);
        viewModel.getDateValid().observe(getViewLifecycleOwner(), valid ->
                errorView.setVisibility(valid ? View.INVISIBLE : View.VISIBLE));

    }

    private void initPicker(View view) {
        TextInputLayout datePickerLayout = view.findViewById(R.id.reservation_date_layout);
        datePickerLayout.setEndIconOnClickListener(v -> {
            var selectedDate = viewModel.getSelectedDate().getValue();
            if (selectedDate != null) {
                var datePicker = getDatePicker(selectedDate);
                datePicker.show(getChildFragmentManager(), "datePicker");
            }
        });
    }

    private MaterialDatePicker<Long> getDatePicker(long selectedDate) {
        var calendarConstraints = new CalendarConstraints.Builder()
                .setOpenAt(selectedDate)
                .setStart(viewModel.getMinDate())
                .setEnd(viewModel.getMaxDate())
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setValidator(new CalendarConstraints.DateValidator() {
                    @Override
                    public boolean isValid(long date) {return viewModel.isServiceAvailable(date);}

                    @Override
                    public int describeContents() {return 0;}

                    @Override
                    public void writeToParcel(@NonNull Parcel dest, int flags) {}
                }).build();

        var datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.reservation_datepicker_title)
                .setSelection(selectedDate)
                .setCalendarConstraints(calendarConstraints)
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                viewModel.setSelectedDate(selection);
            }
        });

        return datePicker;
    }
}