package rs.ac.uns.eventplanner.team7.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import rs.ac.uns.eventplanner.team7.fragments.home.all.AllEventsFragment;
import rs.ac.uns.eventplanner.team7.fragments.home.all.AllItemsFragment;
import rs.ac.uns.eventplanner.team7.fragments.home.top.TopEventsFragment;
import rs.ac.uns.eventplanner.team7.fragments.home.top.TopItemsFragment;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new TopEventsFragment();
            case 1: return new AllEventsFragment();
            case 2: return new TopItemsFragment();
            case 3: return new AllItemsFragment();
            default: throw new IllegalStateException("Unexpected position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
