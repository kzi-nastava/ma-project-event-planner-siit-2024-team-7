package rs.ac.uns.eventplanner.team7.ui.fragments.services;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.interfaces.FilterActionsListener;
import rs.ac.uns.eventplanner.team7.data.services.CategoryService;
import rs.ac.uns.eventplanner.team7.data.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class SPPServicesFilterFragment extends BottomSheetDialogFragment {

    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);

    private TextInputEditText priceInput;
    private AutoCompleteTextView eventTypeDropdown, categoryDropdown;
    private RadioButton availableRadioBtn, unavailableRadioBtn, bothRadioBtn;
    @Getter
    private final Map<String, String> filters;
    private FilterActionsListener listener;

    public SPPServicesFilterFragment() {
        filters = new HashMap<>();
    }

    public SPPServicesFilterFragment(FilterActionsListener listener) {
        this();
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spp_services_filter, container, false);
        priceInput = view.findViewById(R.id.item_price_filter);
        eventTypeDropdown = view.findViewById(R.id.event_type_filter);
        categoryDropdown = view.findViewById(R.id.category_filter);
        availableRadioBtn = view.findViewById(R.id.radio_available);
        unavailableRadioBtn = view.findViewById(R.id.radio_unavailable);
        bothRadioBtn = view.findViewById(R.id.radio_both);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchEventTypeNames();
        fetchCategoryNames();

        MaterialButton closeButton = view.findViewById(R.id.service_filters_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        MaterialButton applyFiltersButton = view.findViewById(R.id.apply_service_filters_button);
        applyFiltersButton.setOnClickListener(v -> {
            applyFilters();
            dismiss();
        });

        MaterialButton resetFiltersButton = view.findViewById(R.id.reset_service_filters_button);
        resetFiltersButton.setOnClickListener(v -> {
            resetFilters();
            dismiss();
        });
    }

    private void resetFilters() {
        if (filters.isEmpty()) {
            dismiss();
            return;
        }
        filters.clear();
        priceInput.setText("");
        eventTypeDropdown.setText("", false);
        categoryDropdown.setText("", false);
        availableRadioBtn.setChecked(false);
        unavailableRadioBtn.setChecked(false);
        bothRadioBtn.setChecked(true);
        listener.onFiltersReset();
    }

    private void applyFilters() {
        filters.clear();
        String price = priceInput.getEditableText().toString();
        if (!price.isEmpty()) filters.put("price", price);

        String eventTypeName = eventTypeDropdown.getEditableText().toString();
        if (!eventTypeName.isEmpty()) filters.put("eventTypeName", eventTypeName);

        String categoryName = categoryDropdown.getEditableText().toString();
        if (!categoryName.isEmpty()) filters.put("categoryName", categoryName);

        if (availableRadioBtn.isChecked()) filters.put("isAvailable", "true");
        else if (unavailableRadioBtn.isChecked()) filters.put("isAvailable", "false");

        listener.onFiltersApplied();
    }

    private void fetchEventTypeNames() {
        eventTypeService.findAllNames().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<String> eventTypes = new ArrayList<>(response.body());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            eventTypes
                    );
                    eventTypeDropdown.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
                Log.e("ERROR", "Request failed", t);
            }
        });
    }

    private void fetchCategoryNames() {
        categoryService.findAllNames().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    List<String> categories = new ArrayList<>(response.body());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            categories
                    );
                    categoryDropdown.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call,
                                  @NonNull Throwable t) {
                Log.e("ERROR", "Request failed", t);
            }
        });
    }
}