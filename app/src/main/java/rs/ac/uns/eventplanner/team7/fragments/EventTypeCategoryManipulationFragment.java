package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CategorySearchAdapter;
import rs.ac.uns.eventplanner.team7.adapters.CategorySelectAdapter;
import rs.ac.uns.eventplanner.team7.dto.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class EventTypeCategoryManipulationFragment extends Fragment {

    private List<CategoryResponseDTO> addableCategories;
    @Getter
    private List<CategoryResponseDTO> selectedCategories;
    private List<CategoryResponseDTO> originalCategories;
    private CategorySearchAdapter searchAdapter;
    private CategorySelectAdapter selectAdapter;
    private String lastSearch;
    private SearchView searchView;
    private final CategoryService categoryService = ClientUtils.injectService(CategoryService.class);

    public interface CategorySelectionListener {
        void onCategoriesSelected(List<CategoryResponseDTO> selectedCategories);
    }

    private CategorySelectionListener listener;

    public interface CategoriesFetchedListener {
        void onCategoriesFetched(List<CategoryResponseDTO> categories);
    }

    @Setter
    private CategoriesFetchedListener categoriesFetchedListener;

    public EventTypeCategoryManipulationFragment() {
        // Required empty public constructor
    }

    public static EventTypeCategoryManipulationFragment newInstance() {
        return new EventTypeCategoryManipulationFragment();
    }


    public void setCategorySelectionListener(CategorySelectionListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_type_category_manipulation, container, false);

        RecyclerView searchRecyclerView = view.findViewById(R.id.search_categories_recycler_view);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RecyclerView selectedRecyclerView = view.findViewById(R.id.selected_categories_recycler_view);
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchRecyclerView.setHasFixedSize(true);
        selectedRecyclerView.setHasFixedSize(true);

        addableCategories = new ArrayList<>();
        selectedCategories = new ArrayList<>();

        selectAdapter = new CategorySelectAdapter(getContext(), selectedCategories, addableCategories);
        searchAdapter = new CategorySearchAdapter(getContext(), addableCategories, selectedCategories, selectAdapter);

        searchRecyclerView.setAdapter(searchAdapter);
        selectedRecyclerView.setAdapter(selectAdapter);

        fetchAllCategories();
        searchView = view.findViewById(R.id.category_search_view);
        setupSearchView();

        MaterialButton resetResults = view.findViewById(R.id.reset_search_results);
        resetResults.setOnClickListener(v -> {

            addableCategories = originalCategories.stream()
                    .filter(category -> selectedCategories.stream()
                            .noneMatch(selected -> selected.getId().equals(category.getId())))
                    .collect(Collectors.toList());
            searchAdapter.updateData(addableCategories);
        });

        return view;
    }

    private void fetchAllCategories() {
        Call<List<CategoryResponseDTO>> call = categoryService.getAll(JwtUtil.getAuthorizationValue(requireContext()));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<CategoryResponseDTO>> call, @NonNull Response<List<CategoryResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchAdapter.updateData(response.body());
                    addableCategories = response.body();
                    originalCategories = response.body();
                    if (categoriesFetchedListener != null) {
                        categoriesFetchedListener.onCategoriesFetched(addableCategories);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CategoryResponseDTO>> call, @NonNull Throwable t) {
                // Handle failure
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<CategoryResponseDTO> filteredCategories = new ArrayList<>();
                if (query.isEmpty()) {
                    return true;
                }
                for (var category : addableCategories) {
                    if (category.getName().toLowerCase().contains(query.toLowerCase())) {
                        filteredCategories.add(category);
                    }
                }
                searchView.clearFocus();
                searchAdapter.updateData(filteredCategories);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals(lastSearch)) {
                    onQueryTextSubmit(lastSearch);
                }
                lastSearch = newText;
                return true;
            }
        });
    }

    public void notifyChange() {
        searchAdapter.notifyDataSetChanged();
    }

    public void notifyChange(List<CategoryResponseDTO> selectedCategories) {
        selectAdapter.updateData(selectedCategories);
        List<CategoryResponseDTO> filteredAddableCategories = addableCategories.stream()
                .filter(category -> selectedCategories.stream()
                        .noneMatch(selected -> selected.getId().equals(category.getId())))
                .collect(Collectors.toList());

        searchAdapter.updateData(filteredAddableCategories);
    }
}
