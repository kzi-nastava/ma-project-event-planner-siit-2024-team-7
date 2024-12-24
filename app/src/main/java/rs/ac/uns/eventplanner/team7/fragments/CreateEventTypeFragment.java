package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CategorySearchAdapter;
import rs.ac.uns.eventplanner.team7.adapters.CategorySelectAdapter;
import rs.ac.uns.eventplanner.team7.dto.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;


public class CreateEventTypeFragment extends Fragment {

    private List<CategoryResponseDTO> addableCategories;
    private CategoryService categoryService;
    private CategorySearchAdapter searchAdapter;
    private CategorySelectAdapter selectAdapter;
    private String lastSearch;
    private SearchView searchView;
    private List<CategoryResponseDTO> selectedCategories;
    private static MaterialButton resetResults;

    public CreateEventTypeFragment() {
        // Required empty public constructor
    }

    public static CreateEventTypeFragment newInstance(String param1, String param2) {
        return new CreateEventTypeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedCategories = new ArrayList<>();  // Initialize the selected categories list
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event_type, container, false);

        RecyclerView searchRecyclerView = view.findViewById(R.id.search_categories_recycler_view);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RecyclerView selectedRecyclerView = view.findViewById(R.id.selected_categories_recycler_view);
        selectedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        categoryService = ClientUtils.retrofit.create(CategoryService.class);

        addableCategories = new ArrayList<>();
        selectAdapter = new CategorySelectAdapter(getContext(), selectedCategories, addableCategories);
        searchAdapter = new CategorySearchAdapter(getContext(), addableCategories, selectedCategories, selectAdapter);

        searchRecyclerView.setAdapter(searchAdapter);
        selectedRecyclerView.setAdapter(selectAdapter);

        fetchAllCategories();
        searchView = view.findViewById(R.id.category_search_view);
        setupSearchView();

        resetResults = view.findViewById(R.id.reset_search_results);
        resetResults.setOnClickListener(v -> searchAdapter.updateData(addableCategories));

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
                Log.d("Search", "Text changed: " + query);
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

    public void notifyItemRemoved() {
        if (searchAdapter != null) {
            searchAdapter.updateData(addableCategories);
            searchView.setQuery(lastSearch, true);
        }
    }
}

