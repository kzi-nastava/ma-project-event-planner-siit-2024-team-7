package rs.ac.uns.eventplanner.team7.ui.fragments.price_list;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.PricingAdapter;
import rs.ac.uns.eventplanner.team7.data.dto.pricing.PricingResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.services.PricingService;
import rs.ac.uns.eventplanner.team7.data.services.ProductService;
import rs.ac.uns.eventplanner.team7.data.services.ServiceService;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.NotificationUtils;
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

        exportPdfBtn.setOnClickListener(v -> {
            pricingService.exportPriceListPdf(AuthUtil.getAuthorizationValue(requireContext()))
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call,
                                               @NonNull Response<ResponseBody> response) {
                            if (!isAdded()) return;
                            if (response.isSuccessful()) {
                                savePDF(response.body());
                            } else {
                                Toast.makeText(requireContext(), "Error downloading PDF", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            Log.e("ERROR", "Request failed", t);
                        }
                    });
        });
    }

    private void setContent() {
        pricingService.getAllPricing(AuthUtil.getAuthorizationValue(requireContext()))
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
                        Log.e("ERROR", "Request failed", t);
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
        serviceService.getService(AuthUtil.getAuthorizationValue(getContext()), id).enqueue(new Callback<>() {
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
                Log.e("ERROR", "Request failed", t);
            }
        });
    }

    private void navigateToProduct(Integer id) {
        productService.getProduct(AuthUtil.getAuthorizationValue(getContext()), id).enqueue(new Callback<>() {
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

    private void savePDF(ResponseBody data) {
        if (data == null) return;
        ContentResolver resolver = requireContext().getContentResolver();
        ContentValues values = new ContentValues();

        values.put(MediaStore.Downloads.DISPLAY_NAME, "price_list.pdf");
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            Toast.makeText(requireContext(), "Failed to download PDF", Toast.LENGTH_SHORT).show();
            return;
        }
        try (InputStream inputStream = data.byteStream();
             OutputStream outputStream = resolver.openOutputStream(uri)) {

            if (outputStream == null) return;

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

            NotificationUtils.showNotificationWithPDF(requireContext(), "Price List PDF", "PDF downloaded successfully: price_list.pdf", uri);
            Toast.makeText(requireContext(), "PDF downloaded successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to download PDF", Toast.LENGTH_SHORT).show();
        }
    }
}