package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.model.interfaces.SearchActionsListener;

public class SPPServicesFragment extends Fragment implements SearchActionsListener {

    public SPPServicesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spp_services, container, false);
    }

    @Override
    public void onSortApplied() {

    }

    @Override
    public void onNextPage() {

    }

    @Override
    public void onFiltersApplied() {

    }

    @Override
    public void onFiltersReset() {

    }
}