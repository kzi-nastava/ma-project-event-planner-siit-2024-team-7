package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;

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
import java.util.Arrays;
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
import rs.ac.uns.eventplanner.team7.model.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.services.ProductService;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.DateConverter;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class ItemFiltersFragment extends BottomSheetDialogFragment {
    private final ProductService productService = ClientUtils.injectService(ProductService.class);
    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    TextInputEditText itemNameInput, descriptionInput, itemPriceInput, serviceDateRangeInput,
            serviceSpecificsInput, serviceMinDurationInput, serviceMaxDurationInput,
            serviceReservationDeadlineInput, serviceCancellationDeadlineInput;
    private MaterialDatePicker<Pair<Long, Long>> dateRangePicker;
    private long availableFromDate, availableUntilDate;
    private final long minDate, maxDate;
    private MaterialAutoCompleteTextView shownItemTypeDropdown, productAvailabilityDropdown,
            serviceAvailabilityDropdown, itemCategoryDropdown, itemLocationDropdown;

    @Getter
    private String shownItemType;
    private String selectedProductAvailability, selectedServiceAvailability, selectedCategory, selectedCity;
    private final List<String> shownTypes, itemAvailability, categoryNames, cities;
    @Getter
    private final Map<String, String> filters;
    private FilterActionsListener listener;
    private LinearLayout serviceFieldsView, productAvailabilityView;

    private ItemFiltersFragment(SearchActionsListener listener) {
        this();
        this.listener = listener;
    }

    public ItemFiltersFragment() {
        var today = LocalDateTime.now();
        var max = today.plusMonths(6);
        minDate = DateConverter.toLong(today);
        maxDate = DateConverter.toLong(max);
        availableFromDate = minDate;
        availableUntilDate = minDate;
        selectedProductAvailability = selectedServiceAvailability = selectedCategory = selectedCity = "";
        filters = new HashMap<>();
        shownTypes = Arrays.asList("Products", "Services");
        itemAvailability = Arrays.asList("Available", "Unavailable", "Both");
        shownItemType = shownTypes.get(0);
        categoryNames = new ArrayList<>();
        cities = new ArrayList<>();
    }

    public static ItemFiltersFragment newInstance(SearchActionsListener listener) {
        return new ItemFiltersFragment(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_filters, container, false);
        itemNameInput = view.findViewById(R.id.item_name_filter);
        descriptionInput = view.findViewById(R.id.item_description_filter);
        itemPriceInput = view.findViewById(R.id.item_price_filter);
        serviceAvailabilityDropdown = view.findViewById(R.id.service_availability_filter);
        serviceDateRangeInput = view.findViewById(R.id.service_date_range_filter);
        serviceSpecificsInput = view.findViewById(R.id.service_specifics_filter);
        serviceMinDurationInput = view.findViewById(R.id.service_min_duration_filter);
        serviceMaxDurationInput = view.findViewById(R.id.service_max_duration_filter);
        serviceReservationDeadlineInput = view.findViewById(R.id.service_reservation_deadline_filter);
        serviceCancellationDeadlineInput = view.findViewById(R.id.service_cancellation_deadline_filter);
        shownItemTypeDropdown = view.findViewById(R.id.shown_item_type);
        productAvailabilityDropdown = view.findViewById(R.id.product_availability_filter);
        itemCategoryDropdown = view.findViewById(R.id.item_category_filter);
        itemLocationDropdown = view.findViewById(R.id.item_location_filter);
        serviceFieldsView = view.findViewById(R.id.service_filters_layout);
        productAvailabilityView = view.findViewById(R.id.product_availability_filter_layout);
        initDatePickers(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userCity = JwtUtil.getCity(requireContext());
        filters.put("city", userCity);

        setDropdownAdapters();

        fetchCategoryNames();
        if (cities.isEmpty() && shownItemType.equals(shownTypes.get(0)))
            fetchCities(productService.findAllCities());

        setupShownItemListener();

        ImageButton closeButton = view.findViewById(R.id.item_filters_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        MaterialButton applyFiltersButton = view.findViewById(R.id.apply_item_filters_button);
        applyFiltersButton.setOnClickListener(v -> {
            applyFilters();
            dismiss();
        });

        MaterialButton resetFiltersButton = view.findViewById(R.id.reset_item_filters_button);
        resetFiltersButton.setOnClickListener(v -> {
            resetFilters();
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        shownItemTypeDropdown.setText(shownItemType, false);
        if (shownItemType.equals(shownTypes.get(1))) switchViewVisibility();
        if (!selectedProductAvailability.isEmpty())
            productAvailabilityDropdown.setText(selectedProductAvailability, false);
        if (!selectedServiceAvailability.isEmpty())
            serviceAvailabilityDropdown.setText(selectedServiceAvailability, false);
        if (!selectedCategory.isEmpty()) itemCategoryDropdown.setText(selectedCategory, false);
        if (!selectedCity.isEmpty()) itemLocationDropdown.setText(selectedCity, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        shownItemTypeDropdown.setText("", false);
        selectedProductAvailability = productAvailabilityDropdown.getEditableText().toString();
        if (!selectedProductAvailability.isEmpty()) productAvailabilityDropdown.setText("", false);
        selectedServiceAvailability = serviceAvailabilityDropdown.getEditableText().toString();
        if (!selectedServiceAvailability.isEmpty()) serviceAvailabilityDropdown.setText("", false);
        selectedCategory = itemCategoryDropdown.getEditableText().toString();
        if (!selectedCategory.isEmpty()) itemCategoryDropdown.setText("", false);
        selectedCity = itemLocationDropdown.getEditableText().toString();
        if (!selectedCity.isEmpty()) itemLocationDropdown.setText("", false);
    }

    private void setDropdownAdapters() {
        shownItemTypeDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, shownTypes));

        productAvailabilityDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, itemAvailability));

        serviceAvailabilityDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, itemAvailability));

        itemCategoryDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, categoryNames));

        itemLocationDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, cities));
    }

    private void setupShownItemListener() {
        shownItemTypeDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = shownTypes.get(position);
            if (position == 0) fetchCities(productService.findAllCities());
            else if (position == 1) fetchCities(serviceService.findAllCities());
            if (selectedType.equals(shownItemType)) return;
            shownItemType = selectedType;
            switchViewVisibility();
        });
    }

    private void switchViewVisibility() {
        int serviceVisibility = serviceFieldsView.getVisibility();
        serviceFieldsView.setVisibility(productAvailabilityView.getVisibility());
        productAvailabilityView.setVisibility(serviceVisibility);
    }

    private void applyFilters() {
        filters.clear();
        String itemName = itemNameInput.getEditableText().toString();
        if (!itemName.isEmpty()) filters.put("name", itemName);

        String description = descriptionInput.getEditableText().toString();
        if (!description.isEmpty()) filters.put("description", description);

        String itemPrice = itemPriceInput.getEditableText().toString();
        if (!itemPrice.isEmpty()) filters.put("price", itemPrice);

        String categoryName = itemCategoryDropdown.getEditableText().toString();
        if (!categoryName.isEmpty()) filters.put("categoryName", categoryName);

        String city = itemLocationDropdown.getEditableText().toString();
        if (!city.isEmpty()) filters.put("city", city);

        switch (shownItemType.toLowerCase()) {
            case "products":
                applyAvailability(productAvailabilityDropdown);
                break;
            case "services":
                applyAvailability(serviceAvailabilityDropdown);
                applyServiceSpecificFilters();
                break;
        }
        listener.onFiltersApplied();
    }

    private void applyAvailability(MaterialAutoCompleteTextView dropdown) {
        String availability = dropdown.getEditableText().toString();
        if (!availability.isEmpty()) {
            switch (availability.toLowerCase()) {
                case "available":
                    filters.put("isAvailable", String.valueOf(true));
                    break;
                case "unavailable":
                    filters.put("isAvailable", String.valueOf(false));
                    break;
            }
        }
    }

    private void applyServiceSpecificFilters() {
        String dateRange = serviceDateRangeInput.getEditableText().toString();
        if (!dateRange.isEmpty()) {
            String[] dates = dateRange.split(" / ");
            filters.put("availableFrom", dates[0]);
            filters.put("availableUntil", dates[1]);
        }

        String specifics = serviceSpecificsInput.getEditableText().toString();
        if (!specifics.isEmpty()) filters.put("specifics", specifics);

        String minDuration = serviceMinDurationInput.getEditableText().toString();
        if (!minDuration.isEmpty()) filters.put("minDuration", minDuration);

        String maxDuration = serviceMaxDurationInput.getEditableText().toString();
        if (!maxDuration.isEmpty()) filters.put("maxDuration", maxDuration);

        String reservationDeadline = serviceReservationDeadlineInput.getEditableText().toString();
        if (!reservationDeadline.isEmpty()) filters.put("reservationDeadline", reservationDeadline);

        String cancellationDeadline =  serviceCancellationDeadlineInput.getEditableText().toString();
        if (!cancellationDeadline.isEmpty()) filters.put("cancellationDeadline", cancellationDeadline);
    }

    private void resetFilters() {
        filters.clear();
        shownItemType = shownTypes.get(0);
        shownItemTypeDropdown.setText(shownItemType, false);
        itemNameInput.setText("");
        productAvailabilityDropdown.setText("", false);
        serviceDateRangeInput.setText("");
        serviceSpecificsInput.setText("");
        serviceMinDurationInput.setText("");
        serviceMaxDurationInput.setText("");
        serviceReservationDeadlineInput.setText("");
        serviceCancellationDeadlineInput.setText("");
        descriptionInput.setText("");
        itemPriceInput.setText("");
        itemCategoryDropdown.setText("", false);
        itemLocationDropdown.setText("", false);
        listener.onFiltersReset();
    }

    private void initDatePickers(View view) {
        TextInputLayout dateRangeLayout = view.findViewById(R.id.service_date_range_layout);
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
                availableFromDate = selection.first != null ? selection.first : minDate;
                availableUntilDate = selection.second != null ? selection.second : maxDate;
                formatDateRange();
            }
        });
    }

    private void formatDateRange() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDate = sdf.format(new Date(availableFromDate));
        String endDate = sdf.format(new Date(this.availableUntilDate));
        serviceDateRangeInput.setText(String.format("%s / %s", startDate, endDate));
    }

    private void fetchCategoryNames() {
        categoryService.findAllNames().enqueue(new Callback<>() {
            /** @noinspection unchecked*/
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    var categoryNames = response.body();
                    if (itemCategoryDropdown.getAdapter() instanceof ArrayAdapter) {
                        var adapter = (ArrayAdapter<String>) itemCategoryDropdown.getAdapter();
                        adapter.addAll(categoryNames);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {}
        });
    }

    private void fetchCities(Call<List<String>> serviceCall) {
        serviceCall.enqueue(new Callback<>() {
            /** @noinspection unchecked*/
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var locations = response.body();
                    if (itemLocationDropdown.getAdapter() instanceof ArrayAdapter) {
                        var adapter = (ArrayAdapter<String>) itemLocationDropdown.getAdapter();
                        if (!adapter.isEmpty()) {
                            itemLocationDropdown.setText("", false);
                            adapter.clear();
                        }
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