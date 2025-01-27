package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.FavouriteItemRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.user.FavouriteItemResponseDTO;
import rs.ac.uns.eventplanner.team7.services.UserService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class ServiceDetailsFragment extends Fragment {
    private final UserService userService = ClientUtils.injectService(UserService.class);
    private final GetServiceResponseDTO serviceDTO;

    ImageView favouriteStar;
    MaterialTextView nameView, descriptionView, specificsView, priceView, discountView, minDurationView, maxDurationView,
            reservationDeadlineView, cancellationDeadlineView, categoryView;

    public ServiceDetailsFragment(GetServiceResponseDTO serviceDTO) {
        this.serviceDTO = serviceDTO;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_details, container, false);
        favouriteStar = view.findViewById(R.id.favourite_star);
        if (serviceDTO.isFavourite())
            favouriteStar.setImageResource(R.drawable.ic_star_filled);

        nameView = view.findViewById(R.id.service_details_name);
        descriptionView = view.findViewById(R.id.service_details_description);
        specificsView = view.findViewById(R.id.service_details_specifics);
        priceView = view.findViewById(R.id.service_details_price);
        discountView = view.findViewById(R.id.service_details_discount);
        minDurationView = view.findViewById(R.id.service_details_min_duration);
        maxDurationView = view.findViewById(R.id.service_details_max_duration);
        reservationDeadlineView = view.findViewById(R.id.service_details_reservation);
        cancellationDeadlineView = view.findViewById(R.id.service_details_cancellation);
        categoryView = view.findViewById(R.id.service_details_category);

        fillDetails();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        favouriteStar.setOnClickListener(v -> {
            serviceDTO.setFavourite(!serviceDTO.isFavourite());
            if (serviceDTO.isFavourite())
                favouriteStar.setImageResource(R.drawable.ic_star_filled);
            else
                favouriteStar.setImageResource(R.drawable.ic_star_border);

            userService.markItemAsFavourite(JwtUtil.getAuthorizationValue(requireContext()),
                                            JwtUtil.extractId(requireContext()),
                                            new FavouriteItemRequestDTO(serviceDTO.getId(), serviceDTO.isFavourite()))
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<FavouriteItemResponseDTO> call,
                                               @NonNull Response<FavouriteItemResponseDTO> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String message = serviceDTO.isFavourite() ? "Service added to favourites!" : "Service removed from favourites!";
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
    }

    private void fillDetails() {
        nameView.setText(serviceDTO.getName());
        descriptionView.setText(serviceDTO.getDescription());
        specificsView.setText(serviceDTO.getSpecifics());
        priceView.setText(String.format("%s €", serviceDTO.getPricing().getPrice()));
        discountView.setText(String.format("%s %%", serviceDTO.getPricing().getDiscount()));
        minDurationView.setText(String.format("%s min", serviceDTO.getMinDurationInMinutes()));
        maxDurationView.setText(String.format("%s min", serviceDTO.getMaxDurationInMinutes()));
        reservationDeadlineView.setText(String.format("%s d", serviceDTO.getReservationDeadlineInDays()));
        cancellationDeadlineView.setText(String.format("%s d", serviceDTO.getCancellationDeadlineInDays()));
        categoryView.setText(serviceDTO.getCategory().getName());
    }
}