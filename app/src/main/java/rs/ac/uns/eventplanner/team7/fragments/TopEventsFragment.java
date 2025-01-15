package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.dto.event.DetailedEventDTO;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.services.EventService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class TopEventsFragment extends Fragment implements CardClickListener {
    private final EventService service = ClientUtils.injectService(EventService.class);

    public TopEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_events, container, false);
        setupView(view);
        return view;
    }

    private void setupView(View view) {
        String userCity = JwtUtil.getCity(requireContext());
        RecyclerView eventsView = view.findViewById(R.id.top_events_recycler_view);
        MaterialTextView messageView = view.findViewById(R.id.top_events_message_view);
        service.findTopFive(userCity).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<DetailedEventDTO>> call,
                                   @NonNull Response<List<DetailedEventDTO>> response) {

                if (!response.isSuccessful() || !isAdded()) {
                    messageView.setText(R.string.unable_to_contact_server);
                    return;
                }
                var events = response.body();
                if (Objects.requireNonNull(events).isEmpty()) {
                    messageView.setText(R.string.no_events_to_show);
                    return;
                }
                messageView.setVisibility(View.GONE);
                eventsView.setAdapter(new CardRecyclerViewAdapter<>(requireContext(),
                        events, true, TopEventsFragment.this));
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
    public void onCardClicked(Integer entityId, String events) {
        // TODO redirect to event details page
    }
}