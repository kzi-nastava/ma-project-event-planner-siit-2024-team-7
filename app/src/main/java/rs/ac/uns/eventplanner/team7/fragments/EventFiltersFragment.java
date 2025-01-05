package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.model.interfaces.FilterActionsListener;
import rs.ac.uns.eventplanner.team7.services.EventService;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.DateConverter;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class EventFiltersFragment extends BottomSheetDialogFragment {

    private final EventService eventService = ClientUtils.injectService(EventService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);
    private TextInputEditText eventNameInput, dateRangeInput, maxParticipantsInput,
            descriptionInput;
    private MaterialDatePicker<Pair<Long, Long>> dateRangePicker;
    private long beginDate, endDate;
    private final long minDate,  maxDate;
    private MaterialAutoCompleteTextView eventTypeDropdown, eventLocationDropdown;
    @Getter
    private final Map<String, String> filters;
    private FilterActionsListener listener;

    public EventFiltersFragment() {
        var today = LocalDateTime.now();
        var max = today.plusMonths(6);
        minDate = DateConverter.toLong(today);
        maxDate = DateConverter.toLong(max);
        beginDate = minDate;
        endDate = minDate;
        filters = new HashMap<>();
    }

    public EventFiltersFragment(FilterActionsListener listener) {
        this();
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_filters, container, false);
        dateRangeInput = view.findViewById(R.id.event_date_range_filter);
        eventNameInput = view.findViewById(R.id.event_name_filter);
        maxParticipantsInput = view.findViewById(R.id.event_max_participants_filter);
        descriptionInput = view.findViewById(R.id.event_description_filter);
        eventTypeDropdown = view.findViewById(R.id.event_type_filter);
        eventLocationDropdown = view.findViewById(R.id.event_location_filter);
        initDatePickers(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDropdownAdapters();

        fetchEventTypeNames();
        fetchCities();

        String userCity = JwtUtil.getCity(requireContext());
        filters.put("city", userCity);

        ImageButton closeButton = view.findViewById(R.id.event_filters_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        MaterialButton applyFiltersButton = view.findViewById(R.id.apply_event_filters_button);
        applyFiltersButton.setOnClickListener(v -> {
            applyFilters();
            dismiss();
        });

        MaterialButton resetFiltersButton = view.findViewById(R.id.reset_event_filters_button);
        resetFiltersButton.setOnClickListener(v -> {
            resetFilters();
            dismiss();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setDropdownAdapters();
    }

    private void setDropdownAdapters() {
        eventTypeDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, new ArrayList<String>()));

        eventLocationDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, new ArrayList<String>()));
    }

    private void applyFilters() {
        filters.clear();
        String eventName = eventNameInput.getEditableText().toString();
        if (!eventName.isEmpty()) filters.put("name", eventName);

        String dateRange = dateRangeInput.getEditableText().toString();
        if (!dateRange.isEmpty()) {
            String[] dates = dateRange.split(" / ");
            filters.put("beginDate", dates[0]);
            filters.put("endDate", dates[1]);
        }

        String maxParticipants = maxParticipantsInput.getEditableText().toString();
        if (!maxParticipants.isEmpty()) filters.put("maxParticipants", maxParticipants);

        String description = descriptionInput.getEditableText().toString();
        if (!description.isEmpty()) filters.put("description", description);

        String typeName = eventTypeDropdown.getEditableText().toString();
        if (!typeName.isEmpty()) filters.put("typeName", typeName);

        String city = eventLocationDropdown.getEditableText().toString();
        if (!city.isEmpty()) filters.put("city", city);
        if (filters.isEmpty()) {
            dismiss();
            return;
        }
        listener.onFiltersApplied();
    }

    private void resetFilters() {
        if (filters.isEmpty()) {
            dismiss();
            return;
        }
        filters.clear();
        dateRangeInput.setText("");
        eventNameInput.setText("");
        maxParticipantsInput.setText("");
        descriptionInput.setText("");
        eventTypeDropdown.dismissDropDown();
        eventTypeDropdown.setText("");
        eventLocationDropdown.dismissDropDown();
        eventLocationDropdown.setText("");
        listener.onFiltersReset();
    }

    private void initDatePickers(View view) {
        TextInputLayout dateRangeLayout = view.findViewById(R.id.event_date_range_layout);
        setupDatePickers();
        dateRangeLayout.setEndIconOnClickListener(v -> dateRangePicker.show(getChildFragmentManager(), "dateRangePicker"));
    }

    private void setupDatePickers() {
        var constraintsBuilder = new CalendarConstraints.Builder()
                .setStart(minDate)
                .setEnd(maxDate)
                .setValidator(DateValidatorPointForward.now());
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.select_event_range_message)
                .setSelection(Pair.create(today, today))
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        dateRangePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                beginDate = selection.first != null ? selection.first : minDate;
                endDate = selection.second != null ? selection.second : maxDate;
                formatDateRange();
            }
        });
    }

    private void formatDateRange() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(new Date(beginDate));
        String endDate = sdf.format(new Date(this.endDate));
        dateRangeInput.setText(String.format("%s / %s", startDate, endDate));
    }

    private void fetchEventTypeNames() {
        eventTypeService.findAllNames().enqueue(new Callback<>() {
            /** @noinspection unchecked*/
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var eventTypeNames = response.body();
                    if (eventTypeDropdown.getAdapter() instanceof ArrayAdapter) {
                        var adapter = (ArrayAdapter<String>) eventTypeDropdown.getAdapter();
                        adapter.addAll(eventTypeNames);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {}
        });
    }

    private void fetchCities() {
        eventService.findAllCities().enqueue(new Callback<>() {
            /** @noinspection unchecked*/
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var locations = response.body();
                    if (eventLocationDropdown.getAdapter() instanceof ArrayAdapter) {
                        var adapter = (ArrayAdapter<String>) eventLocationDropdown.getAdapter();
                        adapter.add("All");
                        adapter.addAll(locations);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {}
        });
    }
}
