package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.dto.item.DetailedItemDTO;
import rs.ac.uns.eventplanner.team7.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.GetServiceResponseDTO;
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

    private void handleServiceResponse(Call<List<DetailedItemDTO>> serviceCall) {
        serviceCall.enqueue(new Callback<>() {
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
            if (serviceResponses.isEmpty() || !isAdded()) {
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
        if (type.equals("services")) {
            serviceService.getService(JwtUtil.getAuthorizationValue(getContext()), entityId).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetServiceResponseDTO> call,
                                       @NonNull Response<GetServiceResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ServiceDetailsFragment fragment = new ServiceDetailsFragment(response.body());
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.home_main_fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                    else {
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
                public void onFailure(@NonNull Call<GetServiceResponseDTO> call, @NonNull Throwable t) {
                    Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                }
            });
        }
        else {
            productService.getProduct(JwtUtil.getAuthorizationValue(requireContext()), entityId).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetProductResponseDTO> call,
                                       @NonNull Response<GetProductResponseDTO> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ProductDetailsFragment fragment = new ProductDetailsFragment(response.body());
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.home_main_fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                    else {
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
                public void onFailure(@NonNull Call<GetProductResponseDTO> call, @NonNull Throwable t) {
                    Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                }
            });
        }
    }
}