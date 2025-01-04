package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView topItemsView;
    private final ProductService productService = ClientUtils.injectService(ProductService.class);
    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final List<DetailedItemDTO> serviceResponses = new ArrayList<>();
    private final AtomicInteger responseCount = new AtomicInteger(0);

    public TopItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_items, container, false);
        setupView(view);
        return view;
    }

    private void setupView(View view) {

        String userCity = JwtUtil.getCity(requireContext());
        topItemsView = view.findViewById(R.id.top_items_recycler_view);
        messageView = view.findViewById(R.id.top_items_message_view);

        handleServiceResponse(productService.findTopFive(userCity));
        handleServiceResponse(serviceService.findTopFive(userCity));
    }

    private void handleServiceResponse(Call<List<DetailedItemDTO>> service) {
        service.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<DetailedItemDTO>> call,
                                   @NonNull Response<List<DetailedItemDTO>> response) {

                if (!response.isSuccessful()) {
                    tryToSetAdapter();
                    return;
                }

                List<DetailedItemDTO> items = response.body();
                if (items != null && !items.isEmpty()) {
                    synchronized (serviceResponses) {
                        messageView.setVisibility(View.GONE);
                        serviceResponses.addAll(items);
                    }
                }
                tryToSetAdapter();
            }

            @Override
            public void onFailure(@NonNull Call<List<DetailedItemDTO>> call, @NonNull Throwable t) {
                tryToSetAdapter();
            }
        });
    }

    private void tryToSetAdapter() {
        if (responseCount.incrementAndGet() == 2) {
            if (serviceResponses.isEmpty()) {
                messageView.setText(R.string.no_items_to_show);
                return;
            }
            messageView.setVisibility(View.GONE);
            topItemsView.setAdapter(new CardRecyclerViewAdapter<>(requireContext(),
                    serviceResponses, true, TopItemsFragment.this));
        }
    }

    @Override
    public void onCardClicked(Integer entityId, String type) {
        // TODO redirect to item details page
    }
}