package rs.ac.uns.eventplanner.team7.ui.fragments.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class SPPDetailsFragment extends Fragment {

    private final UserService userService = ClientUtils.injectService(UserService.class);
    private GetProviderResponseDTO providerDTO = null;
    private Integer itemId;
    private MaterialTextView titleNameView, emailView, descriptionView, addressView, phoneView;
    private ImageView providerImage;

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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userService.getProviderByItemId(JwtUtil.getAuthorizationValue(requireContext()), itemId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<GetProviderResponseDTO> call,
                                           @NonNull Response<GetProviderResponseDTO> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            providerDTO = response.body();
                            fillDetails();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GetProviderResponseDTO> call, @NonNull Throwable t) {
                        Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
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
            Picasso.get()
                    .load(providerDTO.getPhotoURL())
                    .error(R.drawable.image_placeholder)
                    .placeholder(R.drawable.image_placeholder)
                    .into(providerImage);
        }
    }
}