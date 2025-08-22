package rs.ac.uns.eventplanner.team7.ui.fragments.user;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatContactDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.CreateReportRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.ReportDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.data.services.ReportService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.fragments.reporting.ReportReasonDialogFragment;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class SPPDetailsFragment extends Fragment {

    private final UserService userService = ClientUtils.injectService(UserService.class);
    private final ReportService reportService = ClientUtils.injectService(ReportService.class);
    private GetProviderResponseDTO providerDTO = null;
    private Integer itemId;
    private MaterialTextView titleNameView, emailView, descriptionView, addressView, phoneView;
    private ImageView providerImage;
    private MaterialButton reportButton, chatButton;

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
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userService.getProviderByItemId(AuthUtil.getAuthorizationValue(requireContext()), itemId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<GetProviderResponseDTO> call,
                                           @NonNull Response<GetProviderResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            providerDTO = response.body();
                            fillDetails();
                            reportButton.setEnabled(true);
                            chatButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GetProviderResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                    }
                });

        reportButton.setOnClickListener(v -> {
            var dialog = ReportReasonDialogFragment.newInstance();
            dialog.setCancelable(false);
            dialog.setOnSubmitClickListener(this::reportProvider);
            dialog.show(getChildFragmentManager(), "ReportProviderDialog");
        });

        chatButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("contactDTO", new ChatContactDTO(providerDTO.getId(), providerDTO.getEmail(), providerDTO.getPhotoURL(), false));
            Navigation.findNavController(view).navigate(R.id.nav_chats, bundle);
        });
    }

    private void reportProvider(String reportReason) {
        reportButton.setEnabled(false);
        String token = AuthUtil.getAuthorizationValue(requireContext());
        String userEmail = AuthUtil.extractEmail(requireContext());
        CreateReportRequestDTO dto = new CreateReportRequestDTO(userEmail, providerDTO.getEmail(), reportReason);
        reportService.create(token, dto).enqueue(new Callback<>() {
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
        if (providerDTO == null) return;
        titleNameView.setText(providerDTO.getOrgName());
        emailView.setText(providerDTO.getEmail());
        descriptionView.setText(providerDTO.getOrgDesc());
        addressView.setText(String.format("%s %s, %s, %s", providerDTO.getLocation().getStreet(),
                providerDTO.getLocation().getHouseNumber(), providerDTO.getLocation().getCity(),
                providerDTO.getLocation().getCountry()));
        phoneView.setText(providerDTO.getPhone());
        if (providerDTO.getPhotoURL() != null && !providerDTO.getPhotoURL().isEmpty()) {
            String backendUrl = "http://" + BuildConfig.IP_ADDR + ":8080/api/images?imageUrl=" + providerDTO.getPhotoURL();
            Picasso.get()
                    .load(backendUrl)
                    .error(R.drawable.image_placeholder)
                    .placeholder(R.drawable.image_placeholder)
                    .into(providerImage);
        }
    }
}