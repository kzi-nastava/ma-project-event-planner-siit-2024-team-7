package rs.ac.uns.eventplanner.team7.ui.fragments.home.top;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.ui.adapters.CardRecyclerViewAdapter;
import rs.ac.uns.eventplanner.team7.data.dto.item.DetailedItemDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.services.ProductService;
import rs.ac.uns.eventplanner.team7.data.services.ServiceService;
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
        viewAdapter = new CardRecyclerViewAdapter<>(requireContext(), new ArrayList<>(),
                this, true);
        topItemsView.setAdapter(viewAdapter);

        String bearerToken = JwtUtil.getAuthorizationValue(requireContext());
        handleServiceResponse(productService.findTopFive(bearerToken, userCity));
        handleServiceResponse(serviceService.findTopFive(bearerToken, userCity));

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.items_swipe_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(R.string.fetching_data);
            handleServiceResponse(productService.findTopFive(bearerToken, userCity));
            handleServiceResponse(serviceService.findTopFive(bearerToken, userCity));
        });
    }

    @Override
    public void onCardClicked(BasicCard entity) {
        final String bearerToken = JwtUtil.getAuthorizationValue(requireContext());
        if (((DetailedItemDTO)entity).getType().equals("services")) {
            serviceService.getService(bearerToken, entity.getId()).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetServiceResponseDTO> call,
                                       @NonNull Response<GetServiceResponseDTO> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Bundle args = new Bundle();
                        args.putParcelable("serviceDTO", response.body());
                        View view = getView();
                        if (view == null) return;
                        Navigation.findNavController(view).navigate(R.id.navigate_to_service_details, args);
                        return;
                    }
                    try {
                        // Show error message
                        if (response.errorBody() == null) {
                            showToast("Service not found");
                            return;
                        }
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String message = jsonObject.getString("message");
                        showToast(message);
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if (message == null) return;
                        Log.d("ERROR", message);
                        showToast(message);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetServiceResponseDTO> call, @NonNull Throwable t) {
                    String message = t.getMessage();
                    if (message == null) return;
                    Log.d("ERROR", message);
                    showToast(message);
                }
            });
        }
        else {
            productService.getProduct(bearerToken, entity.getId()).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<GetProductResponseDTO> call,
                                       @NonNull Response<GetProductResponseDTO> response) {
                    if (!isAdded()) return;
                    if (response.isSuccessful() && response.body() != null) {
                        Bundle args = new Bundle();
                        args.putParcelable("productDTO", response.body());
                        View view = getView();
                        if (view == null) return;
                        Navigation.findNavController(view).navigate(R.id.navigate_to_product_details, args);
                        return;
                    }
                    try {
                        if (response.errorBody() == null) {
                            showToast("Product not found");
                            return;
                        }
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String message = jsonObject.getString("message");
                        showToast(message);
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if (message == null) return;
                        Log.d("ERROR", message);
                        showToast(message);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GetProductResponseDTO> call, @NonNull Throwable t) {
                    String message = t.getMessage();
                    if (message == null) return;
                    Log.d("ERROR", message);
                    showToast(message);
                }
            });
        }
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
                messageView.setText(R.string.unable_to_contact_server);
                return;
            }
            responseCount.set(0);
            messageView.setVisibility(View.GONE);
            viewAdapter.clear();
            viewAdapter.addAll(serviceResponses);
            serviceResponses.clear();
        }
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}