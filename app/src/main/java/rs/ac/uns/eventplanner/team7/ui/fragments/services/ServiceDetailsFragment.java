package rs.ac.uns.eventplanner.team7.ui.fragments.services;

import android.content.Context;
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
import rs.ac.uns.eventplanner.team7.data.dto.event.FutureReservableEventsDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.AverageRatingDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.FeedbackDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.CreateReportRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.ReportDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.WorkDayDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetUserDetailsResponseDTO;
import rs.ac.uns.eventplanner.team7.data.model.EventType;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.data.services.FeedbackService;
import rs.ac.uns.eventplanner.team7.data.services.ReportService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.adapters.CommentAdapter;
import rs.ac.uns.eventplanner.team7.ui.adapters.ImageAdapter;
import rs.ac.uns.eventplanner.team7.ui.fragments.feedback.FeedbackDialog;
import rs.ac.uns.eventplanner.team7.ui.fragments.reporting.ReportReasonDialogFragment;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class ServiceDetailsFragment extends Fragment {
    private final UserService userService = ClientUtils.injectService(UserService.class);
    private final EventService eventService = ClientUtils.injectService(EventService.class);
    private final FeedbackService feedbackService = ClientUtils.injectService(FeedbackService.class);
    private final ReportService reportService = ClientUtils.injectService(ReportService.class);
    private GetServiceResponseDTO service;
    private GetUserDetailsResponseDTO providerDTO;
    private List<GetEventResponseDTO> organizerEvents;

    private ImageView favouriteStar, star1, star2, star3, star4, star5;
    private MaterialTextView nameView, descriptionView, specificsView, priceView, discountView, minDurationView, maxDurationView,
            reservationDeadlineView, cancellationDeadlineView, categoryView, eventTypesView, workDaysView, noImagesView, ratingCount, noComments;
    private RecyclerView imagesView, commentsView;
    private CommentAdapter commentAdapter;
    private ImageAdapter imageAdapter;

    private MaterialButton viewProviderButton, chatWithProviderButton, rateButton;
    private FloatingActionButton reserveButton;
    private String bearerToken;

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

        rateButton = view.findViewById(R.id.rate_service_button);
        commentsView = view.findViewById(R.id.service_comments);
        noComments = view.findViewById(R.id.no_comments_service);
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
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        commentAdapter = new CommentAdapter(requireContext(), new ArrayList<>());
        commentsView.setAdapter(commentAdapter);
        commentAdapter.setOnReportCommentClickListener(this::showCommentReportDialog);

        userService.getProviderByItemId(AuthUtil.getAuthorizationValue(requireContext()), service.getId())
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<GetUserDetailsResponseDTO> call,
                                           @NonNull Response<GetUserDetailsResponseDTO> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            providerDTO = response.body();
                            chatWithProviderButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GetUserDetailsResponseDTO> call, @NonNull Throwable t) {
                        Log.e("ERROR", "Request failed", t);
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
            bundle.putParcelable("contactDTO", providerDTO);
            Navigation.findNavController(view).navigate(R.id.nav_chats, bundle);
        });


        if (role == UserRole.GUEST) {
            favouriteStar.setVisibility(View.GONE);
            chatWithProviderButton.setVisibility(View.GONE);
        } else if (role == UserRole.SPP) {
            viewProviderButton.setVisibility(View.GONE);
            chatWithProviderButton.setVisibility(View.GONE);
        } else if (role == UserRole.EVENT_ORG) {
            initReserveButton();
        }
        if (role != UserRole.EVENT_ORG || !service.isAvailable()) {
            rateButton.setVisibility(View.GONE);
            reserveButton.setVisibility(View.GONE);
        }

        if (getArguments() != null) {
            boolean reservation = getArguments().getBoolean("reservation");
            if (reservation) {
                Snackbar.make(requireView(), R.string.successfully_reserved_service, Snackbar.LENGTH_SHORT).show();
            }
        }

        rateButton.setEnabled(service.isReserved());

        setFeedbacks();

        rateButton.setOnClickListener(v -> {
            FeedbackDialog dialog = new FeedbackDialog(service.getId(), null, "item", "service");
            dialog.show(requireActivity().getSupportFragmentManager(), "FeedbackDialog");
        });

        favouriteStar.setOnClickListener(v -> handleMarkingAsFavorite(bearerToken));
    }

    private void initReserveButton() {
        String organizerEmail = AuthUtil.extractEmail(requireContext());

        getValidEvents(bearerToken, organizerEmail);
        reserveButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putParcelable("service", service);
            args.putParcelable("organizerEvents", new FutureReservableEventsDTO(organizerEvents));
            Navigation.findNavController(v).navigate(R.id.navigate_to_service_reservation_from_service_details, args);
        });
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
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            String message = service.isFavourite() ? "Service added to favourites!" : "Service removed from favourites!";
                            if (getView() != null) {
                                Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FavouriteItemResponseDTO> call, @NonNull Throwable t) {
                        Log.e("ERROR", "Request failed", t);
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
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            organizerEvents = response.body().getEvents();
                            reserveButton.setEnabled(!organizerEvents.isEmpty() && service.isAvailable());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FutureReservableEventsDTO> call,
                                          @NonNull Throwable t) {
                        Log.e("ERROR", "Request failed", t);
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

    private void setFeedbacks() {
        String token = AuthUtil.getAuthorizationValue(requireContext());
        feedbackService.getAllApprovedForItem(token, service.getId()).enqueue(new Callback<>() {
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
                Log.e("ERROR", "Request failed", t);
            }
        });

        feedbackService.getAverageRatingForItem(token, service.getId()).enqueue(new Callback<>() {
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
                Log.e("ERROR", "Request failed", t);
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

    private void showCommentReportDialog(FeedbackDTO feedback) {
        var dialog = ReportReasonDialogFragment.newInstance(false);
        dialog.setCancelable(false);
        dialog.setOnSubmitClickListener(reportReason -> {
            String userEmail = AuthUtil.extractEmail(requireContext());
            var dto = new CreateReportRequestDTO(userEmail, feedback.getUserEmail(), feedback.getId(), reportReason);
            reportService.create(bearerToken, dto).enqueue(new Callback<>() {
                @Override
                public void onResponse(
                        @NonNull Call<ReportDTO> call,
                        @NonNull Response<ReportDTO> response
                ) {
                    if (!isAdded()) return;
                    View view = getView();
                    Context context = getContext();
                    if (response.isSuccessful() && response.body() != null) {
                        if (view == null) return;
                        Snackbar.make(view, R.string.report_submitted, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if (response.code() == 400) {
                        var errorDto = ClientUtils.convertToErrorMessage(response.errorBody());
                        if (errorDto != null && view != null && context != null) {
                            Snackbar.make(view, errorDto.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ReportDTO> call, @NonNull Throwable t) {
                    String message = t.getMessage();
                    if (message != null) Log.d("ERROR", message);
                }
            });
        });
        dialog.show(getChildFragmentManager(), "ReportCommentDialog");
    }
}