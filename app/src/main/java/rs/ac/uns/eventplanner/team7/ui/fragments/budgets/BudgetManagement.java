package rs.ac.uns.eventplanner.team7.ui.fragments.budgets;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import rs.ac.uns.eventplanner.team7.ui.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.data.dto.budget.CategoryBudgetResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.EventBudgetResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.UpdateCategoryBudgetRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.services.BudgetService;
import rs.ac.uns.eventplanner.team7.data.services.ProductService;
import rs.ac.uns.eventplanner.team7.data.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class BudgetManagement extends Fragment implements CardClickListener {

    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final ProductService productService = ClientUtils.injectService(ProductService.class);
    private final BudgetService budgetService = ClientUtils.injectService(BudgetService.class);

    private EventBudgetResponseDTO eventBudgetDTO;
    private CategoryBudgetResponseDTO categoryBudgetDTO;
    private ArrayAdapter<CategoryBudgetResponseDTO> categoryBudgetAdapter;
    private CardRecyclerViewAdapter<BasicItemDTO> reservedServicesAdapter, purchasedProductsAdapter;
    private final List<BasicItemDTO> services = new ArrayList<>();
    private final List<BasicItemDTO> products = new ArrayList<>();

    private MaterialTextView totalBudgetView, totalSpentView, noBudgetData, categorySpent;
    private TextInputEditText categoryBudget;
    private RecyclerView reservedServicesView, purchasedProductsView;
    private AutoCompleteTextView categoryBudgetDropdown;
    private LinearLayout purchasedReservedItems;
    private MaterialButton addCategoryBtn, saveBudgetBtn, removeCategoryBudgetBtn;

    public BudgetManagement() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventBudgetDTO = getArguments().getParcelable("eventBudget", EventBudgetResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget_management, container, false);
        totalBudgetView = view.findViewById(R.id.total_budget_text_view);
        totalSpentView = view.findViewById(R.id.total_spent_text_view);
        noBudgetData = view.findViewById(R.id.no_budget_data);
        categorySpent = view.findViewById(R.id.category_spent);
        categoryBudget = view.findViewById(R.id.category_budget);
        reservedServicesView = view.findViewById(R.id.recycler_view_reserved_services);
        purchasedProductsView = view.findViewById(R.id.recycler_view_purchased_products);
        purchasedReservedItems = view.findViewById(R.id.purchased_reserved_item_layout);
        categoryBudgetDropdown = view.findViewById(R.id.category_budgets_dropdown);
        addCategoryBtn = view.findViewById(R.id.add_category_budget_btn);
        saveBudgetBtn = view.findViewById(R.id.save_budget_btn);
        removeCategoryBudgetBtn = view.findViewById(R.id.remove_category_budget_btn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        categoryBudgetAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                new ArrayList<>(eventBudgetDTO.getCategoryBudgets())
        );
        categoryBudgetDropdown.setAdapter(categoryBudgetAdapter);
        categoryBudgetDropdown.setOnItemClickListener((parent, v, position, id) -> {
            categoryBudgetDTO = (CategoryBudgetResponseDTO) parent.getItemAtPosition(position);

            reservedServicesAdapter.clear();
            reservedServicesAdapter.addAll(categoryBudgetDTO.getItems().stream().filter(i -> Objects.equals(i.getType(), "services")).toList());
            purchasedProductsAdapter.clear();
            purchasedProductsAdapter.addAll(categoryBudgetDTO.getItems().stream().filter(i -> Objects.equals(i.getType(), "products")).toList());

            categoryBudget.setText(String.valueOf(categoryBudgetDTO.getBudget()));
            categorySpent.setText("SPENT: " + categoryBudgetDTO.getSpent() + " $");
            categoryBudget.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    saveBudgetBtn.setVisibility(s.toString().trim().isEmpty() ? View.GONE : View.VISIBLE);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            if (services.isEmpty() && products.isEmpty()) {
                noBudgetData.setVisibility(View.VISIBLE);
                noBudgetData.setText("No item was purchased/reserved for this event!");
                purchasedReservedItems.setVisibility(View.VISIBLE);
            }
            else {
                noBudgetData.setVisibility(View.GONE);
                purchasedReservedItems.setVisibility(View.VISIBLE);
            }
        });

        Drawable drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.baseline_more_horiz_24);
        reservedServicesAdapter = new CardRecyclerViewAdapter<>(requireContext(), services, this, drawable);
        reservedServicesView.setAdapter(reservedServicesAdapter);
        purchasedProductsAdapter = new CardRecyclerViewAdapter<>(requireContext(), products, this, drawable);
        purchasedProductsView.setAdapter(purchasedProductsAdapter);

        addCategoryBtn.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putParcelable("eventBudgetDTO", eventBudgetDTO);
            Navigation.findNavController(v).navigate(R.id.navigate_to_add_category_budget_dialog, args);
        });

        saveBudgetBtn.setOnClickListener(v -> {
            if (categoryBudget.getText() == null || categoryBudget.getText().toString().isEmpty()) {
                Log.d("ERROR", "Budget must be provided!");
                Snackbar.make(view, "Budget must be provided!", BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }
            if (eventBudgetDTO == null || categoryBudgetDTO == null) {
                Log.d("ERROR", "Budget is null!");
                Snackbar.make(view, "Budget is null!", BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }
            UpdateCategoryBudgetRequestDTO dto = new UpdateCategoryBudgetRequestDTO(Double.parseDouble(categoryBudget.getText().toString()));
            updateBudget(dto);
        });

        removeCategoryBudgetBtn.setOnClickListener(v -> {
            if (!services.isEmpty() || !products.isEmpty()) {
                Log.d("ERROR", "Can't delete budget if items are already added!");
                Snackbar.make(view, "Can't delete budget if items are already added!", BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }
            if (eventBudgetDTO == null || categoryBudgetDTO == null) {
                Log.d("ERROR", "Budget is null!");
                Snackbar.make(view, "Budget is null!", BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }
            showDeleteConfirmationDialog();
        });

        refreshContent();

        getParentFragmentManager().setFragmentResultListener("addCategoryBudgetDismissed", this, (requestKey, result) -> refreshContent());
    }

    private void refreshContent() {
        budgetService.getEventBudget(AuthUtil.getAuthorizationValue(requireContext()), eventBudgetDTO.getEventBudgetId())
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<EventBudgetResponseDTO> call,
                                           @NonNull Response<EventBudgetResponseDTO> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            eventBudgetDTO = response.body();
                            categoryBudgetDTO = null;
                            categoryBudgetDropdown.setText("", false);
                            categoryBudgetAdapter.clear();
                            categoryBudgetAdapter.addAll(eventBudgetDTO.getCategoryBudgets());
                            reservedServicesAdapter.clear();
                            purchasedProductsAdapter.clear();

                            totalBudgetView.setText("TOTAL BUDGET: " + eventBudgetDTO.getTotalBudget() + " $");
                            totalSpentView.setText("TOTAL SPENT: " + eventBudgetDTO.getTotalSpent() + " $");
                            noBudgetData.setVisibility(View.VISIBLE);
                            noBudgetData.setText("No category was selected!");
                            purchasedReservedItems.setVisibility(View.GONE);
                            saveBudgetBtn.setVisibility(View.GONE);
                            return;
                        }
                        try {
                            // Show error message
                            if (response.errorBody() == null) return;
                            String errorBody = response.errorBody().string();
                            JSONObject jsonObject = new JSONObject(errorBody);
                            jsonObject.getString("message");
                        } catch (Exception e) {
                            String message = e.getMessage();
                            if (message == null) return;
                            Log.d("ERROR", message);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<EventBudgetResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }

    @Override
    public void onCardClicked(BasicCard entity) {
        final String token = AuthUtil.getAuthorizationValue(requireContext());
        if (((BasicItemDTO)entity).getType().equals("services")) {
            serviceService.getService(token, entity.getId()).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetServiceResponseDTO> call,
                                       @NonNull Response<GetServiceResponseDTO> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Bundle args = new Bundle();
                        args.putParcelable("serviceDTO", response.body());
                        categoryBudgetDropdown.setText("", false);
                        View view = getView();
                        if (view == null) return;
                        Navigation.findNavController(view).navigate(R.id.navigate_to_service_details_from_budget, args);
                        return;
                    }
                    try {
                        // Show error message
                        if (response.errorBody() == null) return;
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        jsonObject.getString("message");
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if (message == null) return;
                        Log.d("ERROR", message);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetServiceResponseDTO> call, @NonNull Throwable t) {
                    Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                }
            });
        } else {
            productService.getProduct(token, entity.getId()).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetProductResponseDTO> call,
                                       @NonNull Response<GetProductResponseDTO> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Bundle args = new Bundle();
                        args.putParcelable("productDTO", response.body());
                        categoryBudgetDropdown.setText("", false);
                        View view = getView();
                        if (view == null) return;
                        Navigation.findNavController(view).navigate(R.id.navigate_to_product_details_from_budget, args);
                        return;
                    }
                    try {
                        if (response.errorBody() == null) return;
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        jsonObject.getString("message");
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if (message == null) return;
                        Log.d("ERROR", message);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetProductResponseDTO> call, @NonNull Throwable t) {
                    Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                }
            });
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this category budget?")
                .setPositiveButton("Delete", (d, which) -> deleteCategoryBudget())
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button deleteButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (deleteButton != null) {
                deleteButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            }
        });
        dialog.show();
    }

    private void deleteCategoryBudget() {
        budgetService.removeCategoryBudget(AuthUtil.getAuthorizationValue(requireContext()), eventBudgetDTO.getEventBudgetId(), categoryBudgetDTO.getCategoryBudgetId())
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<EventBudgetResponseDTO> call,
                                           @NonNull Response<EventBudgetResponseDTO> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            refreshContent();
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
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }

    private void updateBudget(UpdateCategoryBudgetRequestDTO dto) {
        budgetService.updateCategoryBudget(AuthUtil.getAuthorizationValue(requireContext()),
                eventBudgetDTO.getEventBudgetId(), categoryBudgetDTO.getCategoryBudgetId(), dto)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<CategoryBudgetResponseDTO> call,
                                           @NonNull Response<CategoryBudgetResponseDTO> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            refreshContent();
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
                    public void onFailure(@NonNull Call<CategoryBudgetResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }
}