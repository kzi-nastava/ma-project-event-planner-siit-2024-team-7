package rs.ac.uns.eventplanner.team7.ui.fragments.feedback;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.CreateEventFeedbackRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.CreateItemFeedbackRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.CreateProviderFeedbackRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.FeedbackDTO;
import rs.ac.uns.eventplanner.team7.data.services.FeedbackService;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class FeedbackDialog extends DialogFragment {

    private final FeedbackService feedbackService = ClientUtils.injectService(FeedbackService.class);

    private final Integer id; // can be itemId, eventId or providerId
    private final String providerEmail;
    private final String type, itemType; // item, event, provider + product/service

    private ImageView starOne, starTwo, starThree, starFour, starFive;
    private int selectedRating;
    private TextInputEditText commentTextInput;
    private MaterialButton btnSubmit, btnCancel;

    private String bearerToken, userEmail;

    public FeedbackDialog(Integer id, String providerEmail, String type, String itemType) {
        this.selectedRating = 0;
        this.id = id;
        this.providerEmail = providerEmail;
        this.type = type;
        this.itemType = itemType;
    }

    @Override
    public int getTheme() {
        return R.style.AppTheme_MaterialDialogStyle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback_dialog, container, false);
        starOne = view.findViewById(R.id.feedback_star_one);
        starTwo = view.findViewById(R.id.feedback_star_two);
        starThree = view.findViewById(R.id.feedback_star_three);
        starFour = view.findViewById(R.id.feedback_star_four);
        starFive = view.findViewById(R.id.feedback_star_five);
        commentTextInput = view.findViewById(R.id.feedback_comment_input);
        btnSubmit = view.findViewById(R.id.button_submit_feedback);
        btnCancel = view.findViewById(R.id.button_cancel_feedback);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        userEmail = AuthUtil.extractEmail(requireContext());

        starOne.setOnClickListener(v -> {
            fillStars(1);
        });
        starTwo.setOnClickListener(v -> {
            fillStars(2);
        });
        starThree.setOnClickListener(v -> {
            fillStars(3);
        });
        starFour.setOnClickListener(v -> {
            fillStars(4);
        });
        starFive.setOnClickListener(v -> {
            fillStars(5);
        });

        btnCancel.setOnClickListener(v -> dismiss());
        btnSubmit.setOnClickListener(v -> submit());
    }

    private void showToast(String text) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void fillStars(int rating) {
        int filledStar = R.drawable.ic_star_filled;
        int emptyStar = R.drawable.ic_star;

        ImageView[] stars = { starOne, starTwo, starThree, starFour, starFive };

        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(filledStar);
            } else {
                stars[i].setImageResource(emptyStar);
            }
        }
        selectedRating = rating;
    }

    private void submit() {
        String comment = Objects.requireNonNull(commentTextInput.getText()).toString();
        if (selectedRating == 0 && comment.isEmpty()) {
            showToast("Feedback can't be empty!");
            return;
        }
        switch (type) {
            case "item":
                CreateItemFeedbackRequestDTO itemDTO = new CreateItemFeedbackRequestDTO(userEmail, selectedRating, comment, null, null);
                if (itemType.equals("product")) itemDTO.setProductId(id); else itemDTO.setServiceId(id);
                handleServiceCall(feedbackService.createForItem(bearerToken, itemDTO));
                return;
            case "event":
                CreateEventFeedbackRequestDTO eventDTO = new CreateEventFeedbackRequestDTO(userEmail, selectedRating, comment, id);
                handleServiceCall(feedbackService.createForEvent(bearerToken, eventDTO));
                return;
            case "provider":
                CreateProviderFeedbackRequestDTO providerDTO = new CreateProviderFeedbackRequestDTO(userEmail, selectedRating, comment, providerEmail);
                handleServiceCall(feedbackService.createForProvider(bearerToken, providerDTO));
                return;
            default:
                Log.d("Feedback", "Unknown type:" + type);

        }

    }

    private void handleServiceCall(Call<FeedbackDTO> serviceCall) {
        serviceCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<FeedbackDTO> call, @NonNull Response<FeedbackDTO> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    showToast("Successfully gave feedback!");
                    dismiss();
                } else {
                    try {
                        // Show error message
                        if (response.errorBody() == null) return;
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        showToast(jsonObject.getString("message"));
                    } catch (Exception e) {
                        String message = e.getMessage();
                        if (message == null) return;
                        Log.d("ERROR", message);
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<FeedbackDTO> call, @NonNull Throwable t) {
                String message = t.getMessage();
                Log.d("Feedback", message == null ? "Unknown error" : message);
                showToast(message == null ? "Unknown error" : message);
            }
        });
    }
}