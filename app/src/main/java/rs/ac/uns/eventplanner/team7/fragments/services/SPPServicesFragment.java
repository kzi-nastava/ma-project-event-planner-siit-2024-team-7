package rs.ac.uns.eventplanner.team7.fragments.services;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

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
import rs.ac.uns.eventplanner.team7.dto.Sort;
import rs.ac.uns.eventplanner.team7.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.model.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class SPPServicesFragment extends Fragment implements SearchActionsListener, CardClickListener {
    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);

    private final ServiceFilterFragment filterFragment;
    private final Page<BasicItemDTO> page;
    private Map<String, String> latestFilters;
    private String lastSearch;
    private SearchView searchView;
    private MaterialTextView messageView;
    private RecyclerView servicesView;
    private CardRecyclerViewAdapter<BasicItemDTO> viewAdapter;
    private boolean isLoading;

    public SPPServicesFragment() {
        page = Page.getDefault();
        filterFragment = new ServiceFilterFragment(this);
        latestFilters = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spp_services, container, false);
        messageView = view.findViewById(R.id.services_search_result_message_view);
        searchView = view.findViewById(R.id.spp_services_search_view);
        servicesView = view.findViewById(R.id.spp_services_recycler_view);
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(), false, this);
        servicesView.setAdapter(viewAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setContent(false);
        setupSearchView();

        MaterialButton filtersButton = view.findViewById(R.id.service_filters_button);
        filtersButton.setOnClickListener(v -> filterFragment.show(getChildFragmentManager(),
                filterFragment.getTag()));

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
    public void onCardClicked(Integer entityId, String type) {
        serviceService.getService(JwtUtil.getAuthorizationValue(getContext()), entityId).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetServiceResponseDTO> call,
                                   @NonNull Response<GetServiceResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ServiceManagementFragment fragment = ServiceManagementFragment.newInstance(response.body());
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.home_main_fragment_container, fragment)
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
    }

    private void setContent(boolean isUpdate) {
        isLoading = true;
        Map<String, String> combinedFilters = combineFiltersAndSort();
        serviceService.findServicesByProvider(JwtUtil.getAuthorizationValue(requireContext()), combinedFilters).enqueue(new Callback<Page<BasicItemDTO>>() {
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
                        messageView.setText(R.string.no_services_to_show);
                        messageView.setVisibility(View.VISIBLE);
                    }
                    return;
                }
                page.update(pagedResponse);
                String resultCount = String.format(getString(R.string.d_services_found),
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
        servicesView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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