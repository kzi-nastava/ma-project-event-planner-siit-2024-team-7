package rs.ac.uns.eventplanner.team7.fragments.services;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.adapters.ImageListAdapter;
import rs.ac.uns.eventplanner.team7.dto.pricing.PricingRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.service.CreateServiceRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.service.CreateServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.DeleteServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.UpdateServiceRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.service.UpdateServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.WorkDayDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.EventType;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class ServiceManagementFragment extends Fragment implements CardClickListener {
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private GetServiceResponseDTO serviceDTO;

    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);

    private CardRecyclerViewAdapter<WorkDayDTO> workDayAdapter;
    private CardRecyclerViewAdapter<EventType> selectedEventTypesAdapter;
    private ImageListAdapter imageListAdapter;

    private final List<WorkDayDTO> workDays;
    private final List<String> images;
    private final List<EventType> selectedEventTypes;
    private List<Category> categories;

    private TextInputEditText nameInput, descriptionInput, priceInput, discountInput, specificsInput,
            reservationInput, cancellationInput, minDurationInput, maxDurationInput;
    private AutoCompleteTextView categoryDropdown, eventTypesDropdown;
    private MaterialCheckBox visibleCheckBox, availableCheckBox;
    private MaterialButton selectImagesBtn, addWorkDayBtn, suggestCategoryBtn;
    private RecyclerView imagesView, workDaysView, selectedEventTypesView;

    public ServiceManagementFragment() {
        this.workDays = new ArrayList<>();
        this.images = new ArrayList<>();
        this.selectedEventTypes = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            serviceDTO = getArguments().getParcelable("serviceDTO", GetServiceResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_management, container, false);
        nameInput = view.findViewById(R.id.service_name);
        descriptionInput = view.findViewById(R.id.service_description);
        priceInput = view.findViewById(R.id.service_price);
        discountInput = view.findViewById(R.id.service_discount);
        specificsInput = view.findViewById(R.id.service_specifics);
        reservationInput = view.findViewById(R.id.reservation_deadline);
        cancellationInput = view.findViewById(R.id.cancellation_deadline);
        minDurationInput = view.findViewById(R.id.min_duration);
        maxDurationInput = view.findViewById(R.id.max_duration);
        categoryDropdown = view.findViewById(R.id.categories_dropdown);
        eventTypesDropdown = view.findViewById(R.id.event_type_dropdown);
        visibleCheckBox = view.findViewById(R.id.service_visible);
        availableCheckBox = view.findViewById(R.id.service_available);
        selectImagesBtn = view.findViewById(R.id.button_select_images);
        addWorkDayBtn = view.findViewById(R.id.btn_open_work_day_dialog);
        suggestCategoryBtn = view.findViewById(R.id.button_suggest_category);
        imagesView = view.findViewById(R.id.recycler_view_images);
        workDaysView = view.findViewById(R.id.recycler_view_work_days);
        selectedEventTypesView = view.findViewById(R.id.recycler_view_selected_event_types);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchCategories();
        suggestCategoryBtn.setOnClickListener(v -> {
            CategorySuggestionDialogFragment fragment =
                    CategorySuggestionDialogFragment.newInstance(categories, categoryDropdown);
            fragment.show(requireActivity().getSupportFragmentManager(), "SuggestionDialog");
        });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                String imageName = getImageName(imageUri);
                if (imageName != null) {
                    imageListAdapter.addImage(imageName);
                }
            }
        });
        imageListAdapter = new ImageListAdapter(getContext(), images);
        imagesView.setAdapter(imageListAdapter);

        selectImagesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        workDayAdapter = new CardRecyclerViewAdapter<>(requireContext(), workDays, this, null);
        workDaysView.setAdapter(workDayAdapter);
        addWorkDayBtn.setOnClickListener(v -> {
            AddWorkDayDialogFragment fragment = AddWorkDayDialogFragment.newInstance(workDays, workDayAdapter);
            fragment.show(requireActivity().getSupportFragmentManager(), "WorkDayDialog");
        });

        selectedEventTypesAdapter = new CardRecyclerViewAdapter<>(requireContext(), selectedEventTypes, this, null);
        selectedEventTypesView.setAdapter(selectedEventTypesAdapter);

        fetchEventTypes();

        MaterialTextView title = view.findViewById(R.id.welcomeMessage);
        if (serviceDTO == null) {
            title.setText(R.string.service_creation);
            view.findViewById(R.id.delete_service_button).setVisibility(View.GONE);
        }
        else {
            title.setText(R.string.service_update);
            view.findViewById(R.id.categories_dropdown).setEnabled(false);
            view.findViewById(R.id.categories_dropdown_layout).setEnabled(false);
            view.findViewById(R.id.cant_find_category_text).setVisibility(View.GONE);
            suggestCategoryBtn.setVisibility(View.GONE);
            setServiceFields();
        }

        MaterialButton saveButton = view.findViewById(R.id.save_service_button);
        saveButton.setOnClickListener(v -> {
            if (serviceDTO == null) {
                CreateServiceRequestDTO dto;
                try {
                    dto = createRequestDTO(view);
                } catch (IllegalArgumentException e) {
                    Snackbar snackbar = Snackbar.make(view, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return;
                }

                serviceService.createService(JwtUtil.getAuthorizationValue(requireContext()), dto)
                        .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateServiceResponseDTO> call, @NonNull Response<CreateServiceResponseDTO> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            returnToBaseFragment("Service created successfully!");
                        } else {
                            try {
                                // Show error message
                                String errorBody = Objects.requireNonNull(response.errorBody()).string();
                                JSONObject jsonObject = new JSONObject(errorBody);
                                String message = jsonObject.getString("message");
                                MaterialTextView errorMsg = requireView().findViewById(R.id.error_msg);
                                errorMsg.setText(message);
                                errorMsg.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreateServiceResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
            }
            else {
                UpdateServiceRequestDTO dto;
                try {
                    dto = updateRequestDTO(view);
                } catch (IllegalArgumentException e) {
                    Snackbar snackbar = Snackbar.make(view, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return;
                }
                serviceService.updateService(JwtUtil.getAuthorizationValue(requireContext()),
                        serviceDTO.getId(), dto).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateServiceResponseDTO> call, @NonNull Response<UpdateServiceResponseDTO> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            returnToBaseFragment("Service updated successfully!");
                        } else {
                            try {
                                // Show error message
                                String errorBody = Objects.requireNonNull(response.errorBody()).string();
                                JSONObject jsonObject = new JSONObject(errorBody);
                                String message = jsonObject.getString("message");
                                MaterialTextView errorMsg = requireView().findViewById(R.id.error_msg);
                                errorMsg.setText(message);
                                errorMsg.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UpdateServiceResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
            }
        });

        MaterialButton deleteButton = view.findViewById(R.id.delete_service_button);
        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }


    @Override
    public void onCardClicked(BasicCard entity) {
        if (entity instanceof WorkDayDTO) {
            workDayAdapter.remove((WorkDayDTO) entity);
        } else if (entity instanceof EventType) {
            selectedEventTypesAdapter.remove((EventType) entity);
        }
    }

    private String getImageName(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        try (Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                return cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
        }
        return null;
    }

    private UpdateServiceRequestDTO updateRequestDTO(View view) {
        UpdateServiceRequestDTO dto = new UpdateServiceRequestDTO();
        PricingRequestDTO pricingDTO = new PricingRequestDTO();

        // Validate and set string attributes
        validateAndSet(view, R.id.service_name, R.id.service_name_layout, dto::setName);
        validateAndSet(view, R.id.service_description, R.id.service_description_layout, dto::setDescription);
        validateAndSet(view, R.id.service_specifics, R.id.service_specifics_layout, dto::setSpecifics);

        // Validate and set integer attributes
        validateAndSetInt(view, R.id.reservation_deadline, R.id.reservation_deadline_layout, dto::setReservationDeadlineInDays);
        validateAndSetInt(view, R.id.cancellation_deadline, R.id.cancellation_deadline_layout, dto::setCancellationDeadlineInDays);
        validateAndSetInt(view, R.id.min_duration, R.id.min_duration_layout, dto::setMinDurationInMinutes);
        validateAndSetInt(view, R.id.max_duration, R.id.max_duration_layout, dto::setMaxDurationInMinutes);

        // Validate and set double attributes for Pricing
        validateAndSetDouble(view, R.id.service_price, R.id.service_price_layout, pricingDTO::setPrice);
        validateAndSetDouble(view, R.id.service_discount, R.id.service_discount_layout, pricingDTO::setDiscount);
        pricingDTO.setActiveFrom(LocalDate.now().toString());
        dto.setPricing(pricingDTO);

        dto.setImages(new HashSet<>(images));
        dto.setWorkDaysDTOs(new HashSet<>(workDays));
        dto.setAppliesTo(new HashSet<>(selectedEventTypes));

        dto.setVisible(visibleCheckBox.isChecked());
        dto.setAvailable(availableCheckBox.isChecked());

        return dto;
    }

    private CreateServiceRequestDTO createRequestDTO(View view) {
        CreateServiceRequestDTO dto = new CreateServiceRequestDTO();
        PricingRequestDTO pricingDTO = new PricingRequestDTO();

        // Validate and set string attributes
        validateAndSet(view, R.id.service_name, R.id.service_name_layout, dto::setName);
        validateAndSet(view, R.id.service_description, R.id.service_description_layout, dto::setDescription);
        validateAndSet(view, R.id.service_specifics, R.id.service_specifics_layout, dto::setSpecifics);

        // Validate and set integer attributes
        validateAndSetInt(view, R.id.reservation_deadline, R.id.reservation_deadline_layout, dto::setReservationDeadlineInDays);
        validateAndSetInt(view, R.id.cancellation_deadline, R.id.cancellation_deadline_layout, dto::setCancellationDeadlineInDays);
        if (dto.getReservationDeadlineInDays() < dto.getCancellationDeadlineInDays() || dto.getReservationDeadlineInDays() < 1 || dto.getCancellationDeadlineInDays() < 1)
            throw new IllegalArgumentException("Deadlines not valid!");
        validateAndSetInt(view, R.id.min_duration, R.id.min_duration_layout, dto::setMinDurationInMinutes);
        validateAndSetInt(view, R.id.max_duration, R.id.max_duration_layout, dto::setMaxDurationInMinutes);
        if (dto.getMaxDurationInMinutes() < 0 || dto.getMinDurationInMinutes() < 0 || dto.getMaxDurationInMinutes() < dto.getMinDurationInMinutes())
            throw new IllegalArgumentException("Durations not valid!");

        // Validate and set double attributes for Pricing
        validateAndSetDouble(view, R.id.service_price, R.id.service_price_layout, pricingDTO::setPrice);
        validateAndSetDouble(view, R.id.service_discount, R.id.service_discount_layout, pricingDTO::setDiscount);
        pricingDTO.setActiveFrom(LocalDate.now().toString());
        dto.setPricing(pricingDTO);

        for (Category category : categories) {
            if (category.getName().equals(categoryDropdown.getText().toString()))
                dto.setCategory(category);
        }
        if (dto.getCategory() == null) {
            TextInputLayout layout = view.findViewById(R.id.categories_dropdown_layout);
            layout.setError("Field is required!");
            throw new IllegalArgumentException("Category not valid!");
        }

        dto.setImages(new HashSet<>(images));
        dto.setWorkDaysDTOs(new HashSet<>(workDays));
        dto.setAppliesTo(new HashSet<>(selectedEventTypes));
        dto.setVisible(visibleCheckBox.isChecked());
        dto.setAvailable(availableCheckBox.isChecked());

        if (dto.getCategory().getStatus() == CategoryStatus.PENDING)
            dto.setRecommended(true);

        return dto;
    }

    private void validateAndSet(View view, int inputId, int layoutId, Consumer<String> setter) {
        TextInputLayout layout = view.findViewById(layoutId);
        TextInputEditText input = view.findViewById(inputId);

        if (input != null && input.getText() != null && !input.getText().toString().isEmpty()) {
            setter.accept(input.getText().toString());
            if (layout != null) {
                layout.setError(null);
            }
        } else if (layout != null) {
            layout.setError("Field is required!");
            throw new IllegalArgumentException("Field not valid!");
        }
    }

    private void validateAndSetInt(View view, int inputId, int layoutId, Consumer<Integer> setter) {
        TextInputLayout layout = view.findViewById(layoutId);
        TextInputEditText input = view.findViewById(inputId);

        try {
            if (input != null && input.getText() != null && !input.getText().toString().isEmpty()) {
                int value = Integer.parseInt(input.getText().toString());
                setter.accept(value);
                if (layout != null) {
                    layout.setError(null);
                }
            } else if (layout != null) {
                layout.setError("Field is required!");
                throw new IllegalArgumentException("Field not valid!");
            }
        } catch (NumberFormatException e) {
            if (layout != null) {
                layout.setError("Invalid number format!");
                throw new IllegalArgumentException("Invalid number format!");
            }
        }
    }

    private void validateAndSetDouble(View view, int inputId, int layoutId, Consumer<Double> setter) {
        TextInputLayout layout = view.findViewById(layoutId);
        TextInputEditText input = view.findViewById(inputId);

        try {
            if (input != null && input.getText() != null && !input.getText().toString().isEmpty()) {
                double value = Double.parseDouble(input.getText().toString());
                setter.accept(value);
                if (layout != null) {
                    layout.setError(null);
                }
            } else if (layout != null) {
                layout.setError("Field is required!");
                throw new IllegalArgumentException("Field not valid!");
            }
        } catch (NumberFormatException e) {
            if (layout != null) {
                layout.setError("Invalid number format!");
                throw new IllegalArgumentException("Invalid number format!");
            }
        }
    }

    private void setServiceFields() {
        categoryDropdown.setText(serviceDTO.getCategory().getName(), false);
        nameInput.setText(serviceDTO.getName());
        descriptionInput.setText(serviceDTO.getDescription());
        priceInput.setText(String.valueOf(serviceDTO.getPricing().getPrice()));
        discountInput.setText(String.valueOf(serviceDTO.getPricing().getDiscount()));
        specificsInput.setText(serviceDTO.getSpecifics());

        workDayAdapter.addAll(serviceDTO.getWorkDaysDTOs());

        reservationInput.setText(String.valueOf(serviceDTO.getReservationDeadlineInDays()));
        cancellationInput.setText(String.valueOf(serviceDTO.getCancellationDeadlineInDays()));
        minDurationInput.setText(String.valueOf(serviceDTO.getMinDurationInMinutes()));
        maxDurationInput.setText(String.valueOf(serviceDTO.getMaxDurationInMinutes()));

        images.clear();
        images.addAll(serviceDTO.getImages());
        imageListAdapter.notifyDataSetChanged();

        visibleCheckBox.setChecked(serviceDTO.isVisible());
        availableCheckBox.setChecked(serviceDTO.isAvailable());

        selectedEventTypesAdapter.addAll(serviceDTO.getAppliesTo());
    }

    private void fetchCategories() {
          categoryService.findAllActive(JwtUtil.getAuthorizationValue(getContext()))
                .enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call,
                                   @NonNull Response<List<Category>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    categories = new ArrayList<>(response.body());

                    ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            categories
                    );
                    categoryDropdown.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call,
                                  @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void fetchEventTypes() {
        eventTypeService.findAllActive(JwtUtil.getAuthorizationValue(getContext()))
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

    private void showDeleteConfirmationDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this service?")
                .setPositiveButton("Delete", (d, which) -> deleteService())
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (deleteButton != null) {
                deleteButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            }
        });
        dialog.show();
    }

    private void deleteService() {
        Call<DeleteServiceResponseDTO> call = serviceService.deleteService(JwtUtil.getAuthorizationValue(getContext()), Objects.requireNonNull(serviceDTO).getId());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<DeleteServiceResponseDTO> call,
                                   @NonNull Response<DeleteServiceResponseDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    returnToBaseFragment("Service deleted successfully!");
                } else {
                    try {
                        // Show error message
                        String errorBody = Objects.requireNonNull(response.errorBody()).string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String message = jsonObject.getString("message");
                        MaterialTextView errorMsg = requireView().findViewById(R.id.error_msg);
                        errorMsg.setText(message);
                        errorMsg.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DeleteServiceResponseDTO> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
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

    private void returnToBaseFragment(String responseMessage) {
        Toast.makeText(requireContext(), responseMessage, Toast.LENGTH_LONG).show();
        Navigation.findNavController(requireView()).navigate(R.id.navigate_back_from_service_management);
    }
}