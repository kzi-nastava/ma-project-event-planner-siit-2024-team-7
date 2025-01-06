package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;

import rs.ac.uns.eventplanner.team7.R;

public class SPPServicesBaseFragment extends Fragment {

    public SPPServicesBaseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spp_services_base, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton newServiceButton = view.findViewById(R.id.new_service_button);
        newServiceButton.setOnClickListener(v -> {
            openServiceManagement("SERVICE CREATION");
        });

//        View card1 = view.findViewById(R.id.service_card_1);
//        View card2 = view.findViewById(R.id.service_card_2);
//        View card3 = view.findViewById(R.id.service_card_3);
//        View card4 = view.findViewById(R.id.service_card_4);
//        View card5 = view.findViewById(R.id.service_card_5);

//        card1.setOnClickListener(v -> openServiceManagement("SERVICE UPDATE"));
//        card2.setOnClickListener(v -> openServiceManagement("SERVICE UPDATE"));
//        card3.setOnClickListener(v -> openServiceManagement("SERVICE UPDATE"));
//        card4.setOnClickListener(v -> openServiceManagement("SERVICE UPDATE"));
//        card5.setOnClickListener(v -> openServiceManagement("SERVICE UPDATE"));
    }

    private void openServiceManagement(String title) {
        ServiceManagementFragment fragment = new ServiceManagementFragment();

        Bundle args = new Bundle();
        args.putString("message_key", title);
        fragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.home_main_fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}