package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.CreateEventTypeRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.CreateEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class CreateEventTypeFragment extends Fragment {

    private List<CategoryResponseDTO> selectedCategories; // Categories selected in the child fragment
    private EventTypeService eventTypeService;

    public CreateEventTypeFragment() {
        // Required empty public constructor
    }

    public static CreateEventTypeFragment newInstance() {
        return new CreateEventTypeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedCategories = new ArrayList<>(); // Initialize the selected categories list
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event_type, container, false);

        // Load the EventTypeCategoryManipulationFragment into a child container
        EventTypeCategoryManipulationFragment categoryFragment = EventTypeCategoryManipulationFragment.newInstance();
        categoryFragment.setCategorySelectionListener(selectedCategories -> {
            this.selectedCategories = selectedCategories; // Update selected categories
        });

        getChildFragmentManager().beginTransaction()
                .replace(R.id.category_fragment_container, categoryFragment, "EventTypeCategoryManipulationFragmentTag")
                .commit();

        // Set up the create button for creating the event type
        eventTypeService = ClientUtils.retrofit.create(EventTypeService.class);
        MaterialButton createButton = view.findViewById(R.id.create_event_type);
        createButton.setOnClickListener(v -> {
            Call<CreateEventTypeResponseDTO> call = eventTypeService.create(
                    JwtUtil.getAuthorizationValue(requireContext()),
                    createRequestDTO(view)
            );
            call.enqueue(createCallback());
        });

        return view;
    }

    private CreateEventTypeRequestDTO createRequestDTO(View view) {
        CreateEventTypeRequestDTO dto = new CreateEventTypeRequestDTO();

        // Validate and set input fields
        validateAndSet(view, R.id.event_type_name_layout, R.id.event_type_name, dto);
        validateAndSet(view, R.id.event_type_desc_layout, R.id.event_type_desc, dto);

        // Add selected categories to the DTO
        dto.setRecommendedCategories(new ArrayList<>());
        for (var cat : selectedCategories) {
            Category category = new Category(cat.getName(), cat.getDescription(), true);
            dto.getRecommendedCategories().add(category);
        }

        return dto;
    }

    private Callback<CreateEventTypeResponseDTO> createCallback() {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CreateEventTypeResponseDTO> call, @NonNull Response<CreateEventTypeResponseDTO> response) {
                if (response.isSuccessful()) {
                    // Navigate back to the event type list
                    Fragment fragment = new EventTypeListFragment();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameLayout, fragment)
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
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateEventTypeResponseDTO> call, @NonNull Throwable t) {
                // Handle failure
            }
        };
    }

    private void validateAndSet(View view, int layoutId, int inputId, CreateEventTypeRequestDTO dto) {
        TextInputLayout layout = view.findViewById(layoutId);
        TextInputEditText input = view.findViewById(inputId);
        if (input.getText() != null && !input.getText().toString().isEmpty()) {
            if (inputId == R.id.event_type_name) {
                dto.setName(input.getText().toString());
            } else {
                dto.setDescription(input.getText().toString());
            }
            layout.setError(null);
        } else {
            layout.setError("Field is required!");
        }
    }
}
