package rs.ac.uns.eventplanner.team7.ui.fragments.user;

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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.AverageRatingDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.FeedbackDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.CreateReportRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.ReportDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetUserDetailsResponseDTO;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.data.services.FeedbackService;
import rs.ac.uns.eventplanner.team7.data.services.ReportService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.adapters.CommentAdapter;
import rs.ac.uns.eventplanner.team7.ui.fragments.feedback.FeedbackDialog;
import rs.ac.uns.eventplanner.team7.ui.fragments.reporting.ReportReasonDialogFragment;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.ImageLoader;

public class SPPDetailsFragment extends Fragment {

    private final UserService userService = ClientUtils.injectService(UserService.class);
    private final ReportService reportService = ClientUtils.injectService(ReportService.class);
    private final FeedbackService feedbackService = ClientUtils.injectService(FeedbackService.class);
    private GetUserDetailsResponseDTO providerDTO = null;
    private Integer itemId;
    private MaterialTextView titleNameView, emailView, descriptionView, addressView, phoneView, noComments, ratingCount;
    private ImageView providerImage, star1, star2, star3, star4, star5;
    private MaterialButton reportButton, chatButton, rateButton;
    private RecyclerView commentsView;
    private CommentAdapter commentAdapter;
    private String bearerToken;

    public SPPDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemId = getArguments().getInt("itemId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spp_details, container, false);

        titleNameView = view.findViewById(R.id.provider_org_name_title);
        emailView = view.findViewById(R.id.provider_email);
        descriptionView = view.findViewById(R.id.provider_description);
        addressView = view.findViewById(R.id.provider_address);
        phoneView = view.findViewById(R.id.provider_phone);
        providerImage = view.findViewById(R.id.spp_profile_pic);
        reportButton = view.findViewById(R.id.report_account_button);
        chatButton = view.findViewById(R.id.chat_w_provider_button);
        rateButton = view.findViewById(R.id.rate_provider_button);
        commentsView = view.findViewById(R.id.provider_comments);
        noComments = view.findViewById(R.id.no_comments_provider);
        ratingCount = view.findViewById(R.id.rating_count);
        star1 = view.findViewById(R.id.star1);
        star2 = view.findViewById(R.id.star2);
        star3 = view.findViewById(R.id.star3);
        star4 = view.findViewById(R.id.star4);
        star5 = view.findViewById(R.id.star5);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        commentAdapter = new CommentAdapter(requireContext(), new ArrayList<>());
        commentsView.setAdapter(commentAdapter);
        commentAdapter.setOnReportCommentClickListener(this::showCommentReportDialog);

        UserRole role = AuthUtil.extractRole(requireContext());
        if (role == UserRole.GUEST) {
            reportButton.setVisibility(View.GONE);
            chatButton.setVisibility(View.GONE);
        }

        if (role != UserRole.EVENT_ORG) {
            rateButton.setVisibility(View.GONE);
        }

        userService.getProviderByItemId(bearerToken, itemId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<GetUserDetailsResponseDTO> call,
                                           @NonNull Response<GetUserDetailsResponseDTO> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful() && response.body() != null) {
                            providerDTO = response.body();
                            fillDetails();
                            setFeedbacks();
                            reportButton.setEnabled(true);
                            chatButton.setEnabled(true);
                            rateButton.setEnabled(true);
                        } else {
                            if (getView() != null) Navigation.findNavController(getView()).navigateUp();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GetUserDetailsResponseDTO> call, @NonNull Throwable t) {
                        String message = t.getMessage();
                        if (message != null) Log.d("ERROR", message);
                    }
                });

        reportButton.setOnClickListener(v -> {
            var dialog = ReportReasonDialogFragment.newInstance(true);
            dialog.setCancelable(false);
            dialog.setOnSubmitClickListener(this::reportProvider);
            dialog.show(getChildFragmentManager(), "ReportProviderDialog");
        });

        chatButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("contactDTO", providerDTO);
            Navigation.findNavController(view).navigate(R.id.nav_chats, bundle);
        });

        rateButton.setOnClickListener(v -> {
            FeedbackDialog dialog = new FeedbackDialog(null, providerDTO.getEmail(), "provider", null);
            dialog.show(requireActivity().getSupportFragmentManager(), "FeedbackDialog");
        });
    }

    private void reportProvider(String reportReason) {
        reportButton.setEnabled(false);
        String userEmail = AuthUtil.extractEmail(requireContext());
        CreateReportRequestDTO dto = new CreateReportRequestDTO(userEmail, providerDTO.getEmail(), reportReason);
        handleReport(dto);
    }

    private void showCommentReportDialog(FeedbackDTO feedback) {
        var dialog = ReportReasonDialogFragment.newInstance(false);
        dialog.setCancelable(false);
        dialog.setOnSubmitClickListener(reportReason -> {
            String userEmail = AuthUtil.extractEmail(requireContext());
            handleReport(new CreateReportRequestDTO(userEmail, feedback.getUserEmail(), feedback.getId(), reportReason));
        });
        dialog.show(getChildFragmentManager(), "ReportCommentDialog");
    }

    private void handleReport(CreateReportRequestDTO reportRequestDTO) {
        reportService.create(bearerToken, reportRequestDTO).enqueue(new Callback<>() {
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
    }

    private void fillDetails() {
        if (providerDTO == null) return;
        titleNameView.setText(providerDTO.getOrgName());
        emailView.setText(providerDTO.getEmail());
        descriptionView.setText(providerDTO.getOrgDesc());
        addressView.setText(String.format("%s %s, %s, %s", providerDTO.getLocation().getStreet(),
                providerDTO.getLocation().getHouseNumber(), providerDTO.getLocation().getCity(),
                providerDTO.getLocation().getCountry()));
        phoneView.setText(providerDTO.getPhone());
        if (providerDTO.getPhotoURL() != null && !providerDTO.getPhotoURL().isEmpty()) {
            ImageLoader.loadImage(providerDTO.getPhotoURL(), providerImage);
        }
    }

    private void setFeedbacks() {
        feedbackService.getAllApprovedForProvider(bearerToken, providerDTO.getId()).enqueue(new Callback<>() {
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
                String message = t.getMessage();
                if (message != null) Log.d("ERROR", message);
            }
        });

        feedbackService.getAverageRatingForProvider(bearerToken, providerDTO.getId()).enqueue(new Callback<>() {
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
                String message = t.getMessage();
                if (message != null) Log.d("ERROR", message);
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