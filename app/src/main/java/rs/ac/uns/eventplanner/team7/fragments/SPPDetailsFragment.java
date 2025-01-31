package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.services.UserService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class SPPDetailsFragment extends Fragment {

    private final UserService userService = ClientUtils.injectService(UserService.class);
    private GetProviderResponseDTO providerDTO = null;
    private Integer itemId;
    private MaterialTextView titleNameView, emailView, descriptionView, addressView, phoneView;

    public SPPDetailsFragment(Integer itemId) {
        this.itemId = itemId;
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
    }
}