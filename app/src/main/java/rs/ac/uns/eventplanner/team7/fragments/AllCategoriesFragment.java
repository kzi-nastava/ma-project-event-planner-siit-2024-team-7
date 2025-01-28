package rs.ac.uns.eventplanner.team7.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CategoryCardAdapter;
import rs.ac.uns.eventplanner.team7.dto.category.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class AllCategoriesFragment extends Fragment {
    private CategoryCardAdapter adapter;
    private final List<CategoryResponseDTO> categories;
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);
    private String lastSearch;
    private boolean isActive;

    public AllCategoriesFragment() {
        categories = new ArrayList<>();
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

        LinearLayout content = view.findViewById(R.id.content_view);
        MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
        MaterialButton newCategoryBtn = view.findViewById(R.id.new_category);
        MaterialButton activeCategoriesBtn = view.findViewById(R.id.active_categories);
        MaterialButton suggestedCategoriesBtn = view.findViewById(R.id.suggested_categories);
        MaterialTextView welcomeMsg = view.findViewById(R.id.categories_welcome_msg);

        newCategoryBtn.setOnClickListener(v -> {
            CategoryManagementFragment fragment = new CategoryManagementFragment(null);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.home_main_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

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

        RecyclerView recyclerView = view.findViewById(R.id.categories_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CategoryCardAdapter(requireContext(), categories);
        recyclerView.setAdapter(adapter);

        content.setVisibility(View.GONE);
        loadingMsg.setVisibility(View.VISIBLE);

        fetchActiveCategories(view);

        setupSearchView(view);
    }

    private void fetchActiveCategories(View view) {
        Call<List<CategoryResponseDTO>> call = categoryService.findAllActive(JwtUtil.getAuthorizationValue(requireContext()));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<CategoryResponseDTO>> call,
                                   @NonNull Response<List<CategoryResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    LinearLayout content = view.findViewById(R.id.content_view);
                    MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
                    content.setVisibility(View.VISIBLE);
                    loadingMsg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CategoryResponseDTO>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void fetchPendingCategories(View view) {
        Call<List<CategoryResponseDTO>> call = categoryService.findAllPending(JwtUtil.getAuthorizationValue(requireContext()));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<CategoryResponseDTO>> call,
                                   @NonNull Response<List<CategoryResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories.clear();
                    categories.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    LinearLayout content = view.findViewById(R.id.content_view);
                    MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
                    content.setVisibility(View.VISIBLE);
                    loadingMsg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CategoryResponseDTO>> call, @NonNull Throwable t) {
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
            categories.clear();
            if (isActive) fetchActiveCategories(view);
            else fetchPendingCategories(view);
        });
    }

    private void filterActiveCategories(String query) {
        categoryService.filterActiveCategoriesByName(JwtUtil.getAuthorizationValue(requireContext()), query)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CategoryResponseDTO>> call,
                                           @NonNull Response<List<CategoryResponseDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            categories.clear();
                            categories.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<CategoryResponseDTO>> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }

    private void filterPendingCategories(String query) {
        categoryService.filterPendingCategoriesByName(JwtUtil.getAuthorizationValue(requireContext()), query)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<CategoryResponseDTO>> call,
                                           @NonNull Response<List<CategoryResponseDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            categories.clear();
                            categories.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<CategoryResponseDTO>> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }
}