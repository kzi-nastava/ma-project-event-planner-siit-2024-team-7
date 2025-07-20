package rs.ac.uns.eventplanner.team7.ui.fragments.home.top;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.ui.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.data.dto.event.DetailedEventDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class TopEventsFragment extends Fragment implements CardClickListener {
    private final EventService service = ClientUtils.injectService(EventService.class);
    private String userCity;
    private RecyclerView eventsView;
    private CardRecyclerViewAdapter<DetailedEventDTO> viewAdapter;
    private MaterialTextView messageView;

    public TopEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_events, container, false);
        userCity = JwtUtil.getCity(requireContext());
        eventsView = view.findViewById(R.id.top_events_recycler_view);
        messageView = view.findViewById(R.id.top_events_message_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(),
                this, true);
        eventsView.setAdapter(viewAdapter);

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.events_swipe_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(R.string.fetching_data);
            setContent();
        });

        setContent();
    }

    private void setContent() {
        final String bearerToken = JwtUtil.getAuthorizationValue(requireContext());
        service.findTopFive(bearerToken, userCity).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<DetailedEventDTO>> call,
                                   @NonNull Response<List<DetailedEventDTO>> response) {

                if (!response.isSuccessful() || !isAdded()) {
                    messageView.setText(R.string.unable_to_contact_server);
                    return;
                }

                viewAdapter.clear();
                if (Objects.requireNonNull(response.body()).isEmpty()) {
                    messageView.setText(R.string.no_events_to_show);
                    return;
                }
                viewAdapter.addAll(response.body());
                messageView.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<List<DetailedEventDTO>> call, @NonNull Throwable t) {
                messageView.setText(R.string.unable_to_contact_server);
                messageView.setVisibility(View.VISIBLE);
                eventsView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCardClicked(BasicCard entity) {

    }
}