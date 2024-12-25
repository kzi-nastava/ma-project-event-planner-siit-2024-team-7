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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;


public class UpdateEventTypeFragment extends Fragment {

    private List<CategoryResponseDTO> selectedCategories;
    private List<CategoryResponseDTO> addableCategories;
    private Integer eventTypeId;
    private EventTypeService eventTypeService;
    private EventTypeCategoryManipulationFragment categoryFragment;

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
        Log.d("SIZE", String.valueOf(selectedCategories.size()));
        Log.d("SIZE2", String.valueOf(categoryFragment.getSelectedCategories().size()));
    }
}