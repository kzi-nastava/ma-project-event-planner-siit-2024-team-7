package rs.ac.uns.eventplanner.team7.fragments;

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
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.dto.item.DetailedItemDTO;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.services.ProductService;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class TopItemsFragment extends Fragment implements CardClickListener {
    private MaterialTextView messageView;
    private CardRecyclerViewAdapter<DetailedItemDTO> viewAdapter;
    private final ProductService productService = ClientUtils.injectService(ProductService.class);
    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final List<DetailedItemDTO> serviceResponses = new ArrayList<>();
    private final AtomicInteger responseCount = new AtomicInteger(0);
    private String userCity;

    public TopItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_items, container, false);
        userCity = JwtUtil.getCity(requireContext());
        messageView = view.findViewById(R.id.top_items_message_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView topItemsView = view.findViewById(R.id.top_items_recycler_view);
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(),
                new ArrayList<>(), true, this);
        topItemsView.setAdapter(viewAdapter);

        handleServiceResponse(productService.findTopFive(userCity));
        handleServiceResponse(serviceService.findTopFive(userCity));

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.items_swipe_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(R.string.fetching_data);
            handleServiceResponse(productService.findTopFive(userCity));
            handleServiceResponse(serviceService.findTopFive(userCity));
        });
    }

    private void handleServiceResponse(Call<List<DetailedItemDTO>> serviceCall) {
        serviceCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<DetailedItemDTO>> call,
                                   @NonNull Response<List<DetailedItemDTO>> response) {

                if (!response.isSuccessful()) {
                    joinResponses();
                    return;
                }

                List<DetailedItemDTO> items = response.body();
                if (items != null && !items.isEmpty()) {
                    synchronized (serviceResponses) {
                        messageView.setVisibility(View.GONE);
                        serviceResponses.addAll(items);
                    }
                }
                joinResponses();
            }

            @Override
            public void onFailure(@NonNull Call<List<DetailedItemDTO>> call, @NonNull Throwable t) {
                joinResponses();
            }
        });
    }

    private void joinResponses() {
        if (responseCount.incrementAndGet() == 2) {
            if (serviceResponses.isEmpty() || !isAdded()) {
                viewAdapter.clear();
                messageView.setText(R.string.no_items_to_show);
                return;
            }
            responseCount.set(0);
            messageView.setVisibility(View.GONE);
            viewAdapter.clear();
            viewAdapter.addAll(serviceResponses);
            serviceResponses.clear();
        }
    }

    @Override
    public void onCardClicked(Integer entityId, String type) {
        // TODO redirect to item details page
    }
}