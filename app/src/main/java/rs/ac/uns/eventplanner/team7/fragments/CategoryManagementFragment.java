package rs.ac.uns.eventplanner.team7.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
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
import rs.ac.uns.eventplanner.team7.dto.category.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.category.CreateCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.category.DeleteCategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.category.UpdateCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.model.enums.CategoryStatus;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class CategoryManagementFragment extends Fragment {
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private final CategoryResponseDTO categoryDTO;

    private TextInputEditText nameInput, descriptionInput;
    private MaterialButton saveButton, acceptButton, deleteButton;

    public CategoryManagementFragment(CategoryResponseDTO categoryDTO) {
        this.categoryDTO = categoryDTO;
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

        ImageView back = view.findViewById(R.id.back_button);
        back.setOnClickListener(v -> {
            AllCategoriesFragment fragment = new AllCategoriesFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_main_fragment_container, fragment)
                    .commit();
        });

        MaterialTextView title = view.findViewById(R.id.category_management_welcome);
        if (categoryDTO == null) {
            title.setText("CREATE CATEGORY");
            acceptButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
        else if (categoryDTO.getStatus() == CategoryStatus.ACTIVE) {
            title.setText("UPDATE CATEGORY");
            acceptButton.setVisibility(View.GONE);
            setContent();
        }
        else if (categoryDTO.getStatus() == CategoryStatus.PENDING) {
            title.setText("RECOMMENDED CATEGORY");
            saveButton.setVisibility(View.GONE);
            deleteButton.setText("Reject category");
            setContent();
        }

        saveButton.setOnClickListener(v -> {
            if (categoryDTO == null) {
                CreateCategoryRequestDTO dto;
                try {
                    dto = createRequestDTO(view);
                } catch (IllegalArgumentException e) {
                    Snackbar snackbar = Snackbar.make(view, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return;
                }
                categoryService.createCategory(JwtUtil.getAuthorizationValue(requireContext()), dto)
                        .enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<CategoryResponseDTO> call,
                                                   @NonNull Response<CategoryResponseDTO> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    AllCategoriesFragment fragment = new AllCategoriesFragment();
                                    Bundle args = new Bundle();
                                    args.putString("snackbar_message", "Category created successfully!");
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
                            public void onFailure(@NonNull Call<CategoryResponseDTO> call, @NonNull Throwable t) {
                                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                            }
                        });
            } else {
                UpdateCategoryRequestDTO dto;
                try {
                    dto = updateRequestDTO(view);
                } catch (IllegalArgumentException e) {
                    Snackbar snackbar = Snackbar.make(view, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    return;
                }
                categoryService.updateCategory(JwtUtil.getAuthorizationValue(requireContext()), dto, categoryDTO.getId())
                        .enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<CategoryResponseDTO> call,
                                                   @NonNull Response<CategoryResponseDTO> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    AllCategoriesFragment fragment = new AllCategoriesFragment();
                                    Bundle args = new Bundle();
                                    args.putString("snackbar_message", "Category updated successfully!");
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
                            public void onFailure(@NonNull Call<CategoryResponseDTO> call, @NonNull Throwable t) {
                                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                            }
                        });
            }
        });

        deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        acceptButton.setOnClickListener(v -> {
            categoryService.acceptRecommendedCategory(JwtUtil.getAuthorizationValue(requireContext()), Objects.requireNonNull(categoryDTO).getId())
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<CategoryResponseDTO> call,
                                               @NonNull Response<CategoryResponseDTO> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                AllCategoriesFragment fragment = new AllCategoriesFragment();
                                Bundle args = new Bundle();
                                args.putString("snackbar_message", "Category updated successfully!");
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
                        public void onFailure(@NonNull Call<CategoryResponseDTO> call, @NonNull Throwable t) {
                            Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                        }
                    });
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
        dto.setStatus(categoryDTO.getStatus());
        return dto;
    }

    private void setContent() {
        nameInput.setText(categoryDTO.getName());
        descriptionInput.setText(categoryDTO.getDescription());
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
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this category?")
                .setPositiveButton("Delete", (dialogInterface, which) -> {

                    categoryService.deleteCategory(JwtUtil.getAuthorizationValue(requireContext()), categoryDTO.getId())
                            .enqueue(new Callback<>() {
                                @Override
                                public void onResponse(@NonNull Call<DeleteCategoryResponseDTO> call,
                                                       @NonNull Response<DeleteCategoryResponseDTO> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        AllCategoriesFragment fragment = new AllCategoriesFragment();
                                        Bundle args = new Bundle();
                                        args.putString("snackbar_message", "Category deleted successfully!");
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
                                public void onFailure(@NonNull Call<DeleteCategoryResponseDTO> call, @NonNull Throwable t) {
                                    Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (deleteButton != null)
                deleteButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        });
        dialog.show();
    }

}