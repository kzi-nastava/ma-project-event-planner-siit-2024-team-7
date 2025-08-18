package rs.ac.uns.eventplanner.team7.ui.fragments.events;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.ui.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.Sort;
import rs.ac.uns.eventplanner.team7.data.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class OrganizerEventsFragment extends Fragment implements SearchActionsListener, CardClickListener {
    private final EventService eventService = ClientUtils.injectService(EventService.class);

    private final Page<BasicEventDTO> page = Page.getDefault();;
    private RecyclerView eventsView;
    private MaterialTextView messageView;
    private CardRecyclerViewAdapter<BasicEventDTO> viewAdapter;
    private FloatingActionButton createEventButton;
    private boolean isLoading;

    public OrganizerEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_events, container, false);
        messageView = view.findViewById(R.id.events_search_result_message_view);
        eventsView = view.findViewById(R.id.event_list_recycler_view);
        createEventButton = view.findViewById(R.id.create_event_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(), this);
        eventsView.setAdapter(viewAdapter);
        createEventButton.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.navigate_to_event_creation));

        setContent(false);
    }

    @Override
    public void onCardClicked(BasicCard entity) {
        eventService.getEvent(AuthUtil.getAuthorizationValue(requireContext()), entity.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetEventResponseDTO> call,
                                   @NonNull Response<GetEventResponseDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("eventDTO", response.body());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.navigate_to_update_event, bundle);
                } else {
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
            public void onFailure(@NonNull Call<GetEventResponseDTO> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
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

    }

    @Override
    public void onFiltersReset() {

    }

    private void setContent(boolean isUpdate) {
        eventService.getOrganizerEvents(AuthUtil.getAuthorizationValue(requireContext()), AuthUtil.extractId(requireContext()), "").enqueue(new Callback<>() {
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
}
