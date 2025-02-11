package rs.ac.uns.eventplanner.team7.fragments.admin.categories;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.Objects;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.category.CreateCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.category.DeleteCategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.category.UpdateCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class CategoryManagementFragment extends Fragment {
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private Category category;
    private TextInputEditText nameInput, descriptionInput;
    private MaterialButton saveButton, acceptButton, deleteButton;

    public CategoryManagementFragment() {
        // Required empty public constructor
    }

    public static CategoryManagementFragment newInstance(Category category) {
        CategoryManagementFragment fragment = new CategoryManagementFragment();
        fragment.category = category;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getParcelable("categoryDTO", Category.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_management, container, false);

        nameInput = view.findViewById(R.id.category_name);
        descriptionInput = view.findViewById(R.id.category_desc);

        saveButton = view.findViewById(R.id.save_category);
        acceptButton = view.findViewById(R.id.accept_category);
        deleteButton = view.findViewById(R.id.delete_category);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialTextView title = view.findViewById(R.id.category_management_welcome);
        if (category == null) {
            title.setText(getString(R.string.create_category));
            acceptButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        } else if (category.getStatus() == CategoryStatus.ACTIVE) {
            title.setText(getString(R.string.update_category));
            acceptButton.setVisibility(View.GONE);
            setContent();
        } else if (category.getStatus() == CategoryStatus.PENDING) {
            title.setText(getString(R.string.suggested_category));
            saveButton.setVisibility(View.GONE);
            deleteButton.setText(getString(R.string.reject_category));
            setContent();
        }

        saveButton.setOnClickListener(v -> {
            if (category == null) {
                CreateCategoryRequestDTO dto;
                try {
                    dto = createRequestDTO(view);
                } catch (IllegalArgumentException e) {
                    Snackbar snackbar = Snackbar.make(view, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return;
                }
                handleServiceCall(categoryService.createCategory(
                        JwtUtil.getAuthorizationValue(requireContext()), dto),
                        "Category created successfully!");
            } else {
                UpdateCategoryRequestDTO dto;
                try {
                    dto = updateRequestDTO(view);
                } catch (IllegalArgumentException e) {
                    Snackbar snackbar = Snackbar.make(view, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return;
                }
                handleServiceCall(categoryService.updateCategory(
                        JwtUtil.getAuthorizationValue(requireContext()), dto, category.getId()),
                        "Category updated successfully!");
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (category.getStatus() == CategoryStatus.ACTIVE)
                showDeleteConfirmationDialog();
            else {
                RejectCategoryDialogFragment fragment = RejectCategoryDialogFragment.newInstance(category);
                fragment.show(requireActivity().getSupportFragmentManager(), "RejectCategoryDialog");
            }

        });

        acceptButton.setOnClickListener(v -> handleServiceCall(categoryService
                .acceptRecommendedCategory(
                        JwtUtil.getAuthorizationValue(requireContext()),
                        category.getId()),
                "Category updated successfully!"));
    }

    private <T> void handleServiceCall(Call<T> serviceCall, String responseMessage) {
        serviceCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    returnToBaseFragment(responseMessage);
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
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private CreateCategoryRequestDTO createRequestDTO(View view) {
        CreateCategoryRequestDTO dto = new CreateCategoryRequestDTO();
        validateAndSet(view, R.id.category_name, R.id.category_name_layout, dto::setName);
        validateAndSet(view, R.id.category_desc, R.id.category_desc_layout, dto::setDescription);
        return dto;
    }

    private UpdateCategoryRequestDTO updateRequestDTO(View view) {
        UpdateCategoryRequestDTO dto = new UpdateCategoryRequestDTO();
        validateAndSet(view, R.id.category_name, R.id.category_name_layout, dto::setName);
        validateAndSet(view, R.id.category_desc, R.id.category_desc_layout, dto::setDescription);
        dto.setStatus(category.getStatus());
        return dto;
    }

    private void setContent() {
        nameInput.setText(category.getName());
        descriptionInput.setText(category.getDescription());
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

    private void showDeleteConfirmationDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Delete", (dialogInterface, which) ->
                        handleServiceCall(categoryService.deleteCategory
                                (JwtUtil.getAuthorizationValue(requireContext()), category.getId()),
                                "Category deleted successfully!"))
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (deleteButton != null)
                deleteButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        });
        dialog.show();
    }

    private void returnToBaseFragment(String responseMessage) {
        Toast.makeText(requireContext(), responseMessage, Toast.LENGTH_LONG).show();
        Navigation.findNavController(requireView()).navigate(R.id.navigate_back_from_category_management);
    }

}