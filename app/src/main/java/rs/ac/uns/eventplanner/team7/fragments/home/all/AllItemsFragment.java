package rs.ac.uns.eventplanner.team7.fragments.home.all;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
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
import rs.ac.uns.eventplanner.team7.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.dto.Page;
import rs.ac.uns.eventplanner.team7.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.fragments.ProductDetailsFragment;
import rs.ac.uns.eventplanner.team7.fragments.ServiceDetailsFragment;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.model.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.model.interfaces.Shakeable;
import rs.ac.uns.eventplanner.team7.services.ProductService;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;
import rs.ac.uns.eventplanner.team7.utils.ShakeDetector;

public class AllItemsFragment extends Fragment
        implements SearchActionsListener, CardClickListener, Shakeable {

    private final ProductService productService = ClientUtils.injectService(ProductService.class);
    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final ItemFiltersFragment filtersFragment;
    private final ItemSortOptionsFragment sortOptionsFragment;
    private final Page<BasicItemDTO> page;
    private Map<String, String> latestFilters;
    private MaterialTextView messageView;
    private RecyclerView allItemsView;
    private CardRecyclerViewAdapter<BasicItemDTO> viewAdapter;
    private ShakeDetector shakeDetector;
    private boolean isLoading, hasShownFragment;

    public AllItemsFragment() {
        page = Page.getDefault();
        filtersFragment = ItemFiltersFragment.newInstance(this);
        sortOptionsFragment = ItemSortOptionsFragment.newInstance(this);
        latestFilters = new HashMap<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shakeDetector = new ShakeDetector(requireContext(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_items, container, false);

        messageView = view.findViewById(R.id.items_search_result_message_view);

        allItemsView = view.findViewById(R.id.all_items_recycler_view);
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(), this);
        allItemsView.setAdapter(viewAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        latestFilters.put("city", JwtUtil.getCity(requireContext()));
        setContent(false);

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.items_swipe_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(R.string.fetching_data);
            setContent(false);
        });

        setupButtonListeners(view);

        setupContentScrollListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        shakeDetector.stop();
    }

    @Override
    public void onResume() {
        super.onResume();
        shakeDetector.start();
    }

    @Override
    public void onFiltersApplied() {
        page.resetToDefault();
        latestFilters = filtersFragment.getFilters();
        sortOptionsFragment.scheduleReset(true);
        setContent(false);
    }

    @Override
    public void onFiltersReset() {
        page.resetToDefault();
        sortOptionsFragment.scheduleReset(true);
        latestFilters = new HashMap<>();
        setContent(false);
    }

    @Override
    public void onSortApplied() {
        page.resetToDefault();
        page.setSort(sortOptionsFragment.getSort());
        setContent(false);
    }

    @Override
    public void onNextPage() {
        if (isLoading || page.isLast()) return;
        page.nextPage();
        setContent(true);
    }

    @Override
    public void onCardClicked(BasicCard entity) {
        switch (filtersFragment.getShownItemType().toLowerCase()) {
            case "products":
                productService.getProduct(JwtUtil.getAuthorizationValue(requireContext()), entity.getId()).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<GetProductResponseDTO> call,
                                           @NonNull Response<GetProductResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // TODO add route to nav graph for this and use NavController to navigate to it
                            ProductDetailsFragment fragment = new ProductDetailsFragment(response.body());
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.nav_host_fragment, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        else {
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
                    public void onFailure(@NonNull Call<GetProductResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
                break;
            case "services":
                serviceService.getService(JwtUtil.getAuthorizationValue(getContext()), entity.getId()).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<GetServiceResponseDTO> call,
                                           @NonNull Response<GetServiceResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // TODO add route to nav graph for this and use NavController to navigate to it
                            ServiceDetailsFragment fragment = new ServiceDetailsFragment(response.body());
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.nav_host_fragment, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                        else {
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
                    public void onFailure(@NonNull Call<GetServiceResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
                break;
        }
    }

    @Override
    public void onShakeDetected() {
        sortOptionsFragment.doShake();
        onSortApplied(); // Must be called explicitly as the listener can be null in sortOptions
    }

    private void setContent(boolean isUpdate) {
        isLoading = true;
        messageView.setText(R.string.fetching_data);
        Map<String, String> combinedFilters = combineFiltersAndSort();
        switch (filtersFragment.getShownItemType().toLowerCase()) {
            case "products":
                handleServiceResponse(productService.filter(combinedFilters), isUpdate);
                break;
            case "services":
                handleServiceResponse(serviceService.filter(combinedFilters), isUpdate);
                break;
        }
    }

    private void handleServiceResponse(Call<Page<BasicItemDTO>> serviceCall, boolean isUpdate) {
        serviceCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Page<BasicItemDTO>> call,
                                   @NonNull Response<Page<BasicItemDTO>> response) {
                if (!isAdded()) return;
                if (!isUpdate) viewAdapter.clear();
                if (!response.isSuccessful()) {
                    messageView.setText(R.string.unable_to_contact_server);
                    return;
                }
                Page<BasicItemDTO> pagedResponse = response.body();
                if (pagedResponse == null || pagedResponse.isEmpty()) {
                    if (page.isFirst()) messageView.setText(R.string.no_items_to_show);
                    return;
                }
                page.update(pagedResponse);
                viewAdapter.addAll(page.getContent());
                formatResponseMessage();
                isLoading = false;
            }

            @Override
            public void onFailure(@NonNull Call<Page<BasicItemDTO>> call, @NonNull Throwable t) {
                messageView.setText(R.string.unable_to_contact_server);
                isLoading = false;
            }
        });
    }

    private void setupButtonListeners(@NonNull View view) {
        MaterialButton filtersButton = view.findViewById(R.id.item_filters_button);
        filtersButton.setOnClickListener(v -> {
            if (!hasShownFragment && getChildFragmentManager().findFragmentByTag("itemFiltersFragment") == null) {
                hasShownFragment = true;
                filtersButton.setEnabled(false);
                filtersFragment.show(getChildFragmentManager(), "itemFiltersFragment");
                getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
                        if (f == filtersFragment) {
                            hasShownFragment = false;
                            filtersButton.setEnabled(true);
                            getChildFragmentManager().unregisterFragmentLifecycleCallbacks(this);
                        }
                    }
                }, false);
            }
        });

        MaterialButton sortButton = view.findViewById(R.id.item_sort_button);
        sortButton.setOnClickListener(v -> {
            if (!hasShownFragment && getChildFragmentManager().findFragmentByTag("itemSortOptionsFragment") == null) {
                hasShownFragment = true;
                sortButton.setEnabled(false);
                sortOptionsFragment.show(getChildFragmentManager(), "itemSortOptionsFragment");
                getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
                        if (f == sortOptionsFragment) {
                            hasShownFragment = false;
                            sortButton.setEnabled(true);
                            getChildFragmentManager().unregisterFragmentLifecycleCallbacks(this);
                        }
                    }
                }, false);
            }
        });
    }

    private Map<String, String> combineFiltersAndSort() {
        var combined = Stream.concat(latestFilters.entrySet().stream(), page.toQueryMap().entrySet().stream());
        return combined.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void setupContentScrollListener() {
        allItemsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

    private void formatResponseMessage() {
        int total = page.getTotalElements();
        String shownType = filtersFragment.getShownItemType().toLowerCase();
        String resultCount = String.format(getString(R.string.n_item_search_results_found),
                total, total == 1 ? shownType.substring(0, shownType.length()-1) : shownType);
        messageView.setText(resultCount);
    }

}