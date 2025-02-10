package rs.ac.uns.eventplanner.team7.fragments.admin.categories;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

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
import rs.ac.uns.eventplanner.team7.dto.category.DeleteCategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.category.RejectCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.fragments.MaterialDialogFragment;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class RejectCategoryDialogFragment extends MaterialDialogFragment {
    private Context context;
    private Category category;
    private final List<Category> categories;
    private AutoCompleteTextView categoryDropdown;
    private ArrayAdapter<Category> categoriesAdapter;
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);

    public RejectCategoryDialogFragment() {
        this.categories = new ArrayList<>();
    }

    public static RejectCategoryDialogFragment newInstance(Context context, Category category) {
        RejectCategoryDialogFragment fragment = new RejectCategoryDialogFragment();
        fragment.context = context;
        fragment.category = category;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_reject_category, container, false);
        categoryDropdown = view.findViewById(R.id.replacement_category_dropdown);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoriesAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_list_item_1,
                categories
        );
        categoryDropdown.setAdapter(categoriesAdapter);

        fetchCategories();

        MaterialButton cancel = view.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(v -> dismiss());

        MaterialButton reject = view.findViewById(R.id.reject_category_btn);
        reject.setOnClickListener(v -> {
            RejectCategoryRequestDTO dto = new RejectCategoryRequestDTO();
            for (Category category : categories) {
                if (category.getName().equals(categoryDropdown.getText().toString()))
                    dto.setReplacementCategoryId(category.getId());
            }
            if (dto.getReplacementCategoryId() == null) {
                Log.d("ERROR", "Must choose a replacement category!");
                Snackbar.make(view, "Must choose a replacement category!", BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }

            categoryService.rejectCategory(JwtUtil.getAuthorizationValue(requireContext()), category.getId(), dto)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<DeleteCategoryResponseDTO> call,
                                               @NonNull Response<DeleteCategoryResponseDTO> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful() && response.body() != null) {
                                Bundle args = new Bundle();
                                args.putString("snackbar_message", "Category rejected successfully!");
                                Navigation.findNavController(requireView()).navigate(R.id.navigate_back_from_reject_category, args);
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
                        public void onFailure(@NonNull Call<DeleteCategoryResponseDTO> call, @NonNull Throwable t) {
                            Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                        }
                    });
        });

    }

    private void fetchCategories() {
        categoryService.findAllActive(JwtUtil.getAuthorizationValue(context))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Category>> call,
                                           @NonNull Response<List<Category>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            categoriesAdapter.clear();
                            categoriesAdapter.addAll(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Category>> call,
                                          @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }
}

