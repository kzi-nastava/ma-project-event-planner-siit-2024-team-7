package rs.ac.uns.eventplanner.team7.ui.fragments.products;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.ui.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.Sort;
import rs.ac.uns.eventplanner.team7.data.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.data.services.ProductService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class ProductList extends Fragment implements SearchActionsListener, CardClickListener {
    private final ProductService productService = ClientUtils.injectService(ProductService.class);

    private final ProductFilterFragment filterFragment;
    private final Page<BasicItemDTO> page;
    private Map<String, String> latestFilters;
    private String lastSearch;
    private SearchView searchView;
    private MaterialTextView messageView;
    private RecyclerView productsView;
    private CardRecyclerViewAdapter<BasicItemDTO> viewAdapter;
    private boolean isLoading;

    public ProductList() {
        page = Page.getDefault();
        filterFragment = new ProductFilterFragment(this);
        latestFilters = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        messageView = view.findViewById(R.id.products_search_result_message_view);
        searchView = view.findViewById(R.id.products_search_view);
        productsView = view.findViewById(R.id.products_recycler_view);
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(), this);
        productsView.setAdapter(viewAdapter);

        FloatingActionButton createProductButton = view.findViewById(R.id.add_product_fab);
        createProductButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(R.id.navigate_to_create_product);
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setContent(false);
        setupSearchView();

        MaterialButton filtersButton = view.findViewById(R.id.product_filters_button);
        filtersButton.setOnClickListener(v -> {
            filtersButton.setEnabled(false);
            if (getChildFragmentManager().findFragmentByTag("productFilters") != null) return;
            filterFragment.show(getChildFragmentManager(), "productFilters");
            getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
                    if (f == filterFragment) {
                        filtersButton.setEnabled(true);
                        getChildFragmentManager().unregisterFragmentLifecycleCallbacks(this);
                    }
                }
            }, false);
        });

        setupContentScrollListener();
    }

    @Override
    public void onSortApplied() {
        page.resetToDefault();
        page.setSort(new Sort().by(""));
        setContent(false);
    }

    @Override
    public void onNextPage() {
        if (isLoading || page.isLast()) return;
        page.nextPage();
        setContent(true);
    }

    @Override
    public void onFiltersApplied() {
        page.resetToDefault();
        String query = latestFilters.get("name");
        if (query == null) query = "";
        latestFilters = filterFragment.getFilters();
        latestFilters.put("name", query);
        setContent(false);
    }

    @Override
    public void onFiltersReset() {
        page.resetToDefault();
        latestFilters = new HashMap<>();
        searchView.setQuery("", false);
        searchView.clearFocus();
        setContent(false);
    }

    @Override
    public void onCardClicked(BasicCard entity) {
        productService.getProduct(AuthUtil.getAuthorizationValue(getContext()), entity.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetProductResponseDTO> call,
                                   @NonNull Response<GetProductResponseDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("productDTO", response.body());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.navigate_to_product_update, bundle);
                } else {
                    try {
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
            public void onFailure(@NonNull Call<GetProductResponseDTO> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void setContent(boolean isUpdate) {
        isLoading = true;
        Map<String, String> combinedFilters = combineFiltersAndSort();
        productService.findProductsByProvider(AuthUtil.getAuthorizationValue(requireContext()), combinedFilters).enqueue(new Callback<Page<BasicItemDTO>>() {
            @Override
            public void onResponse(@NonNull Call<Page<BasicItemDTO>> call,
                                   @NonNull Response<Page<BasicItemDTO>> response) {
                if (!isAdded()) return;
                if (!isUpdate) viewAdapter.clear();
                if (!response.isSuccessful()) {
                    messageView.setVisibility(View.VISIBLE);
                    messageView.setText(R.string.unable_to_contact_server);
                    return;
                }
                Page<BasicItemDTO> pagedResponse = response.body();
                if (pagedResponse == null || pagedResponse.isEmpty()) {
                    if (page.isFirst()) {
                        messageView.setText("No products to show");
                        messageView.setVisibility(View.VISIBLE);
                    }
                    return;
                }
                page.update(pagedResponse);
                String resultCount = String.format(getString(R.string.d_products_found),
                        page.getTotalElements());
                messageView.setText(resultCount);
                viewAdapter.addAll(page.getContent());
                isLoading = false;
            }

            @Override
            public void onFailure(@NonNull Call<Page<BasicItemDTO>> call, @NonNull Throwable t) {
                messageView.setVisibility(View.VISIBLE);
                messageView.setText(R.string.error_fetching_more_data);
                isLoading = false;
            }
        });
    }

    private Map<String, String> combineFiltersAndSort() {
        var combined = Stream.concat(latestFilters.entrySet().stream(), page.toQueryMap().entrySet().stream());
        var combinedAndSorted = combined.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        combinedAndSorted.put("sort", "");
        return combinedAndSorted;
    }

    private void setupContentScrollListener() {
        productsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                var layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisiblePosition = Objects.requireNonNull(layoutManager).findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == viewAdapter.getLastItemIndex() && !page.isLast())
                    onNextPage();
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty()) {
                    return true;
                }
                latestFilters.put("name", query);
                searchView.clearFocus();
                setContent(false);
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
}
