package rs.ac.uns.eventplanner.team7.fragments.admin.categories;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class AllCategoriesFragment extends Fragment implements CardClickListener {

    private CardRecyclerViewAdapter<Category> adapter;
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private String lastSearch;
    private boolean isActive;

    public AllCategoriesFragment() {
        isActive = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            Bundle args = getArguments();
            String snackbarMessage = args.getString("snackbar_message");
            if (snackbarMessage != null) {
                Snackbar snackbar = Snackbar.make(view, snackbarMessage, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("OK", v -> snackbar.dismiss()).show();
                args.clear(); // Display this message only once!
            }
        }

        LinearLayout content = view.findViewById(R.id.content_view);
        MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
        MaterialButton newCategoryBtn = view.findViewById(R.id.new_category);
        MaterialButton activeCategoriesBtn = view.findViewById(R.id.active_categories);
        MaterialButton suggestedCategoriesBtn = view.findViewById(R.id.suggested_categories);
        MaterialTextView welcomeMsg = view.findViewById(R.id.categories_welcome_msg);

        RecyclerView recyclerView = view.findViewById(R.id.categories_recycler_view);
        adapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(), this, false, getString(R.string.edit));
        recyclerView.setAdapter(adapter);

        newCategoryBtn.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.navigate_to_category_management));

        welcomeMsg.setText(R.string.all_active_categories);
        activeCategoriesBtn.setVisibility(View.GONE);
        activeCategoriesBtn.setOnClickListener(v -> {
            suggestedCategoriesBtn.setVisibility(View.VISIBLE);
            activeCategoriesBtn.setVisibility(View.GONE);
            isActive = true;

            welcomeMsg.setText(R.string.all_active_categories);
            content.setVisibility(View.GONE);
            loadingMsg.setVisibility(View.VISIBLE);

            fetchActiveCategories(view);

        });
        suggestedCategoriesBtn.setOnClickListener(v -> {
            activeCategoriesBtn.setVisibility(View.VISIBLE);
            suggestedCategoriesBtn.setVisibility(View.GONE);
            isActive = false;

            welcomeMsg.setText(R.string.all_pending_categories);
            content.setVisibility(View.GONE);
            loadingMsg.setVisibility(View.VISIBLE);

            fetchPendingCategories(view);
        });

        content.setVisibility(View.GONE);
        loadingMsg.setVisibility(View.VISIBLE);

        fetchActiveCategories(view);

        setupSearchView(view);
    }

    @Override
    public void onCardClicked(BasicCard entity) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("categoryDTO", (Category) entity);
        Navigation.findNavController(requireView()).navigate(R.id.navigate_to_category_management, bundle);
    }

    private void fetchActiveCategories(View view) {
        Call<List<Category>> call = categoryService.findAllActive(JwtUtil.getAuthorizationValue(requireContext()));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call,
                                   @NonNull Response<List<Category>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    adapter.clear();
                    adapter.addAll(response.body());

                    LinearLayout content = view.findViewById(R.id.content_view);
                    MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
                    content.setVisibility(View.VISIBLE);
                    loadingMsg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void fetchPendingCategories(View view) {
        Call<List<Category>> call = categoryService.findAllPending(JwtUtil.getAuthorizationValue(requireContext()));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call,
                                   @NonNull Response<List<Category>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    adapter.clear();
                    adapter.addAll(response.body());

                    LinearLayout content = view.findViewById(R.id.content_view);
                    MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
                    content.setVisibility(View.VISIBLE);
                    loadingMsg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void setupSearchView(View view) {
        SearchView searchView = view.findViewById(R.id.categories_search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty())
                    return true;

                if (isActive) filterActiveCategories(query);
                else filterPendingCategories(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(lastSearch))
                    onQueryTextSubmit(newText);
                lastSearch = newText;
                return true;

            }
        });

        MaterialButton reset = view.findViewById(R.id.reset_category_search);
        reset.setOnClickListener(v -> {
            lastSearch = "";
            searchView.setQuery("", false);
            searchView.clearFocus();
            adapter.clear();
            if (isActive) fetchActiveCategories(view);
            else fetchPendingCategories(view);
        });
    }

    private void filterActiveCategories(String query) {
        categoryService.filterActiveCategoriesByName(JwtUtil.getAuthorizationValue(requireContext()), query)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Category>> call,
                                           @NonNull Response<List<Category>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.clear();
                            adapter.addAll(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }

    private void filterPendingCategories(String query) {
        categoryService.filterPendingCategoriesByName(JwtUtil.getAuthorizationValue(requireContext()), query)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Category>> call,
                                           @NonNull Response<List<Category>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.clear();
                            adapter.addAll(response.body());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }
}