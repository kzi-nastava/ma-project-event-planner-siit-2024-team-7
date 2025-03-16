package rs.ac.uns.eventplanner.team7.fragments.purchases;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.dto.Page;
import rs.ac.uns.eventplanner.team7.dto.Sort;
import rs.ac.uns.eventplanner.team7.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.model.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.services.EventService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class SelectEventForPurchase extends Fragment implements SearchActionsListener, CardClickListener {
    private final EventService eventService = ClientUtils.injectService(EventService.class);

    private final Page<BasicEventDTO> page;
    private RecyclerView eventsView;
    private MaterialTextView messageView;
    private CardRecyclerViewAdapter<BasicEventDTO> viewAdapter;
    private boolean isLoading;
    private SearchView searchView;
    private String lastSearch;
    private String eventQuery;
    private GetProductResponseDTO productDTO;

    private MaterialTextView purchaseWelcome;
    private MaterialButton resetSearch;

    public SelectEventForPurchase() {
        page = Page.getDefault();
        eventQuery = "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productDTO = getArguments().getParcelable("productDTO", GetProductResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_event_for_purchase, container, false);
        purchaseWelcome = view.findViewById(R.id.purchase_welcome);
        messageView = view.findViewById(R.id.events_search_result_message_view);
        searchView = view.findViewById(R.id.select_event_search_view);
        resetSearch = view.findViewById(R.id.reset_event_search_btn);
        eventsView = view.findViewById(R.id.select_event_recycler_view);
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(), this);
        eventsView.setAdapter(viewAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        purchaseWelcome.setText(String.format("Purchase %s", productDTO.getName()));
        setContent(false, eventQuery);
        setupSearchView();
    }

    @Override
    public void onCardClicked(BasicCard entity) {

    }

    @Override
    public void onSortApplied() {
        page.resetToDefault();
        page.setSort(new Sort().by(""));
        setContent(false, eventQuery);
    }

    @Override
    public void onNextPage() {
        if (isLoading || page.isLast()) return;
        page.nextPage();
        setContent(true, eventQuery);
    }

    @Override
    public void onFiltersApplied() {}

    @Override
    public void onFiltersReset() {}

    private void setContent(boolean isUpdate, String name) {
        eventService.getOrganizerEvents(JwtUtil.getAuthorizationValue(requireContext()), JwtUtil.extractId(requireContext()), name).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Page<BasicEventDTO>> call,
                                   @NonNull Response<Page<BasicEventDTO>> response) {
                if (!isAdded()) return;
                if (!isUpdate) viewAdapter.clear();
                if (!response.isSuccessful()) {
                    messageView.setVisibility(View.VISIBLE);
                    messageView.setText(R.string.unable_to_contact_server);
                    return;
                }
                Page<BasicEventDTO> pagedResponse = response.body();
                if (pagedResponse == null || pagedResponse.isEmpty()) {
                    if (page.isFirst()) {
                        messageView.setText(R.string.no_events_to_show);
                        messageView.setVisibility(View.VISIBLE);
                    }
                    return;
                }
                page.update(pagedResponse);
                String resultCount = String.format(getString(R.string.d_events_found),
                        page.getTotalElements());
                messageView.setText(resultCount);
                viewAdapter.addAll(page.getContent());
                isLoading = false;
            }

            @Override
            public void onFailure(@NonNull Call<Page<BasicEventDTO>> call, @NonNull Throwable t) {
                messageView.setVisibility(View.VISIBLE);
                messageView.setText(R.string.error_fetching_more_data);
                isLoading = false;
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
                eventQuery = query;
                searchView.clearFocus();
                setContent(false, eventQuery);
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

        resetSearch.setOnClickListener(v -> {
            eventQuery = "";
            searchView.setQuery("", false);
            searchView.clearFocus();
            setContent(false, eventQuery);
        });
    }
}