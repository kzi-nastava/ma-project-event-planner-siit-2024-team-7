package rs.ac.uns.eventplanner.team7.ui.fragments.products;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.ui.adapters.ImageAdapter;
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemResponseDTO;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class ProductDetailsFragment extends Fragment {
    private final UserService userService = ClientUtils.injectService(UserService.class);
    private GetProductResponseDTO productDTO;

    private ImageView favouriteStar;
    private MaterialTextView nameView, descriptionView, priceView, discountView, categoryView, eventTypesView, availabilityView, noImagesView;
    private RecyclerView imagesView;
    private ImageAdapter imageAdapter;

    private MaterialButton buyButton, viewProviderButton, chatWithProviderButton;

    public ProductDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productDTO = getArguments().getParcelable("productDTO", GetProductResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_details, container, false);
        favouriteStar = view.findViewById(R.id.favourite_star);
        if (productDTO.isFavourite())
            favouriteStar.setImageResource(R.drawable.ic_star_filled);

        nameView = view.findViewById(R.id.product_details_name);
        descriptionView = view.findViewById(R.id.product_details_description);
        priceView = view.findViewById(R.id.product_details_price);
        discountView = view.findViewById(R.id.product_details_discount);
        categoryView = view.findViewById(R.id.product_details_category);
        eventTypesView = view.findViewById(R.id.product_details_event_types);
        availabilityView = view.findViewById(R.id.product_details_availability);
        imagesView = view.findViewById(R.id.product_details_images);
        noImagesView = view.findViewById(R.id.product_details_no_images);

        buyButton = view.findViewById(R.id.buy_button);
        viewProviderButton = view.findViewById(R.id.view_provider_button);
        chatWithProviderButton = view.findViewById(R.id.chat_w_provider_button);

        fillDetails();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        favouriteStar.setOnClickListener(v -> {
            productDTO.setFavourite(!productDTO.isFavourite());
            if (productDTO.isFavourite())
                favouriteStar.setImageResource(R.drawable.ic_star_filled);
            else
                favouriteStar.setImageResource(R.drawable.ic_star);

            userService.markItemAsFavourite(JwtUtil.getAuthorizationValue(requireContext()),
                            JwtUtil.extractId(requireContext()),
                            new FavouriteItemRequestDTO(productDTO.getId(), productDTO.isFavourite()))
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<FavouriteItemResponseDTO> call,
                                               @NonNull Response<FavouriteItemResponseDTO> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String message = productDTO.isFavourite() ? "Product added to favourites!" : "Product removed from favourites!";
                                Snackbar snackbar = Snackbar.make(view, message, BaseTransientBottomBar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<FavouriteItemResponseDTO> call, @NonNull Throwable t) {
                            Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                        }
                    });
        });

        if (!productDTO.isAvailable())
            buyButton.setEnabled(false);

        if (Objects.equals(JwtUtil.getRole(requireContext()), UserRole.SPP.toString())) {
            viewProviderButton.setVisibility(View.GONE);
            chatWithProviderButton.setVisibility(View.GONE);
        }

        viewProviderButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("itemId", productDTO.getId());
            View view1 = getView();
            if (view1 == null) return;
            Navigation.findNavController(view1).navigate(R.id.navigate_to_spp_details_from_product, args);
        });
    }

    private void fillDetails() {
        nameView.setText(productDTO.getName());
        descriptionView.setText(productDTO.getDescription());
        priceView.setText(String.format("%s €", productDTO.getPricing().getPrice()));
        discountView.setText(String.format("%s %%", productDTO.getPricing().getDiscount()));
        categoryView.setText(productDTO.getCategory().getName());

        if (!productDTO.getAppliesTo().isEmpty()) {
            StringBuilder eventTypesStr = new StringBuilder();
            for (EventType eventType : productDTO.getAppliesTo()) {
                eventTypesStr.append(eventType.getName()).append('\n');
            }
            eventTypesStr.deleteCharAt(eventTypesStr.length()-1);
            eventTypesView.setText(eventTypesStr.toString());
        }
        else {
            eventTypesView.setText("Currently no event types!");
        }

        if (productDTO.isAvailable()) {
            availabilityView.setText("Available");
        }
        else {
            availabilityView.setText("Currently unavailable!");
        }

        if (!productDTO.getImages().isEmpty()) {
            imageAdapter = new ImageAdapter(requireContext(), new ArrayList<>(productDTO.getImages()));
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
            imagesView.setLayoutManager(layoutManager);
            imagesView.setAdapter(imageAdapter);
        }
        else {
            noImagesView.setVisibility(View.VISIBLE);
            imagesView.setVisibility(View.GONE);
        }
    }
}