package rs.ac.uns.eventplanner.team7.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
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
import rs.ac.uns.eventplanner.team7.adapters.ImageListAdapter;
import rs.ac.uns.eventplanner.team7.adapters.SelectedEventTypesAdapter;
import rs.ac.uns.eventplanner.team7.adapters.WorkDayAdapter;
import rs.ac.uns.eventplanner.team7.dto.category.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.pricing.CreatePricingRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.service.WorkDayDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.pricing.UpdatePricingRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.service.CreateServiceRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.service.CreateServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.DeleteServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.UpdateServiceRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.service.UpdateServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.model.EventType;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class ServiceManagementFragment extends Fragment {
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private final GetServiceResponseDTO serviceDTO;

    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);

    private WorkDayAdapter workDayAdapter;
    private SelectedEventTypesAdapter selectedEventTypesAdapter;
    private ImageListAdapter imageListAdapter;

    private final List<WorkDayDTO> workDayList;
    private final List<String> images;
    private final List<GetEventTypeResponseDTO> selectedEventTypes;
    private List<CategoryResponseDTO> categories;

    private TextInputEditText nameInput, descriptionInput, priceInput, discountInput, specificsInput, reservationInput, cancellationInput, minDurationInput, maxDurationInput;
    private AutoCompleteTextView categoryDropdown, eventTypesDropdown;
    private MaterialCheckBox visibleCheckBox, availableCheckBox;
    private MaterialButton selectImagesBtn, addWorkDayBtn, recommendCategoryBtn;
    private RecyclerView imagesView, workDaysView, selectedEventTypesView;

    public ServiceManagementFragment(GetServiceResponseDTO serviceDTO) {
        this.serviceDTO = serviceDTO;
        this.workDayList = new ArrayList<>();
        this.images = new ArrayList<>();
        this.selectedEventTypes = new ArrayList<>();
        this.categories = new ArrayList<>();
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
        recommendCategoryBtn = view.findViewById(R.id.button_recommend_category);
        imagesView = view.findViewById(R.id.recycler_view_images);
        workDaysView = view.findViewById(R.id.recycler_view_work_days);
        selectedEventTypesView = view.findViewById(R.id.recycler_view_selected_event_types);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton back = view.findViewById(R.id.back_button);
        back.setOnClickListener(v -> {
            SPPServicesBaseFragment fragment = new SPPServicesBaseFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_main_fragment_container, fragment)
                    .commit();
        });

        fetchCategories();
        recommendCategoryBtn.setOnClickListener(v -> {
            CategoryRecommendationFragment fragment = new CategoryRecommendationFragment(categories, categoryDropdown);
            fragment.show(requireActivity().getSupportFragmentManager(), "RecommendationDialog");
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
        imagesView.setLayoutManager(new LinearLayoutManager(getContext()));
        imagesView.setHasFixedSize(true);
        imagesView.setAdapter(imageListAdapter);
        selectImagesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        workDayAdapter = new WorkDayAdapter(getContext(), workDayList);
        workDaysView.setLayoutManager(new LinearLayoutManager(getContext()));
        workDaysView.setHasFixedSize(true);
        workDaysView.setAdapter(workDayAdapter);
        addWorkDayBtn.setOnClickListener(v -> {
            WorkDayDialogFragment fragment = new WorkDayDialogFragment(workDayList, workDayAdapter);
            fragment.show(requireActivity().getSupportFragmentManager(), "WorkDayDialog");
        });

        fetchEventTypes();
        selectedEventTypesView.setLayoutManager(new LinearLayoutManager(getContext()));
        selectedEventTypesView.setHasFixedSize(true);
        selectedEventTypesAdapter = new SelectedEventTypesAdapter(getContext(), selectedEventTypes);
        selectedEventTypesView.setAdapter(selectedEventTypesAdapter);


        MaterialTextView title = view.findViewById(R.id.welcomeMessage);
        if (serviceDTO == null) {
            title.setText(R.string.service_creation);
            view.findViewById(R.id.delete_service_button).setVisibility(View.GONE);
        }
        else {
            title.setText(R.string.service_update);
            view.findViewById(R.id.categories_dropdown).setEnabled(false);
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
                Call<CreateServiceResponseDTO> call = serviceService.createService(
                        JwtUtil.getAuthorizationValue(requireContext()),
                        dto
                );
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateServiceResponseDTO> call, @NonNull Response<CreateServiceResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Navigate back to the event type list
                            SPPServicesBaseFragment fragment = new SPPServicesBaseFragment();
                            Bundle args = new Bundle();
                            args.putString("snackbar_message", "Service created successfully!");
                            fragment.setArguments(args);
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.home_main_fragment_container, fragment)
                                    .commit();
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
                Call<UpdateServiceResponseDTO> call = serviceService.updateService(
                        JwtUtil.getAuthorizationValue(requireContext()),
                        serviceDTO.getId(),
                        dto
                );
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateServiceResponseDTO> call, @NonNull Response<UpdateServiceResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Navigate back to the event type list
                            SPPServicesBaseFragment fragment = new SPPServicesBaseFragment();
                            Bundle args = new Bundle();
                            args.putString("snackbar_message", "Service created successfully!");
                            fragment.setArguments(args);
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.home_main_fragment_container, fragment)
                                    .commit();
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
            Call<DeleteServiceResponseDTO> call = serviceService.deleteService(JwtUtil.getAuthorizationValue(getContext()), Objects.requireNonNull(serviceDTO).getId());
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<DeleteServiceResponseDTO> call,
                                       @NonNull Response<DeleteServiceResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Navigate back to the event type list
                        SPPServicesBaseFragment fragment = new SPPServicesBaseFragment();
                        Bundle args = new Bundle();
                        args.putString("snackbar_message", "Service created successfully!");
                        fragment.setArguments(args);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.home_main_fragment_container, fragment)
                                .commit();
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
        });
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
        UpdatePricingRequestDTO pricingDTO = new UpdatePricingRequestDTO();

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
        dto.setWorkDaysDTOs(new HashSet<>(workDayList));
        dto.setAppliesTo(new HashSet<>());
        for (GetEventTypeResponseDTO eventTypeDTO : selectedEventTypes) {
            dto.getAppliesTo().add(eventTypeDTO.toEventType());
        }

        dto.setVisible(visibleCheckBox.isChecked());
        dto.setAvailable(availableCheckBox.isChecked());

        return dto;
    }

    private CreateServiceRequestDTO createRequestDTO(View view) {
        CreateServiceRequestDTO dto = new CreateServiceRequestDTO();
        CreatePricingRequestDTO pricingDTO = new CreatePricingRequestDTO();

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

        for (CategoryResponseDTO categoryDTO : categories) {
            if (categoryDTO.getName().equals(categoryDropdown.getText().toString()))
                dto.setCategory(categoryDTO.toCategory());
        }
        if (dto.getCategory() == null) {
            TextInputLayout layout = view.findViewById(R.id.categories_dropdown_layout);
            layout.setError("Field is required!");
            throw new IllegalArgumentException("Category not valid!");
        }

        dto.setImages(new HashSet<>(images));
        dto.setWorkDaysDTOs(new HashSet<>(workDayList));
        dto.setAppliesTo(new HashSet<>());
        for (GetEventTypeResponseDTO eventTypeDTO : selectedEventTypes) {
            dto.getAppliesTo().add(eventTypeDTO.toEventType());
        }

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

        workDayList.clear();
        workDayList.addAll(serviceDTO.getWorkDaysDTOs());
        workDayAdapter.notifyDataSetChanged();

        reservationInput.setText(String.valueOf(serviceDTO.getReservationDeadlineInDays()));
        cancellationInput.setText(String.valueOf(serviceDTO.getCancellationDeadlineInDays()));
        minDurationInput.setText(String.valueOf(serviceDTO.getMinDurationInMinutes()));
        maxDurationInput.setText(String.valueOf(serviceDTO.getMaxDurationInMinutes()));

        images.clear();
        images.addAll(serviceDTO.getImages());
        imageListAdapter.notifyDataSetChanged();

        visibleCheckBox.setChecked(serviceDTO.isVisible());
        availableCheckBox.setChecked(serviceDTO.isAvailable());

        selectedEventTypes.clear();
        for (EventType et : serviceDTO.getAppliesTo())
            selectedEventTypes.add(new GetEventTypeResponseDTO(et));
        selectedEventTypesAdapter.notifyDataSetChanged();
    }

    private void fetchCategories() {
          categoryService.findAllActive(JwtUtil.getAuthorizationValue(getContext()))
                .enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<CategoryResponseDTO>> call,
                                   @NonNull Response<List<CategoryResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = new ArrayList<>(response.body());

                    ArrayAdapter<CategoryResponseDTO> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            categories
                    );
                    categoryDropdown.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CategoryResponseDTO>> call,
                                  @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void fetchEventTypes() {
        eventTypeService.findAllActive(JwtUtil.getAuthorizationValue(getContext()))
                .enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<GetEventTypeResponseDTO>> call,
                                   @NonNull Response<List<GetEventTypeResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetEventTypeResponseDTO> eventTypes = new ArrayList<>(response.body());

                    ArrayAdapter<GetEventTypeResponseDTO> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            eventTypes
                    );
                    eventTypesDropdown.setAdapter(adapter);

                    eventTypesDropdown.setOnItemClickListener((parent, view, position, id) -> {
                        GetEventTypeResponseDTO selectedEventType = (GetEventTypeResponseDTO) parent.getItemAtPosition(position);
                        for (GetEventTypeResponseDTO eventTypeDTO : selectedEventTypes) {
                            if (eventTypeDTO.getName().equals(selectedEventType.getName()))
                                return;
                        }
                        selectedEventTypes.add(selectedEventType);  // Add to the Set
                        Log.d("EventTypeSelected", "Added event type: " + selectedEventType);
                        selectedEventTypesAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GetEventTypeResponseDTO>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

}