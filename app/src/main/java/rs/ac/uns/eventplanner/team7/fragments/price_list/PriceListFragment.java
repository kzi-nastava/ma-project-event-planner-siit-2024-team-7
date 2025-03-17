package rs.ac.uns.eventplanner.team7.fragments.price_list;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.PricingAdapter;
import rs.ac.uns.eventplanner.team7.dto.pricing.PricingResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.services.PricingService;
import rs.ac.uns.eventplanner.team7.services.ProductService;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;
import rs.ac.uns.eventplanner.team7.utils.RecyclerViewBorderDecoration;

public class PriceListFragment extends Fragment implements PricingAdapter.OnItemSelectedListener {
    private final PricingService pricingService = ClientUtils.injectService(PricingService.class);
    private final ServiceService serviceService = ClientUtils.injectService(ServiceService.class);
    private final ProductService productService = ClientUtils.injectService(ProductService.class);

    private RecyclerView servicesView, productsView;
    private MaterialButton exportPdfBtn, updateServiceBtn, updateProductBtn;
    private MaterialTextView noDataText;
    private PricingAdapter serviceAdapter, productAdapter;
    private Integer selectedServiceId = null, selectedProductId = null;

    public PriceListFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_price_list, container, false);
        servicesView = view.findViewById(R.id.service_pricings_recycler_view);
        productsView = view.findViewById(R.id.product_pricings_recycler_view);
        exportPdfBtn = view.findViewById(R.id.export_pdf_btn);
        updateServiceBtn = view.findViewById(R.id.update_service_pricing_btn);
        updateProductBtn = view.findViewById(R.id.update_product_pricing_btn);
        noDataText = view.findViewById(R.id.no_data_text);

        servicesView.addItemDecoration(new RecyclerViewBorderDecoration(requireContext(), Color.BLACK, 5));
        servicesView.setLayoutManager(new LinearLayoutManager(requireContext()));
        serviceAdapter = new PricingAdapter(requireContext(), new ArrayList<>(), this);
        servicesView.setAdapter(serviceAdapter);

        productsView.setLayoutManager(new LinearLayoutManager(requireContext()));
        productsView.addItemDecoration(new RecyclerViewBorderDecoration(requireContext(), Color.BLACK, 5));
        productAdapter = new PricingAdapter(requireContext(), new ArrayList<>(), this);
        productsView.setAdapter(productAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContent();

        updateServiceBtn.setOnClickListener(v -> {
            if (selectedServiceId == null) return;
            navigateToService(selectedServiceId);
        });

        updateProductBtn.setOnClickListener(v -> {
            if (selectedProductId == null) return;
            navigateToProduct(selectedProductId);
        });
    }

    private void setContent() {
        pricingService.getAllPricing(JwtUtil.getAuthorizationValue(requireContext()))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<PricingResponseDTO>> call,
                                           @NonNull Response<List<PricingResponseDTO>> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            List<PricingResponseDTO> servicePricings = response.body().stream().filter(p -> "services".equals(p.getItemType())).toList();
                            List<PricingResponseDTO> productPricings = response.body().stream().filter(p -> "products".equals(p.getItemType())).toList();

                            serviceAdapter.clear();
                            serviceAdapter.addAll(servicePricings);
                            productAdapter.clear();
                            productAdapter.addAll(productPricings);
                        }
                        else {
                            noDataText.setVisibility(View.VISIBLE);
                            noDataText.setText(R.string.unable_to_contact_server);
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<List<PricingResponseDTO>> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }

    private void selectService(int itemId) {
        selectedServiceId = itemId;
        updateServiceBtn.setVisibility(View.VISIBLE);
        updateProductBtn.setVisibility(View.GONE);
    }

    private void selectProduct(int itemId) {
        selectedProductId = itemId;
        updateProductBtn.setVisibility(View.VISIBLE);
        updateServiceBtn.setVisibility(View.GONE);
    }

    private void navigateToService(Integer id) {
        serviceService.getService(JwtUtil.getAuthorizationValue(getContext()), id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetServiceResponseDTO> call,
                                   @NonNull Response<GetServiceResponseDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("serviceDTO", response.body());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.navigate_to_update_service, bundle);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetServiceResponseDTO> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void navigateToProduct(Integer id) {
        productService.getProduct(JwtUtil.getAuthorizationValue(getContext()), id).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GetProductResponseDTO> call,
                                   @NonNull Response<GetProductResponseDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("productDTO", response.body());
                    Navigation.findNavController(requireView())
                            .navigate(R.id.navigate_to_update_product, bundle);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetProductResponseDTO> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    @Override
    public void onItemSelected(Integer itemId, String type) {
        if (type.equals("services")) selectService(itemId);
        else selectProduct(itemId);
    }
}