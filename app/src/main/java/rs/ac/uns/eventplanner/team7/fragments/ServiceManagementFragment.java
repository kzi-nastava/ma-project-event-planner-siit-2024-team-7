package rs.ac.uns.eventplanner.team7.fragments;

import android.app.Activity;
import android.content.Context;
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
import rs.ac.uns.eventplanner.team7.dto.WorkDayDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.CreateServiceRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.service.CreateServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class ServiceManagementFragment extends Fragment {
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);

    private WorkDayAdapter workDayAdapter;
    private SelectedEventTypesAdapter selectedEventTypesAdapter;
    private ImageListAdapter imageListAdapter;

    private List<WorkDayDTO> workDayList;
    private List<String> images;
    private List<GetEventTypeResponseDTO> selectedEventTypes;
    private List<CategoryResponseDTO> categories;

    private AutoCompleteTextView categoryDropdown, eventTypesDropdown;
    private MaterialCheckBox visibleCheckBox, availableCheckBox;
    private MaterialButton selectImagesBtn, addWorkDayBtn, recommendCategoryBtn;
    private RecyclerView imagesView, workDaysView, selectedEventTypesView;

    public ServiceManagementFragment() {
        this.workDayList = new ArrayList<>();
        this.images = new ArrayList<>();
        this.selectedEventTypes = new ArrayList<>();
        this.categories = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_management, container, false);
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
        MaterialTextView title = view.findViewById(R.id.welcomeMessage);
        Bundle args = getArguments();
        if (args != null) {
            title.setText(args.getString("message_key"));
        }
        if (title.getText() == "SERVICE CREATION") {
            view.findViewById(R.id.delete_service_button).setVisibility(View.GONE);
        }
        else if (title.getText() == "SERVICE UDPATE") {
            view.findViewById(R.id.categories_dropdown).setEnabled(false);
        }

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

        MaterialButton saveButton = view.findViewById(R.id.save_service_button);
        saveButton.setOnClickListener(v -> {
            if (title.getText() == "SERVICE CREATION") {
                CreateServiceRequestDTO dto;
                try {
                    dto = createRequestDTO(view);
                } catch (IllegalArgumentException e) {
                    Snackbar snackbar = Snackbar.make(view, "Fields are not valid", Snackbar.LENGTH_SHORT);
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
                        if (response.isSuccessful()) {
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
                                String errorBody = response.errorBody().string();
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
        });
    }

    private String getImageName(Uri uri) {
        // Use the fragment's context to access the ContentResolver
        Context context = getContext(); // Or requireContext() for non-null guarantee
        if (context == null) {
            return null; // Safeguard if the context is unavailable
        }

        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                return cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            Log.d("ERROR", Objects.requireNonNull(e.getMessage()));
        }
        return null;
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
        validateAndSetInt(view, R.id.min_duration, R.id.min_duration_layout, dto::setMinDurationInMinutes);
        validateAndSetInt(view, R.id.max_duration, R.id.max_duration_layout, dto::setMaxDurationInMinutes);

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
            throw new IllegalArgumentException();
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
            throw new IllegalArgumentException();
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
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            if (layout != null) {
                layout.setError("Invalid number format!");
                throw new IllegalArgumentException();
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
                throw new IllegalArgumentException();
            }
        } catch (NumberFormatException e) {
            if (layout != null) {
                layout.setError("Invalid number format!");
                throw new IllegalArgumentException();
            }
        }
    }

    private void fetchCategories() {
          categoryService.getAll(JwtUtil.getAuthorizationValue(getContext()))
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