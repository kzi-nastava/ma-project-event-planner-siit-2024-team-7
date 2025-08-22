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
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatContactDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.AverageRatingDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.FeedbackDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.data.services.FeedbackService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.adapters.CommentAdapter;
import rs.ac.uns.eventplanner.team7.ui.adapters.ImageAdapter;
import rs.ac.uns.eventplanner.team7.ui.fragments.feedback.FeedbackDialog;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class ProductDetailsFragment extends Fragment {
    private final UserService userService = ClientUtils.injectService(UserService.class);
    private final FeedbackService feedbackService = ClientUtils.injectService(FeedbackService.class);
    private GetProductResponseDTO productDTO;
    private GetProviderResponseDTO providerDTO;

    private ImageView favouriteStar, star1, star2, star3, star4, star5;
    private MaterialTextView nameView, descriptionView, priceView, discountView, categoryView, eventTypesView, availabilityView, noImagesView, ratingCount, noComments;
    private RecyclerView imagesView, commentsView;
    private ImageAdapter imageAdapter;
    private CommentAdapter commentAdapter;

    private MaterialButton buyButton, viewProviderButton, chatWithProviderButton, rateButton;

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

        rateButton = view.findViewById(R.id.rate_product_button);
        commentsView = view.findViewById(R.id.product_comments);
        noComments = view.findViewById(R.id.no_comments_product);
        ratingCount = view.findViewById(R.id.rating_count);
        star1 = view.findViewById(R.id.star1);
        star2 = view.findViewById(R.id.star2);
        star3 = view.findViewById(R.id.star3);
        star4 = view.findViewById(R.id.star4);
        star5 = view.findViewById(R.id.star5);

        fillDetails();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commentAdapter = new CommentAdapter(requireContext(), new ArrayList<>());
        commentsView.setAdapter(commentAdapter);

        userService.getProviderByItemId(AuthUtil.getAuthorizationValue(requireContext()), productDTO.getId())
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<GetProviderResponseDTO> call,
                                           @NonNull Response<GetProviderResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            providerDTO = response.body();
                            chatWithProviderButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GetProviderResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });

        favouriteStar.setOnClickListener(v -> {
            productDTO.setFavourite(!productDTO.isFavourite());
            if (productDTO.isFavourite())
                favouriteStar.setImageResource(R.drawable.ic_star_filled);
            else
                favouriteStar.setImageResource(R.drawable.ic_star);

            userService.markItemAsFavourite(AuthUtil.getAuthorizationValue(requireContext()),
                            AuthUtil.extractId(requireContext()),
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

        if (AuthUtil.extractRole(requireContext()) != UserRole.EVENT_ORG || !productDTO.isAvailable())
            buyButton.setVisibility(View.GONE);

        if (AuthUtil.extractRole(requireContext()) == UserRole.SPP) {
            viewProviderButton.setVisibility(View.GONE);
            chatWithProviderButton.setVisibility(View.GONE);
        }

        if (!productDTO.isPurchased())
            rateButton.setEnabled(true);
        setFeedbacks();

        buyButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("productDTO", productDTO);
            View view1 = getView();
            if (view1 == null) return;
            Navigation.findNavController(view1).navigate(R.id.navigate_to_select_event, bundle);
        });

        viewProviderButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("itemId", productDTO.getId());
            View view1 = getView();
            if (view1 == null) return;
            Navigation.findNavController(view1).navigate(R.id.navigate_to_spp_details_from_product, args);
        });

        chatWithProviderButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("contactDTO", new ChatContactDTO(providerDTO.getId(), providerDTO.getEmail(), providerDTO.getPhotoURL(), false));
            Navigation.findNavController(view).navigate(R.id.nav_chats, bundle);
        });

        rateButton.setOnClickListener(v -> {
            FeedbackDialog dialog = new FeedbackDialog(productDTO.getId(), null, "item", "product");
            dialog.show(requireActivity().getSupportFragmentManager(), "FeedbackDialog");
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

    private void setFeedbacks() {
        String token = AuthUtil.getAuthorizationValue(requireContext());
        feedbackService.getAllApprovedForItem(token, productDTO.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<FeedbackDTO>> call,
                                   @NonNull Response<List<FeedbackDTO>> response) {
                if (!isAdded()) return;
                commentAdapter.clear();
                if (response.body() == null || !response.isSuccessful()) {
                    noComments.setText(R.string.unable_to_contact_server);
                    return;
                }
                if (response.body().isEmpty()) {
                    noComments.setText(R.string.no_comments_yet);
                    return;
                }
                commentAdapter.addAll(response.body());
                noComments.setText("");
            }

            @Override
            public void onFailure(@NonNull Call<List<FeedbackDTO>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });

        feedbackService.getAverageRatingForItem(token, productDTO.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<AverageRatingDTO> call,
                                   @NonNull Response<AverageRatingDTO> response) {
                if (!isAdded()) return;
                if (response.body() == null || !response.isSuccessful()) return;
                ratingCount.setText(String.format("(%s)", response.body().getFeedbackCount()));
                fillStars(response.body().getRating());
            }

            @Override
            public void onFailure(@NonNull Call<AverageRatingDTO> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    private void fillStars(double rating) {
        int filledStar = R.drawable.ic_star_filled;
        int emptyStar = R.drawable.ic_star;
        int halfStar = R.drawable.ic_half_star;

        ImageView[] stars = { star1, star2, star3, star4, star5 };

        int fullStars = (int) rating;
        double decimal = rating - fullStars;

        if (decimal > 0.7) {
            fullStars++;
            decimal = 0;
        } else if (decimal < 0.3) {
            decimal = 0;
        } else {
            decimal = 0.5;
        }

        for (int i = 0; i < stars.length; i++) {
            if (i < fullStars) {
                stars[i].setImageResource(filledStar);
            } else if (i == fullStars && decimal == 0.5) {
                stars[i].setImageResource(halfStar);
            } else {
                stars[i].setImageResource(emptyStar);
            }
        }
    }
}