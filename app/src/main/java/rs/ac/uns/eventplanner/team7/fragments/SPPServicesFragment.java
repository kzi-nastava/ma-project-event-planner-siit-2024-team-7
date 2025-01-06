package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.dto.Page;
import rs.ac.uns.eventplanner.team7.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.model.interfaces.SearchActionsListener;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class SPPServicesFragment extends Fragment implements SearchActionsListener, CardClickListener {
    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);

    private final ServiceFilterFragment filterFragment;
    private final Page<BasicItemDTO> page;
    private Map<String, String> latestFilters;
    private RecyclerView servicesView;
    private CardRecyclerViewAdapter<BasicItemDTO> viewAdapter;

    public SPPServicesFragment() {
        page = Page.getDefault();
        filterFragment = new ServiceFilterFragment(this);
        latestFilters = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spp_services, container, false);
        servicesView = view.findViewById(R.id.spp_services_recycler_view);
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(), true, this);
        servicesView.setAdapter(viewAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onSortApplied() {

    }

    @Override
    public void onNextPage() {

    }

    @Override
    public void onFiltersApplied() {

    }

    @Override
    public void onFiltersReset() {

    }

    @Override
    public void onCardClicked(Integer entityId, String type) {

    }
}