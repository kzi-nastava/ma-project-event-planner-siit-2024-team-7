package rs.ac.uns.eventplanner.team7.ui.fragments.admin.categories;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.ui.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.data.model.Category;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.services.CategoryService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class AllCategoriesFragment extends Fragment implements CardClickListener {

    private final CategoryService service = ClientUtils.injectService(CategoryService.class);
    private CardRecyclerViewAdapter<Category> adapter;
    private LinearLayout content;
    private MaterialTextView welcomeMsg, loadingMsg;
    private MaterialButton switchView;
    private SearchView searchView;
    private String lastSearch;
    private boolean isActive, isLoading;

    public AllCategoriesFragment() {
        isActive = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_categories, container, false);
        content = view.findViewById(R.id.content_view);
        loadingMsg = view.findViewById(R.id.loading_msg);
        switchView = view.findViewById(R.id.switch_view);
        welcomeMsg = view.findViewById(R.id.categories_welcome_msg);
        searchView = view.findViewById(R.id.categories_search_view);
        return view;
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

        RecyclerView recyclerView = view.findViewById(R.id.categories_recycler_view);
        adapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(),
                this, false, getString(R.string.edit));
        recyclerView.setAdapter(adapter);

        setupSearchView();

        FloatingActionButton newCategoryBtn = view.findViewById(R.id.new_category);
        newCategoryBtn.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.navigate_to_category_management));

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.categories_swipe_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            handleView();
        });

        switchView.setOnClickListener(v -> {
            isActive = !isActive;
            handleView();
        });

        handleView();
    }

    private void handleView() {
        String token = AuthUtil.getAuthorizationValue(requireContext());
        if (isActive) {
            welcomeMsg.setText(R.string.all_active_categories);
            switchView.setTooltipText(getString(R.string.view_suggested));
            fetchCategories(service.findAllActive(token));
        } else {
            welcomeMsg.setText(R.string.all_pending_categories);
            switchView.setTooltipText(getString(R.string.view_active));
            fetchCategories(service.findAllPending(token));
        }
        lastSearch = "";
        searchView.setQuery("", false);
        searchView.clearFocus();
    }

    @Override
    public void onCardClicked(BasicCard entity) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("categoryDTO", (Category) entity);
        Navigation.findNavController(requireView()).navigate(R.id.navigate_to_category_management, bundle);
    }

    private void setupSearchView() {
        String token = AuthUtil.getAuthorizationValue(requireContext());
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty())
                    return true;

                if (isActive) filterCategories(service.filterActiveCategoriesByName(token, query));
                else filterCategories(service.filterPendingCategoriesByName(token, query));

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
    }
    
    private void fetchCategories(Call<List<Category>> serviceCall) {
        if (isLoading) return;
        isLoading = true;
        content.setVisibility(View.GONE);
        loadingMsg.setVisibility(View.VISIBLE);
        serviceCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call,
                                   @NonNull Response<List<Category>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    adapter.clear();
                    adapter.addAll(response.body());
                    content.setVisibility(View.VISIBLE);
                    loadingMsg.setVisibility(View.GONE);
                    isLoading = false;
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                Log.e("ERROR", "Category fetch failed", t);
                isLoading = false;
            }
        });
    }

    private void filterCategories(Call<List<Category>> serviceCall) {
        serviceCall.enqueue(new Callback<>() {
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
                Log.e("ERROR", "Category filter failed", t);
            }
        });
    }
}