package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.EventTypeCardAdapter;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;


public class    EventTypeListFragment extends Fragment {

    private EventTypeCardAdapter adapter;
    private List<GetEventTypeResponseDTO> eventTypes;
    private EventTypeService eventTypeService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_event_types, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventTypeService = ClientUtils.retrofit.create(EventTypeService.class);

        eventTypes = new ArrayList<>();
        adapter = new EventTypeCardAdapter(requireContext(), eventTypes);
        recyclerView.setAdapter(adapter);

        LinearLayout content = view.findViewById(R.id.content_view);
        MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
        content.setVisibility(View.GONE);
        loadingMsg.setVisibility(View.VISIBLE);

        fetchData(view);

        MaterialButton create = view.findViewById(R.id.create_event_type);
        create.setOnClickListener(v -> {
            Fragment fragment = new CreateEventTypeFragment();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.home_main_fragment_container, fragment, "CreateEventTypeFragmentTag")
                        .addToBackStack(null)
                        .commit();
            }

        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if a message was passed and show the Snackbar
        if (getArguments() != null && getArguments().containsKey("snackbar_message")) {
            String message = getArguments().getString("snackbar_message");
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction("OK", v -> {
                snackbar.dismiss();
            });

            snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            snackbar.show();
        }
    }

    private void fetchData(View view) {
        Call<List<GetEventTypeResponseDTO>> call = eventTypeService.getAll(JwtUtil.getAuthorizationValue(requireContext()));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<GetEventTypeResponseDTO>> call, @NonNull Response<List<GetEventTypeResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    LinearLayout content = view.findViewById(R.id.content_view);
                    MaterialTextView loadingMsg = view.findViewById(R.id.loading_msg);
                    content.setVisibility(View.VISIBLE);
                    loadingMsg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GetEventTypeResponseDTO>> call, @NonNull Throwable t) {

            }
        });
    }
}

