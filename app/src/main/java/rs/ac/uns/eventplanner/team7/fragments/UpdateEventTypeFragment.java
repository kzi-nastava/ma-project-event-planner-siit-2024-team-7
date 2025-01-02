package rs.ac.uns.eventplanner.team7.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.UpdateEventTypeRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.UpdateEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;


public class UpdateEventTypeFragment extends Fragment {

    private List<CategoryResponseDTO> selectedCategories;
    private Integer eventTypeId;
    private EventTypeService eventTypeService;
    private EventTypeCategoryManipulationFragment categoryFragment;
    private boolean isActive;

    public UpdateEventTypeFragment() {
        // Required empty public constructor
    }

    public static UpdateEventTypeFragment newInstance() {
        return new UpdateEventTypeFragment();
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
            fillFields(view);
        });
        getChildFragmentManager().beginTransaction()
                .replace(R.id.category_fragment_container, categoryFragment, "EventTypeCategoryManipulationFragmentTag")
                .commit();

        LinearLayout content = view.findViewById(R.id.content_view);
        MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
        content.setVisibility(View.GONE);
        loadingMsg.setVisibility(View.VISIBLE);

        selectedCategories = new ArrayList<>();
        eventTypeService = ClientUtils.retrofit.create(EventTypeService.class);

        MaterialButton updateButton = view.findViewById(R.id.update_event_type);
        updateButton.setOnClickListener(v -> update(isActive));
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
                        setupDeleteButton(view); // need to call it here for sync
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

    private void update(boolean active) {
        this.selectedCategories = categoryFragment.getSelectedCategories();
        UpdateEventTypeRequestDTO requestDTO = new UpdateEventTypeRequestDTO();
        TextInputLayout descLayout = requireView().findViewById(R.id.update_event_type_desc_layout);
        TextInputEditText descInput = requireView().findViewById(R.id.update_event_type_desc);
        if (Objects.requireNonNull(descInput.getText()).toString().isEmpty()) {
            descLayout.setError("Field is required");
            return;
        }
        requestDTO.setDescription(descInput.getText().toString());
        requestDTO.setActive(active);
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
                    navigateToEventTypeList("Event type updated successfully!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateEventTypeResponseDTO> call, @NonNull Throwable t) {

            }
        };
    }

    private void setupDeleteButton(View view) {
        MaterialButton deleteButton = view.findViewById(R.id.delete_event_type);
        if (!isActive) {
            deleteButton.setText(R.string.reactivate_event_type);
            deleteButton.setBackgroundColor(getResources().getColor(R.color.blue_200));

            Pair<AlertDialog, View> dialogData = createDialog();
            AlertDialog dialog = dialogData.first;
            View dialogView = dialogData.second;

            MaterialTextView msg = dialogView.findViewById(R.id.event_type_deactivation_msg);
            msg.setText(R.string.reactivate_message);

            dialog.setOnShowListener(d -> {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    update(!isActive);
                    dialog.dismiss();
                });
            });

            deleteButton.setOnClickListener(v -> dialog.show());

            // set the content

        } else {
            deleteButton.setOnClickListener(v -> delete());
        }
        LinearLayout content = view.findViewById(R.id.content_view);
        MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
        content.setVisibility(View.VISIBLE);
        loadingMsg.setVisibility(View.GONE);
    }

    private void delete() {
        Call<Void> call = eventTypeService.delete(JwtUtil.getAuthorizationValue(requireContext()), eventTypeId);

        Pair<AlertDialog, View> dialogData = createDialog();
        AlertDialog dialog = dialogData.first;

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                call.enqueue(deleteCallback(dialog));
            });
        });

        dialog.show();
    }


    private Callback<Void> deleteCallback(Dialog dialog) {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    navigateToEventTypeList("Event type deactivated successfully!");
                }
                else {
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

            }
        };
    }

    private Pair<AlertDialog, View> createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_deactivation_event_type, null);
        builder.setView(dialogView)
                .setPositiveButton(R.string.yes, null) // Set to null for custom behavior
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        return new Pair<>(builder.create(), dialogView);
    }


    private void navigateToEventTypeList(String msg) {
        Fragment fragment = new EventTypeListFragment();
        Bundle args = new Bundle();
        args.putString("snackbar_message", msg);
        fragment.setArguments(args);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.home_main_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

}