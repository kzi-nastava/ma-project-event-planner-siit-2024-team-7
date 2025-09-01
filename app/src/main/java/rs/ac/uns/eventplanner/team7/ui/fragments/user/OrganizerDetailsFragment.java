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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.CreateReportRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.ReportDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetUserDetailsResponseDTO;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.data.services.ReportService;
import rs.ac.uns.eventplanner.team7.ui.fragments.reporting.ReportReasonDialogFragment;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class OrganizerDetailsFragment extends Fragment {
    private static final String ARG_ORGANIZER_DTO = "organizerDTO";

    private final ReportService reportService = ClientUtils.injectService(ReportService.class);

    private GetUserDetailsResponseDTO organizerDTO;

    private MaterialTextView organizerNameView, emailView, addressView, phoneView;
    private ImageView organizerImage;
    private MaterialButton reportButton, chatButton;

    String bearerToken;

    public OrganizerDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            organizerDTO= getArguments().getParcelable(ARG_ORGANIZER_DTO, GetUserDetailsResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_details, container, false);
        organizerNameView = view.findViewById(R.id.organizer_name);
        emailView = view.findViewById(R.id.organizer_email);
        addressView = view.findViewById(R.id.organizer_address);
        phoneView = view.findViewById(R.id.organizer_phone);
        organizerImage = view.findViewById(R.id.organizer_profile_pic);
        reportButton = view.findViewById(R.id.report_account_button);
        chatButton = view.findViewById(R.id.chat_w_organizer_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        UserRole role = AuthUtil.extractRole(requireContext());
        if (role == UserRole.GUEST) {
            reportButton.setVisibility(View.GONE);
            chatButton.setVisibility(View.GONE);
        }

        fillDetails();

        reportButton.setOnClickListener(v -> {
            var dialog = ReportReasonDialogFragment.newInstance(true);
            dialog.setCancelable(false);
            dialog.setOnSubmitClickListener(this::reportOrganizer);
            dialog.show(getChildFragmentManager(), "ReportOrganizerDialog");
        });

        chatButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("contactDTO", organizerDTO);
            Navigation.findNavController(view).navigate(R.id.nav_chats, bundle);
        });
    }

    private void reportOrganizer(String reportReason) {
        reportButton.setEnabled(false);
        String userEmail = AuthUtil.extractEmail(requireContext());
        CreateReportRequestDTO dto = new CreateReportRequestDTO(userEmail, organizerDTO.getEmail(), reportReason);
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
                if (message == null) return;
                Log.d("ERROR", message);
            }
        });
    }

    private void fillDetails() {
        if (organizerDTO == null) return;
        String fullName = String.format("%s %s", organizerDTO.getFirstName(), organizerDTO.getLastName());
        organizerNameView.setText(fullName);
        emailView.setText(organizerDTO.getEmail());
        addressView.setText(String.format("%s %s, %s, %s", organizerDTO.getLocation().getStreet(),
                organizerDTO.getLocation().getHouseNumber(), organizerDTO.getLocation().getCity(),
                organizerDTO.getLocation().getCountry()));
        phoneView.setText(organizerDTO.getPhone());
        if (organizerDTO.getPhotoURL() != null && !organizerDTO.getPhotoURL().isEmpty()) {
            String backendUrl = "http://" + BuildConfig.IP_ADDR + ":8080/api/images?imageUrl=" + organizerDTO.getPhotoURL();
            Picasso.get()
                    .load(backendUrl)
                    .error(R.drawable.image_placeholder)
                    .placeholder(R.drawable.image_placeholder)
                    .into(organizerImage);
        }
    }
}