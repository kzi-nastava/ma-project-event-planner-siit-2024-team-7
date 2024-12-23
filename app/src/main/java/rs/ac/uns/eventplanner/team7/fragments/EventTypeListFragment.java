package rs.ac.uns.eventplanner.team7.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rs.ac.uns.eventplanner.team7.R;


public class EventTypeListFragment extends Fragment {

    public EventTypeListFragment() {
        // Required empty public constructor
    }

    public static EventTypeListFragment newInstance(String param1, String param2) {
        EventTypeListFragment fragment = new EventTypeListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_type_list, container, false);
    }
}