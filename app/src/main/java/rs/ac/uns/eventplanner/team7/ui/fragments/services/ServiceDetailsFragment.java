package rs.ac.uns.eventplanner.team7.ui.fragments.services;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import rs.ac.uns.eventplanner.team7.data.dto.event.FutureReservableEventsDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.WorkDayDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.adapters.ImageAdapter;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class ServiceDetailsFragment extends Fragment {
    private final UserService userService = ClientUtils.injectService(UserService.class);
    private final EventService eventService = ClientUtils.injectService(EventService.class);
    private GetServiceResponseDTO service;
    private GetProviderResponseDTO providerDTO;
    private List<GetEventResponseDTO> organizerEvents;

    private ImageView favouriteStar;
    private MaterialTextView nameView, descriptionView, specificsView, priceView, discountView, minDurationView, maxDurationView,
            reservationDeadlineView, cancellationDeadlineView, categoryView, eventTypesView, workDaysView, noImagesView;
    private RecyclerView imagesView;
    private ImageAdapter imageAdapter;

    private MaterialButton viewProviderButton, chatWithProviderButton;
    private FloatingActionButton reserveButton;

    public ServiceDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            service = getArguments().getParcelable("serviceDTO", GetServiceResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_details, container, false);
        favouriteStar = view.findViewById(R.id.favourite_star);
        if (service.isFavourite())
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
        eventTypesView = view.findViewById(R.id.service_details_event_types);
        workDaysView = view.findViewById(R.id.service_details_availability);
        imagesView = view.findViewById(R.id.service_details_images);
        noImagesView = view.findViewById(R.id.service_details_no_images);
        chatWithProviderButton = view.findViewById(R.id.chat_w_provider_button);
        reserveButton = view.findViewById(R.id.reserve_button);
        viewProviderButton = view.findViewById(R.id.view_provider_button);

        fillDetails();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userService.getProviderByItemId(AuthUtil.getAuthorizationValue(requireContext()), service.getId())
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

        UserRole role = AuthUtil.extractRole(requireContext());
        viewProviderButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("itemId", service.getId());
            View view1 = getView();
            if (view1 == null) return;
            Navigation.findNavController(view1).navigate(R.id.navigate_to_spp_details_from_service, args);
        });
        chatWithProviderButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("contactDTO", new ChatContactDTO(providerDTO.getId(), providerDTO.getEmail(), providerDTO.getPhotoURL(), false));
            Navigation.findNavController(view).navigate(R.id.nav_chats, bundle);
        });
        if (role == UserRole.SPP) {
            viewProviderButton.setVisibility(View.GONE);
            chatWithProviderButton.setVisibility(View.GONE);
        } else if (role == UserRole.EVENT_ORG) {
            reserveButton.setVisibility(View.VISIBLE);
            favouriteStar.setVisibility(View.VISIBLE);
            initButtonsForOrganizer(view);
        }

        if (getArguments() != null) {
            boolean reservation = getArguments().getBoolean("reservation");
            if (reservation) {
                Snackbar.make(requireView(), R.string.successfully_reserved_service, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void initButtonsForOrganizer(@NonNull View view) {
        String bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        String organizerEmail = AuthUtil.extractEmail(requireContext());

        getValidEvents(bearerToken, organizerEmail);
        reserveButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putParcelable("service", service);
            args.putParcelable("organizerEvents", new FutureReservableEventsDTO(organizerEvents));
            Navigation.findNavController(view).navigate(R.id.navigate_to_service_reservation_from_service_details, args);
        });

        favouriteStar.setOnClickListener(v -> handleMarkingAsFavorite(bearerToken));
    }

    private void handleMarkingAsFavorite(String bearerToken) {
        service.setFavourite(!service.isFavourite());
        if (service.isFavourite())
            favouriteStar.setImageResource(R.drawable.ic_star_filled);
        else
            favouriteStar.setImageResource(R.drawable.ic_star);

        userService.markItemAsFavourite(bearerToken,
                                        AuthUtil.extractId(requireContext()),
                                        new FavouriteItemRequestDTO(service.getId(), service.isFavourite()))
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<FavouriteItemResponseDTO> call,
                                           @NonNull Response<FavouriteItemResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String message = service.isFavourite() ? "Service added to favourites!" : "Service removed from favourites!";
                            if (getView() != null) {
                                Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FavouriteItemResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });
    }

    private void getValidEvents(String bearerToken, String organizerEmail) {
        eventService.getOrganizerFutureReservableEvents(bearerToken, service.getId(), organizerEmail)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<FutureReservableEventsDTO> call,
                            @NonNull Response<FutureReservableEventsDTO> response
                    ) {
                        if (response.isSuccessful() && response.body() != null) {
                            organizerEvents = response.body().getEvents();
                            reserveButton.setEnabled(!organizerEvents.isEmpty() && service.isAvailable());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FutureReservableEventsDTO> call,
                                          @NonNull Throwable t) {

                    }
        });
    }

    private void fillDetails() {
        nameView.setText(service.getName());
        descriptionView.setText(service.getDescription());
        specificsView.setText(service.getSpecifics());
        priceView.setText(String.format("%s €", service.getPricing().getPrice()));
        discountView.setText(String.format("%s %%", service.getPricing().getDiscount()));
        minDurationView.setText(String.format("%s min", service.getMinDurationInMinutes()));
        maxDurationView.setText(String.format("%s min", service.getMaxDurationInMinutes()));
        reservationDeadlineView.setText(String.format("%s d", service.getReservationDeadlineInDays()));
        cancellationDeadlineView.setText(String.format("%s d", service.getCancellationDeadlineInDays()));
        categoryView.setText(service.getCategory().getName());

        if (!service.getAppliesTo().isEmpty()) {
            StringBuilder eventTypesStr = new StringBuilder();
            for (EventType eventType : service.getAppliesTo()) {
                eventTypesStr.append(eventType.getName()).append('\n');
            }
            eventTypesStr.deleteCharAt(eventTypesStr.length()-1);
            eventTypesView.setText(eventTypesStr.toString());
        }
        else {
            eventTypesView.setText("Currently no event types!");
        }

        if (!service.getWorkDaysDTOs().isEmpty()) {
            StringBuilder workDaysStr = new StringBuilder();
            for (WorkDayDTO workDayDTO : service.getWorkDaysDTOs()) {
                workDaysStr.append(workDayDTO).append('\n');
            }
            workDaysStr.deleteCharAt(workDaysStr.length()-1);
            workDaysView.setText(workDaysStr.toString());
        } else {
            workDaysView.setText("Currently unavailable!");
        }

        if (!service.getImages().isEmpty()) {
            imageAdapter = new ImageAdapter(requireContext(), new ArrayList<>(service.getImages()));
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