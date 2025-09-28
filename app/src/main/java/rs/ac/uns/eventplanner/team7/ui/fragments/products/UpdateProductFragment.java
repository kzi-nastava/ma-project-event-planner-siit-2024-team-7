package rs.ac.uns.eventplanner.team7.ui.fragments.products;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

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
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.UpdateProductRequestDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.model.Category;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.services.CategoryService;
import rs.ac.uns.eventplanner.team7.data.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.data.services.ProductService;
import rs.ac.uns.eventplanner.team7.ui.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.ui.fragments.services.CategorySuggestionDialogFragment;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class UpdateProductFragment extends Fragment implements CardClickListener {

    private static final String PRODUCT = "productDTO";
    private GetProductResponseDTO dto;
    private UpdateProductRequestDTO updateDTO = new UpdateProductRequestDTO();

    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);
    private final ProductService productService = ClientUtils.injectService(ProductService.class);

    private MaterialAutoCompleteTextView categoryDropdown, eventTypesDropdown;
    private final List<EventType> selectedEventTypes = new ArrayList<>();
    private CardRecyclerViewAdapter<EventType> selectedEventTypesAdapter;
    private RecyclerView selectedEventTypesView;
    private List<Category> categories;

    private TextInputEditText productName;
    private TextInputEditText productDescription;
    private MaterialCheckBox productVisible;
    private MaterialCheckBox productAvailable;
    private TextInputEditText productPrice;
    private TextInputEditText productDiscount;
    private MaterialButton deleteProductButton;

    public UpdateProductFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dto = getArguments().getParcelable(PRODUCT, GetProductResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_product, container, false);

        productName = view.findViewById(R.id.product_name);
        productDescription = view.findViewById(R.id.product_description);
        productVisible = view.findViewById(R.id.product_visible);
        productAvailable = view.findViewById(R.id.product_available);
        productPrice = view.findViewById(R.id.product_price);
        productDiscount = view.findViewById(R.id.product_discount);

        TextInputLayout categoryLayout = view.findViewById(R.id.category_layout);
        categoryLayout.setEnabled(false);
        categoryDropdown = view.findViewById(R.id.category_dropdown);
        categoryDropdown.setEnabled(false); // cant change category
        eventTypesDropdown = view.findViewById(R.id.event_type_dropdown);
        selectedEventTypesView = view.findViewById(R.id.recycler_view_selected_event_types);
        checkCurrent(view);
        MaterialButton updateProductButton = view.findViewById(R.id.save_product_button);
        MaterialButton deleteProductButton = view.findViewById(R.id.delete_product_button);
        deleteProductButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        updateProductButton.setOnClickListener(v -> trySubmit());
        return view;
    }

    private void checkCurrent(View view) {
        if (!dto.isCurrent()) {
            productName.setEnabled(false);
            productDescription.setEnabled(false);
            productVisible.setEnabled(false);
            productAvailable.setEnabled(false);
            productPrice.setEnabled(false);
            productDiscount.setEnabled(false);
            TextInputLayout eventTypeLayout = view.findViewById(R.id.event_types_layout);
            eventTypeLayout.setEnabled(false);
            MaterialButton updateProductButton = view.findViewById(R.id.save_product_button);
            updateProductButton.setEnabled(false);
            MaterialButton deleteProductButton = view.findViewById(R.id.delete_product_button);
            deleteProductButton.setEnabled(false);
            MaterialTextView cantUpdate = view.findViewById(R.id.cant_update_product);
            cantUpdate.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedEventTypesAdapter = new CardRecyclerViewAdapter<>(requireContext(), selectedEventTypes, this, null);
        selectedEventTypesView.setAdapter(selectedEventTypesAdapter);

        fetchCategories();
        fetchEventTypes();

        prefillFields();
    }

    private void prefillFields() {
        if (dto == null) return;

        productName.setText(dto.getName());
        productDescription.setText(dto.getDescription());
        productVisible.setChecked(dto.isVisible());
        productAvailable.setChecked(dto.isAvailable());

        if (dto.getPricing() != null) {
            productPrice.setText(String.valueOf(dto.getPricing().getPrice()));
            productDiscount.setText(String.valueOf(dto.getPricing().getDiscount()));
        }

        if (dto.getCategory() != null) {
            categoryDropdown.setText(dto.getCategory().getName(), false);
        }

        if (dto.getAppliesTo() != null) {
            for (EventType type : dto.getAppliesTo()) {
                selectedEventTypesAdapter.add(type);
            }
        }
    }

    private void fetchEventTypes() {
        eventTypeService.findAllActive(AuthUtil.getAuthorizationValue(getContext()))
                .enqueue(new Callback<List<EventType>>() {
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
                        Log.e("ERROR", "Request failed", t);
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
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                Log.e("ERROR", "Request failed", t);
            }
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
        updateDTO.setName(validateRequiredField(productName, R.string.product_name_is_required));
        updateDTO.setDescription(validateRequiredField(productDescription, R.string.product_desc_is_required));
        updateDTO.setVisible(productVisible.isChecked());
        updateDTO.setAvailable(productAvailable.isChecked());

        if (validateRequiredField(productPrice, R.string.product_price_is_required) != null &&
                validateRequiredField(productDiscount, R.string.product_discount_is_required) != null)
        {
            PricingRequestDTO pricing = new PricingRequestDTO(
                    Double.parseDouble(Objects.requireNonNull(productPrice.getText()).toString()),
                    Double.parseDouble(Objects.requireNonNull(productDiscount.getText()).toString()),
                    LocalDate.now().toString()
            );
            updateDTO.setPricing(pricing);
        }

        if (selectedEventTypes.isEmpty()) {
            TextInputLayout eventTypeLayout = requireView().findViewById(R.id.event_types_layout);
            eventTypeLayout.setError("Select at least 1 event type");
            return;
        }
        updateDTO.setImages(new HashSet<>(dto.getImages())); // to not mess up the frontend code
        updateDTO.setAppliesTo(new HashSet<>(selectedEventTypes.stream().map(EventType::getName).collect(Collectors.toList())));

        updateProduct();
    }

    private void updateProduct() {
        productService.updateProduct(AuthUtil.getAuthorizationValue(requireContext()), updateDTO, dto.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Product updated successfully", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigate(R.id.navigate_back_from_product_update);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e("ERROR", "Request failed", t);
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

    private void showDeleteConfirmationDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton("Delete", (d, which) ->
                        deleteProduct())
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (deleteButton != null) {
                deleteButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            }
        });
        dialog.show();
    }

    private void deleteProduct() {
        productService.deleteProduct(AuthUtil.getAuthorizationValue(requireContext()), dto.getId())
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Product deletion successful", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigate(R.id.navigate_back_from_product_update);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e("ERROR", "Request failed", t);
                    }
                });
    }
}
