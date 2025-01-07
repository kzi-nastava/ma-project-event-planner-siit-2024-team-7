package rs.ac.uns.eventplanner.team7.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

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
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.SelectedEventTypesAdapter;
import rs.ac.uns.eventplanner.team7.adapters.WorkDayAdapter;
import rs.ac.uns.eventplanner.team7.dto.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.CreateEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.CreateServiceRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.service.CreateServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.EventType;
import rs.ac.uns.eventplanner.team7.model.Pricing;
import rs.ac.uns.eventplanner.team7.model.WorkDay;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class ServiceManagementFragment extends Fragment {
    private final int PICK_IMAGE_REQUEST = 1;

    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);

    private WorkDayDialogFragment workDayDialogFragment;
    private WorkDayAdapter workDayAdapter;
    private SelectedEventTypesAdapter selectedEventTypesAdapter;

    private Set<WorkDay> workDaySet;
    private Set<String> images;
    private Set<String> selectedEventTypesNames;

    private AutoCompleteTextView categoryDropdown, eventTypesDropdown;
    private MaterialCheckBox visibleCheckBox, availableCheckBox;
    private MaterialButton selectImagesBtn, addWorkDayBtn, recommendCategoryBtn;
    private RecyclerView imagesView, workDaysView, selectedEventTypesView;

    public ServiceManagementFragment() {
        this.workDayDialogFragment = new WorkDayDialogFragment(workDaySet, workDayAdapter);
        this.workDaySet = new HashSet<>();
        this.images = new HashSet<>();
        this.selectedEventTypesNames = new HashSet<>();
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

        fetchCategoryNames();
        recommendCategoryBtn.setOnClickListener(v -> {
            CategoryRecommendationFragment fragment = new CategoryRecommendationFragment();
            fragment.show(requireActivity().getSupportFragmentManager(), "RecommendationDialog");
        });

        selectImagesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        addWorkDayBtn.setOnClickListener(v -> {
            workDayDialogFragment.show(requireActivity().getSupportFragmentManager(), "WorkDayDialog");
        });

        fetchEventTypeNames();
        selectedEventTypesView.setLayoutManager(new LinearLayoutManager(requireContext()));
        selectedEventTypesAdapter = new SelectedEventTypesAdapter(new ArrayList<>(selectedEventTypesNames));
        selectedEventTypesView.setAdapter(selectedEventTypesAdapter);

        MaterialButton saveButton = view.findViewById(R.id.save_service_button);
        saveButton.setOnClickListener(v -> {
            if (title.getText() == "SERVICE CREATION") {
                CreateServiceRequestDTO dto = createRequestDTO(view);
                Call<CreateServiceResponseDTO> call = serviceService.createService(
                        JwtUtil.getAuthorizationValue(requireContext()),
                        dto
                );
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<CreateServiceResponseDTO> call, @NonNull Response<CreateServiceResponseDTO> response) {
                        if (response.isSuccessful()) {
                            // Navigate back to the event type list
                            Fragment fragment = new SPPServicesFragment();
                            Bundle args = new Bundle();
                            args.putString("snackbar_message", "Service created successfully!");
                            fragment.setArguments(args);
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.home_main_fragment_container, fragment)
                                    .addToBackStack(null)
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
                        // Handle failure
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // Get the image name
                String imageName = getImageName(imageUri);

                // Add the image name to the Set
                if (imageName != null) {
                    images.add(imageName);
                    TextView newImage = new TextView(getContext());
                    newImage.setText(imageName);
                    imagesView.addView(newImage);
                    Log.d("ImageNamesSet", "Added: " + imageName);
                }
            }
        }
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
            e.printStackTrace();
        }
        return null;
    }


    private CreateServiceRequestDTO createRequestDTO(View view) {
        CreateServiceRequestDTO dto = new CreateServiceRequestDTO();
        Pricing pricing = new Pricing();

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
        validateAndSetDouble(view, R.id.service_price, R.id.service_price_layout, pricing::setPrice);
        validateAndSetDouble(view, R.id.service_discount, R.id.service_discount_layout, pricing::setDiscount);
        pricing.setActiveFrom(LocalDate.now());

        dto.setPricing(pricing); // Assign the Pricing object to the DTO

        if (!categoryDropdown.getText().toString().isEmpty()){
            categoryService.findActiveCategoryByName(categoryDropdown.getText().toString())
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<CategoryResponseDTO> call, @NonNull Response<CategoryResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            dto.setCategory(response.body().toCategory());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<CategoryResponseDTO> call, @NonNull Throwable t) {

                    }
                });
        }
        else {
            TextInputLayout layout = view.findViewById(R.id.categories_dropdown_layout);
            layout.setError("Field is required!");
        }

        dto.setImages(images);
        dto.setWorkDays(workDaySet);

        dto.setAppliesTo(new HashSet<>());
        for (String name : selectedEventTypesNames) {
            Call<GetEventTypeResponseDTO> call = eventTypeService.getByName(name);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetEventTypeResponseDTO> call, @NonNull Response<GetEventTypeResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        dto.getAppliesTo().add(response.body().toEventType());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetEventTypeResponseDTO> call, @NonNull Throwable t) {

                }
            });
        }

        dto.setVisible(visibleCheckBox.isChecked());
        dto.setAvailable(availableCheckBox.isChecked());

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
            }
        } catch (NumberFormatException e) {
            if (layout != null) {
                layout.setError("Invalid number format!");
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
            }
        } catch (NumberFormatException e) {
            if (layout != null) {
                layout.setError("Invalid number format!");
            }
        }
    }

    private void fetchCategoryNames() {
        categoryService.findAllNames().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> categories = new ArrayList<>(response.body());
                    categories.add(0, "All");

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

            }
        });
    }

    private void fetchEventTypeNames() {
        eventTypeService.findAllNames().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<String>> call,
                                   @NonNull Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> eventTypes = new ArrayList<>(response.body());
                    eventTypes.add(0, "All");

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            eventTypes
                    );
                    eventTypesDropdown.setAdapter(adapter);

                    eventTypesDropdown.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedEventType = (String) parent.getItemAtPosition(position);
                        selectedEventTypesNames.add(selectedEventType);  // Add to the Set
                        Log.d("EventTypeSelected", "Added event type: " + selectedEventType);

                        // Optionally, update your RecyclerView or UI to reflect the change
                        selectedEventTypesAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {}
        });
    }

}