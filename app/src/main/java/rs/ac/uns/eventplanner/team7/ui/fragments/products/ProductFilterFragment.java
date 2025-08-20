package rs.ac.uns.eventplanner.team7.ui.fragments.products;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import java.util.stream.Collectors;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.interfaces.FilterActionsListener;
import rs.ac.uns.eventplanner.team7.data.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.services.CategoryService;
import rs.ac.uns.eventplanner.team7.data.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.data.services.ProductService;
import rs.ac.uns.eventplanner.team7.data.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.DateConverter;


public class ProductFilterFragment extends BottomSheetDialogFragment {

    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);

    TextInputEditText itemNameInput, descriptionInput, itemPriceInput;
    MaterialAutoCompleteTextView itemCategoryDropdown, eventTypeDropdown, availabilityDropdown;

    @Getter
    private final Map<String, String> filters = new HashMap<>();
    private FilterActionsListener listener;

    private final List<String> categoryNames = new ArrayList<>();
    private List<String> eventTypes = new ArrayList<>();

    ProductFilterFragment(FilterActionsListener listener) {
        this();
        this.listener = listener;
    }

    public ProductFilterFragment() {}

    public static ProductFilterFragment newInstance(FilterActionsListener listener) {
        return new ProductFilterFragment(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_filter, container, false);

        itemNameInput = view.findViewById(R.id.item_name_filter);
        descriptionInput = view.findViewById(R.id.item_description_filter);
        itemPriceInput = view.findViewById(R.id.item_price_filter);
        itemCategoryDropdown = view.findViewById(R.id.item_category_filter);
        eventTypeDropdown = view.findViewById(R.id.event_type_filter);
        availabilityDropdown = view.findViewById(R.id.product_available_dropdown);


        setDropdownAdapters();
        fetchCategoryNames();
        fetchEventTypes();

        MaterialButton closeButton = view.findViewById(R.id.item_filters_close_button);
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

        return view;
    }

    private void setDropdownAdapters() {


        itemCategoryDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, categoryNames));

        List<String> availabilityOptions = Arrays.asList("Both", "Available", "Unavailable");
        availabilityDropdown.setAdapter(new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                availabilityOptions
        ));
    }

    private void fetchCategoryNames() {
        categoryService.findAllNames().enqueue(new Callback<>() {
            /** @noinspection unchecked*/
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    var categories = response.body();
                    if (itemCategoryDropdown.getAdapter() instanceof ArrayAdapter) {
                        var adapter = (ArrayAdapter<String>) itemCategoryDropdown.getAdapter();
                        adapter.addAll(categories);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {}
        });
    }

    private void fetchEventTypes() {
        eventTypeService.getAll(AuthUtil.getAuthorizationValue(requireContext())).enqueue(
                new Callback<List<EventType>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<EventType>> call, @NonNull Response<List<EventType>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            eventTypes = response.body().stream().map((EventType::getName)).collect(Collectors.toList());
                            eventTypeDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                                    android.R.layout.simple_list_item_1, eventTypes));



                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<EventType>> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void applyFilters() {
        filters.clear();

        String itemName = itemNameInput.getEditableText().toString();
        if (!itemName.isEmpty()) filters.put("name", itemName);

        String description = descriptionInput.getEditableText().toString();
        if (!description.isEmpty()) filters.put("description", description);

        String itemPrice = itemPriceInput.getEditableText().toString();
        if (!itemPrice.isEmpty()) filters.put("price", itemPrice);

        String category = itemCategoryDropdown.getEditableText().toString();
        if (!category.isEmpty()) filters.put("categoryName", category);

        String eventType = eventTypeDropdown.getEditableText().toString();
        if (!eventType.isEmpty()) filters.put("eventType", eventType);

        String availability = availabilityDropdown.getEditableText().toString();
        if (!availability.isEmpty()) {
            switch (availability) {
                case "Available":
                    filters.put("isAvailable", "true");
                    break;
                case "Unavailable":
                    filters.put("isAvailable", "false");
                    break;
                case "Both": // don't add anything, null = no filter
                default:
                    break;
            }
        }

        listener.onFiltersApplied();
    }

    private void resetFilters() {
        filters.clear();
        itemNameInput.setText("");
        descriptionInput.setText("");
        itemPriceInput.setText("");
        itemCategoryDropdown.setText("", false);
        eventTypeDropdown.setText("", false);
        availabilityDropdown.setText("", false);
        listener.onFiltersReset();
    }
}