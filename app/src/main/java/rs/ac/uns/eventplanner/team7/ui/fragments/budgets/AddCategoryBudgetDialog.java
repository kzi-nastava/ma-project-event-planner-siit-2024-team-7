package rs.ac.uns.eventplanner.team7.ui.fragments.budgets;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.budget.AddCategoryBudgetRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.CategoryBudgetResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.EventBudgetResponseDTO;

import rs.ac.uns.eventplanner.team7.data.model.Category;
import rs.ac.uns.eventplanner.team7.data.services.BudgetService;
import rs.ac.uns.eventplanner.team7.data.services.CategoryService;
import rs.ac.uns.eventplanner.team7.ui.fragments.MaterialDialogFragment;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class AddCategoryBudgetDialog extends MaterialDialogFragment {

    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private final BudgetService budgetService = ClientUtils.injectService(BudgetService.class);

    private ArrayAdapter<Category> categoriesAdapter;
    private final List<Category> categories;
    private final List<Category> usedCategories;
    private EventBudgetResponseDTO eventBudgetDTO;

    public AddCategoryBudgetDialog() {
        categories = new ArrayList<>();
        usedCategories = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventBudgetDTO = getArguments().getParcelable("eventBudgetDTO", EventBudgetResponseDTO.class);
            if (eventBudgetDTO == null) return;
            for (CategoryBudgetResponseDTO dto : eventBudgetDTO.getCategoryBudgets())
                usedCategories.add(dto.getCategory());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_category_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AutoCompleteTextView categoryDropdown = view.findViewById(R.id.add_category_budget_dropdown);
        TextInputEditText budgetInput = view.findViewById(R.id.budget);
        categoriesAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                categories
        );
        categoryDropdown.setAdapter(categoriesAdapter);

        fetchCategories();

        MaterialButton cancel = view.findViewById(R.id.cancel_btn);
        cancel.setOnClickListener(v -> dismiss());

        MaterialButton submit = view.findViewById(R.id.submit_btn);
        submit.setOnClickListener(v -> {
            AddCategoryBudgetRequestDTO dto = new AddCategoryBudgetRequestDTO();
            String selectedCategoryName = categoryDropdown.getText().toString();
            for (Category category : categories) {
                if (category.getName().equals(selectedCategoryName))
                    dto.setCategory(category);
            }
            if (dto.getCategory() == null || budgetInput.getText() == null || budgetInput.getText().toString().isEmpty()) {
                Log.d("ERROR", "Must provide a category and a budget!");
                Snackbar.make(view, "Must provide a category and a budget!", BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }
            dto.setBudget(Double.parseDouble(budgetInput.getText().toString()));

            budgetService.addCategoryBudget(AuthUtil.getAuthorizationValue(requireContext()), eventBudgetDTO.getEventBudgetId(), dto)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<EventBudgetResponseDTO> call,
                                               @NonNull Response<EventBudgetResponseDTO> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful() && response.body() != null) {
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
                        public void onFailure(@NonNull Call<EventBudgetResponseDTO> call, @NonNull Throwable t) {
                            Log.e("ERROR", "Add failed", t);
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
                            for (Category category : response.body()) {
                                boolean exists = false;
                                for (Category c : usedCategories) {
                                    if (c.getId().equals(category.getId())) {
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists) {
                                    categoriesAdapter.add(category);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Category>> call,
                                          @NonNull Throwable t) {
                        Log.e("ERROR", "Request failed", t);
                    }
                });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        getParentFragmentManager().setFragmentResult("addCategoryBudgetDismissed", new Bundle());
    }
}