package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import rs.ac.uns.eventplanner.team7.R;

public class EventSortOptionsFragment extends BottomSheetDialogFragment {

    public EventSortOptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_sort_options, container, false);
    }
}