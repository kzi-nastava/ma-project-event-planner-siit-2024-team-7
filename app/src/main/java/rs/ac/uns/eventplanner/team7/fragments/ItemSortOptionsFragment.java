package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import rs.ac.uns.eventplanner.team7.R;

public class ItemSortOptionsFragment extends BottomSheetDialogFragment {

    public ItemSortOptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_options, container, false);
    }
}