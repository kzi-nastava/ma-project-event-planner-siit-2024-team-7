package rs.ac.uns.eventplanner.team7.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.category.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.category.DeleteCategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.category.RejectCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class RejectCategoryFragment extends MaterialDialogFragment {
    private final Context context;
    private final CategoryResponseDTO categoryDTO;

    private List<CategoryResponseDTO> categories;
    private AutoCompleteTextView categoryDropdown;
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);

    public RejectCategoryFragment(Context context, CategoryResponseDTO categoryDTO) {
        this.context = context;
        this.categories = new ArrayList<>();
        this.categoryDTO = categoryDTO;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reject_category, container, false);

        categoryDropdown = view.findViewById(R.id.replacement_category_dropdown);
        fetchCategories();

        MaterialButton cancel = view.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(v -> {
            dismiss();
        });

        MaterialButton reject = view.findViewById(R.id.reject_category_btn);
        reject.setOnClickListener(v -> {
            RejectCategoryRequestDTO dto = new RejectCategoryRequestDTO();
            for (CategoryResponseDTO categoryDTO : categories) {
                if (categoryDTO.getName().equals(categoryDropdown.getText().toString()))
                    dto.setReplacementCategoryId(categoryDTO.getId());
            }
            if (dto.getReplacementCategoryId() == null) {
                Log.d("ERROR", "Must choose a replacement category!");
                Snackbar.make(view, "Must choose a replacement category!", BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }

            categoryService.rejectCategory(JwtUtil.getAuthorizationValue(requireContext()), categoryDTO.getId(), dto)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<DeleteCategoryResponseDTO> call,
                                               @NonNull Response<DeleteCategoryResponseDTO> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                dismiss();
                                AllCategoriesFragment fragment = new AllCategoriesFragment();
                                Bundle args = new Bundle();
                                args.putString("snackbar_message", "Category rejected successfully!");
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
        });

        return view;
    }

    private void fetchCategories() {
        categoryService.findAllActive(JwtUtil.getAuthorizationValue(context))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CategoryResponseDTO>> call,
                                           @NonNull Response<List<CategoryResponseDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            categories = new ArrayList<>(response.body());

                            ArrayAdapter<CategoryResponseDTO> adapter = new ArrayAdapter<>(
                                    context,
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
}

