package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.category.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.category.CreateCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class CategoryRecommendationFragment extends DialogFragment {
    private final List<CategoryResponseDTO> categories;
    private final AutoCompleteTextView categoryDropdown;
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);

    public CategoryRecommendationFragment(List<CategoryResponseDTO> categories,
                                          AutoCompleteTextView categoryDropdown) {
        this.categories = categories;
        this.categoryDropdown = categoryDropdown;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_recommendation, container, false);

        TextInputEditText nameInput = view.findViewById(R.id.recommended_category_name);
        TextInputEditText descriptionInput = view.findViewById(R.id.recommended_category_description);

        MaterialButton submitButton = view.findViewById(R.id.button_submit);
        submitButton.setOnClickListener(v -> {
            String name = Objects.requireNonNull(nameInput.getText()).toString();
            String description = Objects.requireNonNull(descriptionInput.getText()).toString();

            if (!name.isEmpty() && !description.isEmpty()) {
                categoryService.createCategory(
                        JwtUtil.getAuthorizationValue(getContext()),
                        new CreateCategoryRequestDTO(name, description))
                        .enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<CategoryResponseDTO> call,
                                                   @NonNull Response<CategoryResponseDTO> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    categories.add(response.body());
                                    categoryDropdown.setText(response.body().toString(), false);
                                    dismiss();
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
                            public void onFailure(@NonNull Call<CategoryResponseDTO> call,
                                                  @NonNull Throwable t) {
                                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                            }
                        });
            }
        });

        MaterialButton cancelButton = view.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(v -> {
            dismiss();
        });

        return view;
    }
}