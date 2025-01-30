package rs.ac.uns.eventplanner.team7.fragments.services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

        FloatingActionButton newServiceButton = view.findViewById(R.id.new_service_button);
        newServiceButton.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.navigate_to_service_management_creation));

    }
}