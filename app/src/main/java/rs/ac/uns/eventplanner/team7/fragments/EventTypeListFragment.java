package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

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


public class EventTypeListFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventTypeCardAdapter adapter;
    private List<GetEventTypeResponseDTO> eventTypes;
    private EventTypeService eventTypeService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_event_types, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventTypeService = ClientUtils.retrofit.create(EventTypeService.class);

        eventTypes = new ArrayList<>();
        adapter = new EventTypeCardAdapter(requireContext(), eventTypes);
        recyclerView.setAdapter(adapter);

        fetchData();

        MaterialButton create = view.findViewById(R.id.create_event_type);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new CreateEventTypeFragment();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout, fragment, "CreateEventTypeFragmentTag")
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void fetchData() {
        Call<List<GetEventTypeResponseDTO>> call = eventTypeService.getAll(JwtUtil.getAuthorizationValue(requireContext()));
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<GetEventTypeResponseDTO>> call, @NonNull Response<List<GetEventTypeResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    eventTypes.clear();
                    eventTypes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<GetEventTypeResponseDTO>> call, @NonNull Throwable t) {

            }
        });
    }
}

