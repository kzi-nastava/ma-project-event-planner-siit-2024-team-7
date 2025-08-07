package rs.ac.uns.eventplanner.team7.ui.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import rs.ac.uns.eventplanner.team7.ui.fragments.reservation.ReservationDateStepFragment;
import rs.ac.uns.eventplanner.team7.ui.fragments.reservation.ReservationEventStepFragment;
import rs.ac.uns.eventplanner.team7.ui.fragments.reservation.ReservationTimesStepFragment;
import rs.ac.uns.eventplanner.team7.ui.fragments.reservation.ReservationTimestampStepFragment;

public class ReservationStepsAdapter extends FragmentStateAdapter {

    public ReservationStepsAdapter(@NonNull Fragment host) {
        super(host);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return ReservationEventStepFragment.newInstance();
            case 1: return ReservationDateStepFragment.newInstance();
            case 2: return ReservationTimestampStepFragment.newInstance();
            case 3: return ReservationTimesStepFragment.newInstance();
            default: throw new IllegalArgumentException("Unknown step");
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

}
