package rs.ac.uns.eventplanner.team7.ui.fragments.products;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.pricing.PricingRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.CreateProductRequestDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.model.Category;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.model.enums.CategoryStatus;
import rs.ac.uns.eventplanner.team7.data.model.enums.ItemStatus;
import rs.ac.uns.eventplanner.team7.data.services.CategoryService;
import rs.ac.uns.eventplanner.team7.data.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.data.services.ProductService;
import rs.ac.uns.eventplanner.team7.ui.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.ui.fragments.services.CategorySuggestionDialogFragment;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;


public class CreateProductFragment extends Fragment implements CardClickListener {

    private CreateProductRequestDTO createDTO = new CreateProductRequestDTO();

    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);
    private final ProductService productService = ClientUtils.injectService(ProductService.class);

    private MaterialAutoCompleteTextView categoryDropdown, eventTypesDropdown;

    private final List<EventType> selectedEventTypes;
    private CardRecyclerViewAdapter<EventType> selectedEventTypesAdapter;
    private RecyclerView selectedEventTypesView;
    private List<Category> categories;

    private TextInputEditText productName;

    private TextInputEditText productDescription;

    private MaterialCheckBox productVisible;
    private MaterialCheckBox productAvailable;

    private TextInputEditText productPrice;

    private TextInputEditText productDiscount;

    private MaterialButton createProductButton;
    private MaterialButton suggestCategoryButton;


    public CreateProductFragment() {
        this.selectedEventTypes = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_product, container, false);

        productName = view.findViewById(R.id.product_name);
        productDescription = view.findViewById(R.id.product_description);
        productVisible = view.findViewById(R.id.product_visible);
        productAvailable = view.findViewById(R.id.product_available);
        productPrice = view.findViewById(R.id.product_price);
        productDiscount = view.findViewById(R.id.product_discount);

        categoryDropdown = view.findViewById(R.id.category_dropdown);
        eventTypesDropdown = view.findViewById(R.id.event_type_dropdown);
        selectedEventTypesView = view.findViewById(R.id.recycler_view_selected_event_types);
        createProductButton = view.findViewById(R.id.save_product_button);
        suggestCategoryButton = view.findViewById(R.id.button_suggest_category);

        suggestCategoryButton.setOnClickListener(v -> {
            CategorySuggestionDialogFragment fragment =
                    CategorySuggestionDialogFragment.newInstance(categories, categoryDropdown);
            fragment.show(requireActivity().getSupportFragmentManager(), "SuggestionDialog");
        });

        createProductButton.setOnClickListener(v -> trySubmit());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchCategories();

        selectedEventTypesAdapter = new CardRecyclerViewAdapter<>(requireContext(), selectedEventTypes, this, null);
        selectedEventTypesView.setAdapter(selectedEventTypesAdapter);

        fetchEventTypes();

    }


    private void fetchEventTypes() {
        eventTypeService.findAllActive(AuthUtil.getAuthorizationValue(getContext()))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<EventType>> call,
                                           @NonNull Response<List<EventType>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<EventType> eventTypes = new ArrayList<>(response.body());

                            ArrayAdapter<EventType> adapter = new ArrayAdapter<>(
                                    requireContext(),
                                    android.R.layout.simple_list_item_1,
                                    eventTypes
                            );
                            eventTypesDropdown.setAdapter(adapter);

                            eventTypesDropdown.setOnItemClickListener((parent, view, position, id) ->
                                    handleEventTypeSelection((EventType) parent.getItemAtPosition(position)));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<EventType>> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }

    private void fetchCategories() {
        categoryService.findAllActive(AuthUtil.getAuthorizationValue(requireContext())).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body();
                    categoryDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_list_item_1,
                            response.body()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {}
        });
    }

    private void handleEventTypeSelection(EventType selectedEventType) {
        boolean isUnknownSelected = selectedEventType.getName().equals("UNKNOWN");
        if (isUnknownSelected) {
            selectedEventTypesAdapter.clear();
            selectedEventTypesAdapter.add(selectedEventType);
            return;
        }
        EventType unknownType = null;
        boolean eventTypeIsPresent = false;
        for (EventType eventType : selectedEventTypes) {
            if (eventType.getName().equals("UNKNOWN")) {
                unknownType = eventType;
                break;
            }
            if (eventType.getName().equals(selectedEventType.getName())) {
                eventTypeIsPresent = true;
                break;
            }
        }
        if (unknownType != null) selectedEventTypesAdapter.remove(unknownType);
        if (eventTypeIsPresent) return;
        selectedEventTypesAdapter.add(selectedEventType);
    }

    @Override
    public void onCardClicked(BasicCard entity) {
        selectedEventTypesAdapter.remove((EventType) entity);
    }

    private void trySubmit() {
        createDTO.setName(validateRequiredField(productName, R.string.product_name_is_required));
        createDTO.setDescription(validateRequiredField(productDescription, R.string.product_desc_is_required));
        createDTO.setVisible(productVisible.isChecked());
        createDTO.setAvailable(productAvailable.isChecked());
        if (validateRequiredField(productPrice, R.string.product_price_is_required) != null &&
            validateRequiredField(productDiscount, R.string.product_discount_is_required) != null)
        {
            PricingRequestDTO pricing = new PricingRequestDTO(Double.parseDouble(Objects.requireNonNull(productPrice.getText()).toString()),
                    Double.parseDouble(Objects.requireNonNull(productDiscount.getText()).toString()), LocalDate.now().toString());
            createDTO.setPricing(pricing);
        }

        if (selectedEventTypes.isEmpty()) {
            TextInputLayout eventTypeLayout = requireView().findViewById(R.id.event_types_layout);
            eventTypeLayout.setError("Select atleast 1 event type");
            return;
        }
        if (categoryDropdown.getText().toString().isEmpty()) {
            TextInputLayout categoryLayout = requireView().findViewById(R.id.category_layout);
            categoryLayout.setError("Select a category");
            return;
        }
        CategoryStatus status = null;
        for (Category category : categories) {
            if (category.getName().equals(categoryDropdown.getText().toString())) {
                createDTO.setCategoryId(category.getId());
                status = category.getStatus();
            }
        }
        createDTO.setAppliesTo(new HashSet<>(selectedEventTypes.stream().map(EventType::getName).collect(Collectors.toList())));
        if (status == CategoryStatus.PENDING) {
            createDTO.setRecommended(true);
        }

        createProduct();
    }

    private void createProduct() {
        productService.createProduct(AuthUtil.getAuthorizationValue(requireContext()), createDTO)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Product created successfully", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigate(R.id.navigate_back_from_product_creation);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String validateRequiredField(TextInputEditText input, @StringRes int errorMessage) {
        String text = Objects.requireNonNull(input.getText()).toString().trim();

        if (text.isEmpty()) {
            input.setError(getString(errorMessage));
            return null;
        }
        return text;
    }
}



