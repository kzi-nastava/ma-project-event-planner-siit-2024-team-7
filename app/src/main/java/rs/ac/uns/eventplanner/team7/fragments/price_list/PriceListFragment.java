package rs.ac.uns.eventplanner.team7.fragments.price_list;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rs.ac.uns.eventplanner.team7.R;

public class PriceListFragment extends Fragment {

    public PriceListFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_price_list, container, false);
    }
}