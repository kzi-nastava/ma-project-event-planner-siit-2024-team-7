package rs.ac.uns.eventplanner.team7.ui.fragments.admin.categories;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.FragmentKt;

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
import rs.ac.uns.eventplanner.team7.data.dto.category.DeleteCategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.category.RejectCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.ui.fragments.MaterialDialogFragment;
import rs.ac.uns.eventplanner.team7.data.model.Category;
import rs.ac.uns.eventplanner.team7.data.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class RejectCategoryDialogFragment extends MaterialDialogFragment {
    private Category category;
    private final List<Category> categories;
    private ArrayAdapter<Category> categoriesAdapter;
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);

    public RejectCategoryDialogFragment() {
        this.categories = new ArrayList<>();
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            category = getArguments().getParcelable("category", Category.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_reject_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoCompleteTextView categoryDropdown = view.findViewById(R.id.replacement_category_dropdown);
        categoriesAdapter = new ArrayAdapter<>(
                requireContext(),
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
            String selectedCategoryName = categoryDropdown.getText().toString();
            for (Category category : categories) {
                if (category.getName().equals(selectedCategoryName))
                    dto.setReplacementCategoryId(category.getId());
            }
            if (dto.getReplacementCategoryId() == null) {
                Log.d("ERROR", "Must choose a replacement category!");
                Snackbar.make(view, "Must choose a replacement category!", BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }

            categoryService.rejectCategory(AuthUtil.getAuthorizationValue(requireContext()), category.getId(), dto)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<DeleteCategoryResponseDTO> call,
                                           @NonNull Response<DeleteCategoryResponseDTO> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            Bundle args = new Bundle();
                            args.putString("snackbar_message", "Category rejected successfully!");
                            returnToBaseFragment(args);
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
                        String message = t.getMessage();
                        Bundle args = new Bundle();
                        if (message != null) {
                            Log.d("ERROR", message);
                            args.putString("snackbar_message", message);
                        }
                        returnToBaseFragment(args);
                    }
                });
        });

    }

    private void fetchCategories() {
        categoryService.findAllActive(AuthUtil.getAuthorizationValue(requireContext()))
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

    private void returnToBaseFragment(Bundle args) {
        FragmentKt.findNavController(RejectCategoryDialogFragment.this)
                .navigate(R.id.navigate_back_from_reject_category, args);
    }
}

