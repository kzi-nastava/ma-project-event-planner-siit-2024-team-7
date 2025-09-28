package rs.ac.uns.eventplanner.team7.ui.fragments.admin.event_types;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import rs.ac.uns.eventplanner.team7.data.model.Category;
import rs.ac.uns.eventplanner.team7.data.dto.event_type.UpdateEventTypeRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event_type.UpdateEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class UpdateEventTypeFragment extends Fragment {

    private List<Category> selectedCategories;
    private Integer eventTypeId;
    private final EventTypeService eventTypeService = ClientUtils.injectService(EventTypeService.class);
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

        MaterialButton updateButton = view.findViewById(R.id.update_event_type);
        updateButton.setOnClickListener(v -> update(isActive));

        return view;
    }

    private void fillFields(View view) {
        Call<EventType> call = eventTypeService.get(AuthUtil.getAuthorizationValue(requireContext()), eventTypeId);
        call.enqueue(updateCallBack(view));
    }

    private Callback<EventType> updateCallBack(View view) {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<EventType> call, @NonNull Response<EventType> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    EventType dto = response.body();
                    if (dto != null) {
                        isActive = dto.isActive();
                        fillFields(view, dto);
                        setupDeleteButton(view); // need to call it here for sync
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<EventType> call, @NonNull Throwable t) {
                Log.e("ERROR", "Update failed", t);
            }
        };
    }

    private void fillFields(View view, EventType dto) {
        TextInputEditText nameInput = view.findViewById(R.id.update_event_type_name);
        TextInputEditText descInput = view.findViewById(R.id.update_event_type_desc);
        nameInput.setText(dto.getName());
        descInput.setText(dto.getDescription());
        for (var cat : dto.getRecommendedCategories()) {
            Category category = new Category();
            category.setName(cat.getName());
            category.setDescription(cat.getDescription());
            category.setId(cat.getId());
            category.setStatus(cat.getStatus());
            selectedCategories.add(category);
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

        Call<UpdateEventTypeResponseDTO> call = eventTypeService.update(AuthUtil.getAuthorizationValue(requireContext()), eventTypeId, requestDTO);
        call.enqueue(updateCallback());
    }

    private Callback<UpdateEventTypeResponseDTO> updateCallback() {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UpdateEventTypeResponseDTO> call, @NonNull Response<UpdateEventTypeResponseDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    returnToBaseFragment("Event type updated successfully!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UpdateEventTypeResponseDTO> call, @NonNull Throwable t) {
                Log.e("ERROR", "Update failed", t);
            }
        };
    }

    private void setupDeleteButton(View view) {
        MaterialButton deleteButton = view.findViewById(R.id.delete_event_type);
        if (!isActive) {
            deleteButton.setText(R.string.reactivate_event_type);
            deleteButton.setBackgroundColor(getResources().getColor(R.color.blue_300));

            MaterialAlertDialogBuilder builder = createMaterialDialog(
                    R.string.event_type_reactivation,
                    R.string.reactivate_event_type_message,
                    (dialog, which) -> {
                        update(!isActive);
                        dialog.dismiss();
                    }
            );
            deleteButton.setOnClickListener(v -> builder.show());
        } else {
            deleteButton.setOnClickListener(v -> delete());
        }
        LinearLayout content = view.findViewById(R.id.content_view);
        MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
        content.setVisibility(View.VISIBLE);
        loadingMsg.setVisibility(View.GONE);
    }

    private void delete() {
        Call<Void> call = eventTypeService.delete(AuthUtil.getAuthorizationValue(requireContext()), eventTypeId);

        MaterialAlertDialogBuilder builder = createMaterialDialog(
                R.string.event_type_deactivation,
                R.string.event_type_deactivation_message,
                (dialog, which) -> call.enqueue(deleteCallback(dialog))
        );

        builder.show();
    }

    private Callback<Void> deleteCallback(android.content.DialogInterface dialog) {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    dialog.dismiss();
                    returnToBaseFragment("Event type deactivated successfully!");
                } else {
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("ERROR", "Delete failed", t);
            }
        };
    }

    private MaterialAlertDialogBuilder createMaterialDialog(int titleResId, int messageResId, android.content.DialogInterface.OnClickListener positiveAction) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.yes, positiveAction)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
    }

    private void returnToBaseFragment(String msg) {
        Bundle args = new Bundle();
        args.putString("snackbar_message", msg);
        Navigation.findNavController(requireView()).navigate(R.id.navigate_back_from_event_type_update, args);
    }
}
