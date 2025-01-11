package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

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
import rs.ac.uns.eventplanner.team7.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.model.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.services.EventService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class AllEventsFragment extends Fragment implements SearchActionsListener, CardClickListener {
    private final EventService service = ClientUtils.injectService(EventService.class);
    private final EventFiltersFragment filtersFragment;
    private final EventSortOptionsFragment sortOptionsFragment;
    private final Page<BasicEventDTO> page;
    private Map<String, String> latestFilters;
    private MaterialTextView messageView;
    private RecyclerView allEventsView;
    private CardRecyclerViewAdapter<BasicEventDTO> viewAdapter;
    private boolean isLoading, hasShownFragment;

    public AllEventsFragment() {
        page = Page.getDefault();
        filtersFragment = EventFiltersFragment.newInstance(this);
        sortOptionsFragment = EventSortOptionsFragment.newInstance(this);
        latestFilters = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_events, container, false);

        messageView = view.findViewById(R.id.events_search_result_message_view);

        allEventsView = view.findViewById(R.id.all_events_recycler_view);
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(), false, this);
        allEventsView.setAdapter(viewAdapter);
        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        latestFilters.put("city", JwtUtil.getCity(requireContext()));
        setContent(false);

        setupButtonListeners(view);

        setupContentScrollListener();
    }

    @Override
    public void onFiltersApplied() {
        page.resetToDefault();
        latestFilters = filtersFragment.getFilters();
        sortOptionsFragment.scheduleReset();
        setContent(false);
    }

    @Override
    public void onFiltersReset() {
        page.resetToDefault();
        sortOptionsFragment.scheduleReset();
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
    public void onCardClicked(Integer entityId, String type) {
        // TODO redirect to event details page
    }

    private void setContent(boolean isUpdate) {
        isLoading = true;
        messageView.setText(R.string.fetching_data);
        Map<String, String> combinedFilters = combineFiltersAndSort();
        service.filter(combinedFilters).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Page<BasicEventDTO>> call,
                                   @NonNull Response<Page<BasicEventDTO>> response) {
                if (!isUpdate) viewAdapter.clear();
                if (!response.isSuccessful()) {
                    messageView.setText(R.string.unable_to_contact_server);
                    return;
                }
                Page<BasicEventDTO> pagedResponse = response.body();
                if (pagedResponse == null || pagedResponse.isEmpty()) {
                    if (page.isFirst()) messageView.setText(R.string.no_events_to_show);
                    return;
                }
                page.update(pagedResponse);
                viewAdapter.addAll(page.getContent());
                formatResponseMessage();
                isLoading = false;
            }

            @Override
            public void onFailure(@NonNull Call<Page<BasicEventDTO>> call, @NonNull Throwable t) {
                messageView.setText(R.string.error_fetching_more_data);
                isLoading = false;
            }
        });
    }

    private void setupButtonListeners(@NonNull View view) {
        MaterialButton filtersButton = view.findViewById(R.id.event_filters_button);
        filtersButton.setOnClickListener(v -> {
            if (!hasShownFragment && getChildFragmentManager().findFragmentByTag("eventFiltersFragment") == null) {
                hasShownFragment = true;
                filtersButton.setEnabled(false);
                filtersFragment.show(getChildFragmentManager(), "eventFiltersFragment");
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

        MaterialButton sortButton = view.findViewById(R.id.event_sort_button);
        sortButton.setOnClickListener(v -> {
            if (!hasShownFragment && getChildFragmentManager().findFragmentByTag("eventSortOptionsFragment") == null) {
                hasShownFragment = true;
                sortButton.setEnabled(false);
                sortOptionsFragment.show(getChildFragmentManager(), "eventSortOptionsFragment");
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
        allEventsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        String resultCount = String.format(getString(R.string.n_event_search_results_found),
                total, total == 1 ? "" : "s");
        messageView.setText(resultCount);
    }
}