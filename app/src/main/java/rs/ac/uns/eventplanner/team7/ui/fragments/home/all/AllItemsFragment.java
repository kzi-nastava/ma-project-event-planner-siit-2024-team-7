package rs.ac.uns.eventplanner.team7.ui.fragments.home.all;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
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
import rs.ac.uns.eventplanner.team7.ui.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.data.interfaces.Shakeable;
import rs.ac.uns.eventplanner.team7.data.services.ProductService;
import rs.ac.uns.eventplanner.team7.data.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
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
    private String bearerToken;

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

        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        
        latestFilters.put("city", AuthUtil.extractCity(requireContext()));
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
        if (((BasicItemDTO)entity).getType().equals("services")) {
            serviceService.getService(bearerToken, entity.getId()).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetServiceResponseDTO> call,
                                       @NonNull Response<GetServiceResponseDTO> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Bundle args = new Bundle();
                        args.putParcelable("serviceDTO", response.body());
                        View view = getView();
                        if (view == null) return;
                        Navigation.findNavController(view).navigate(R.id.navigate_to_service_details, args);
                        return;
                    }
                    try {
                        // Show error message
                        if (response.errorBody() == null) {
                            showToast("Service not found");
                            return;
                        }
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String message = jsonObject.getString("message");
                        showToast(message);
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if (message == null) return;
                        Log.d("ERROR", message);
                        showToast(message);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetServiceResponseDTO> call, @NonNull Throwable t) {
                    Log.e("ERROR", "Request failed", t);
                }
            });
        } else {
            productService.getProduct(bearerToken, entity.getId()).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetProductResponseDTO> call,
                                       @NonNull Response<GetProductResponseDTO> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Bundle args = new Bundle();
                        args.putParcelable("productDTO", response.body());
                        View view = getView();
                        if (view == null) return;
                        Navigation.findNavController(view).navigate(R.id.navigate_to_product_details, args);
                        return;
                    }
                    try {
                        if (response.errorBody() == null) {
                            showToast("Product not found");
                            return;
                        }
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String message = jsonObject.getString("message");
                        showToast(message);
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if (message == null) return;
                        Log.d("ERROR", message);
                        showToast(message);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetProductResponseDTO> call, @NonNull Throwable t) {
                    Log.e("ERROR", "Request failed", t);
                }
            });
        }
    }

    @Override
    public void onShakeDetected() {
        sortOptionsFragment.doShake();
        onSortApplied(); // Must be called explicitly as the listener can be null in sortOptions
    }

    private void setContent(boolean isUpdate) {
        isLoading = true;
        if (!isUpdate) page.resetToDefault();
        messageView.setText(R.string.fetching_data);
        Map<String, String> combinedFilters = combineFiltersAndSort();
        switch (filtersFragment.getShownItemType().toLowerCase()) {
            case "products":
                handleServiceResponse(productService.filter(bearerToken, combinedFilters), isUpdate);
                break;
            case "services":
                handleServiceResponse(serviceService.filter(bearerToken, combinedFilters), isUpdate);
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

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

}