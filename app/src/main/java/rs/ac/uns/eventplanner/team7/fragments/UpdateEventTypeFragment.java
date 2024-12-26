package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.CreateEventTypeRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.UpdateEventTypeRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.UpdateEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;


public class UpdateEventTypeFragment extends Fragment {

    private List<CategoryResponseDTO> selectedCategories;
    private List<CategoryResponseDTO> addableCategories;
    private Integer eventTypeId;
    private EventTypeService eventTypeService;
    private EventTypeCategoryManipulationFragment categoryFragment;
    private boolean isActive;

    public UpdateEventTypeFragment() {
        // Required empty public constructor
    }

    public static UpdateEventTypeFragment newInstance(String param1, String param2) {
        UpdateEventTypeFragment fragment = new UpdateEventTypeFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventTypeId = getArguments().getInt("eventTypeId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_event_type, container, false);

        categoryFragment = EventTypeCategoryManipulationFragment.newInstance();
        categoryFragment.setCategoriesFetchedListener(categories -> {
            this.addableCategories = categories;
            fillFields(view);

        });
        getChildFragmentManager().beginTransaction()
                .replace(R.id.category_fragment_container, categoryFragment, "EventTypeCategoryManipulationFragmentTag")
                .commit();
//        categoryFragment.notifyChange();
        selectedCategories = new ArrayList<>();
        addableCategories = new ArrayList<>();
        eventTypeService = ClientUtils.retrofit.create(EventTypeService.class);

        MaterialButton updateButton = view.findViewById(R.id.update_event_type);
        updateButton.setOnClickListener(v -> update());

        return view;
    }

    private void fillFields(View view) {
        Call<GetEventTypeResponseDTO> call = eventTypeService.get(JwtUtil.getAuthorizationValue(requireContext()), eventTypeId);
        call.enqueue(updateCallBack(view));
    }

    private Callback<GetEventTypeResponseDTO> updateCallBack(View view) {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetEventTypeResponseDTO> call, @NonNull Response<GetEventTypeResponseDTO> response) {
                if (response.isSuccessful()) {
                    GetEventTypeResponseDTO dto = response.body();
                    if (dto != null) {
                        isActive = dto.isActive();
                        fillFields(view, dto);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetEventTypeResponseDTO> call, @NonNull Throwable t) {

            }
        };
    }

    private void fillFields(View view, GetEventTypeResponseDTO dto) {
        TextInputEditText nameInput = view.findViewById(R.id.update_event_type_name);
        TextInputEditText descInput = view.findViewById(R.id.update_event_type_desc);
        nameInput.setText(dto.getName());
        descInput.setText(dto.getDescription());
        for (var cat : dto.getRecommendedCategories()) {
            CategoryResponseDTO catDto = new CategoryResponseDTO();
            catDto.setName(cat.getName());
            catDto.setDescription(cat.getDescription());
            catDto.setId(cat.getId());
            catDto.setStatus(cat.getStatus());
            selectedCategories.add(catDto);
        }
        categoryFragment.notifyChange(selectedCategories);
    }

    private void update() {
        this.selectedCategories = categoryFragment.getSelectedCategories();
        UpdateEventTypeRequestDTO requestDTO = new UpdateEventTypeRequestDTO();
        TextInputLayout descLayout = requireView().findViewById(R.id.update_event_type_desc_layout);
        TextInputEditText descInput = requireView().findViewById(R.id.update_event_type_desc);
        if (descInput.getText().toString().isEmpty()) {
            descLayout.setError("Field is required");
            return;
        }
        requestDTO.setDescription(descInput.getText().toString());
        requestDTO.setActive(isActive);
        requestDTO.setRecommendedCategories(new ArrayList<>());
        for (var cat : this.selectedCategories) {
            Category category = new Category(cat.getId(), cat.getName(), cat.getDescription(), cat.getStatus());
            requestDTO.getRecommendedCategories().add(category);
        }

        Call<UpdateEventTypeResponseDTO> call = eventTypeService.update(JwtUtil.getAuthorizationValue(requireContext()), eventTypeId, requestDTO);
        call.enqueue(updateCallback());
    }

    private Callback<UpdateEventTypeResponseDTO> updateCallback() {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UpdateEventTypeResponseDTO> call, @NonNull Response<UpdateEventTypeResponseDTO> response) {
                if (response.isSuccessful()) {
                    Fragment fragment = new EventTypeListFragment();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameLayout, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateEventTypeResponseDTO> call, @NonNull Throwable t) {

            }
        };
    }


}