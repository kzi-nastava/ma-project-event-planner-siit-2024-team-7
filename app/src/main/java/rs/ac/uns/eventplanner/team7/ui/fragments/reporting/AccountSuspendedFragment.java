package rs.ac.uns.eventplanner.team7.ui.fragments.reporting;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.ui.activities.HomeActivity;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;


public class AccountSuspendedFragment extends Fragment {

    private MaterialTextView suspensionCountdown;
    private MaterialButton signOutButton;

    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            updateCountdown();
            timeHandler.postDelayed(this, 1000);
        }
    };

    private Instant suspensionEnd;

    public AccountSuspendedFragment() {
        // Required empty public constructor
    }

    public static AccountSuspendedFragment newInstance() {
        return new AccountSuspendedFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account_suspended, container, false);
        suspensionCountdown = view.findViewById(R.id.suspension_countdown_timer);
        signOutButton = view.findViewById(R.id.sign_out_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        suspensionEnd = AuthUtil.getSuspensionEnd(requireContext());
        if (suspensionEnd == null) returnToMainActivity();
        updateCountdown();

        signOutButton.setOnClickListener(v -> returnToMainActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        timeHandler.post(timeUpdater);
    }

    @Override
    public void onPause() {
        super.onPause();
        timeHandler.removeCallbacks(timeUpdater);
    }

    private void updateCountdown() {
        long difference = Duration.between(Instant.now(), suspensionEnd).getSeconds();

        if (difference <= 0) {
            returnToMainActivity();
        }

        long days = difference / (60 * 60 * 24);
        difference -= days * 60 * 60 * 24;
        long hours = difference / (60 * 60);
        difference -= hours * 60 * 60;
        long minutes = difference / (60);
        difference -= minutes * 60;

        String remainingText = String.format(
                Locale.getDefault(),
                "%dd %dh %dm %ds", days, hours, minutes, difference
        );
        suspensionCountdown.setText(remainingText);
    }

    private void returnToMainActivity() {
        FragmentActivity activity = getActivity();
        if (activity == null) return;
        AuthUtil.clearPreferences(activity);
        Intent intent = new Intent(activity, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        activity.finish();
    }

}