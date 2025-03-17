package rs.ac.uns.eventplanner.team7.fragments.price_list;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.PricingAdapter;
import rs.ac.uns.eventplanner.team7.dto.pricing.PricingResponseDTO;
import rs.ac.uns.eventplanner.team7.services.PricingService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;
import rs.ac.uns.eventplanner.team7.utils.RecyclerViewBorderDecoration;

public class PriceListFragment extends Fragment {
    private final PricingService pricingService = ClientUtils.injectService(PricingService.class);

    private RecyclerView servicesView, productsView;
    private MaterialButton exportPdfBtn, updateServiceBtn, updateProductBtn;
    private MaterialTextView noDataText;

    private List<PricingResponseDTO> servicePricings;
    private List<PricingResponseDTO> productPricings;

    private PricingAdapter serviceAdapter, productAdapter;

    public PriceListFragment() {
        servicePricings = new ArrayList<>();
        productPricings = new ArrayList<>();
    }


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
        serviceAdapter = new PricingAdapter(requireContext(), servicePricings);
        servicesView.setAdapter(serviceAdapter);

        productsView.setLayoutManager(new LinearLayoutManager(requireContext()));
        productsView.addItemDecoration(new RecyclerViewBorderDecoration(requireContext(), Color.BLACK, 5));
        productAdapter = new PricingAdapter(requireContext(), productPricings);
        productsView.setAdapter(productAdapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContent();
    }

    private void setContent() {
        pricingService.getAllPricing(JwtUtil.getAuthorizationValue(requireContext()))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<PricingResponseDTO>> call,
                                           @NonNull Response<List<PricingResponseDTO>> response) {
                        if (!isAdded()) return;
                        serviceAdapter.clear();
                        productAdapter.clear();
                        if (response.isSuccessful() && response.body() != null) {
                            servicePricings = response.body().stream().filter(p -> "services".equals(p.getItemType())).toList();
                            productPricings = response.body().stream().filter(p -> "products".equals(p.getItemType())).toList();

                            serviceAdapter.addAll(servicePricings);
                            productAdapter.addAll(productPricings);
                        }
                        else {
                            noDataText.setVisibility(View.VISIBLE);
                            noDataText.setText(R.string.unable_to_contact_server);
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<List<PricingResponseDTO>> call, @NonNull Throwable t) {

                    }
                });
    }
}