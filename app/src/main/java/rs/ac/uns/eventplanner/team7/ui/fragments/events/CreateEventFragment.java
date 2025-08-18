package rs.ac.uns.eventplanner.team7.ui.fragments.events;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.model.enums.EventVisibility;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.data.services.InvitationService;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;


public class CreateEventFragment extends Fragment {

    private final EventService eventService = ClientUtils.injectService(EventService.class);
    private final InvitationService invitationService = ClientUtils.injectService(InvitationService.class);

    private final ArrayList<String> invitedPeopleEmails = new ArrayList<>();

    private TextInputEditText maxParticipantsInput;
    MaterialAutoCompleteTextView visibilityDropdown;
    private MaterialTextView currentCount, maxCount;
    private LinearLayout invitePeopleLayout;
    private MaterialButton inviteButton, submitButton;

    private String bearerToken, organizerEmail;

    public CreateEventFragment() {
        // Required empty public constructor
    }

    public static CreateEventFragment newInstance() {
        return new CreateEventFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        maxParticipantsInput = view.findViewById(R.id.max_participants_input);
        invitePeopleLayout = view.findViewById(R.id.invite_people_layout);
        currentCount = view.findViewById(R.id.invited_people_count);
        maxCount = view.findViewById(R.id.max_count);
        inviteButton = view.findViewById(R.id.invite_people_btn);
        visibilityDropdown = view.findViewById(R.id.event_visibility_dropdown);
        submitButton = view.findViewById(R.id.submit_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        organizerEmail = AuthUtil.extractEmail(requireContext());
        initVisibilityDropdown();

        initInviteButtonListener();

        listenForMaxParticipantsChanges();

        submitButton.setOnClickListener(v -> tryToSubmit());
    }

    private void initVisibilityDropdown() {
        visibilityDropdown.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                EventVisibility.values()
        ));

        visibilityDropdown.setOnItemClickListener((parent, v, position, id) -> {
            EventVisibility selected = (EventVisibility) parent.getItemAtPosition(position);
            invitePeopleLayout.setVisibility(selected == EventVisibility.PRIVATE ? View.VISIBLE : View.GONE);
        });
    }

    private void listenForMaxParticipantsChanges() {
        maxParticipantsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable text) {
                String countText = "?";
                if (text != null) {
                    try {
                        int count = Integer.parseUnsignedInt(text.toString());
                        countText = String.valueOf(count);
                        inviteButton.setEnabled(count > 0);
                    } catch (NumberFormatException e) {
                        inviteButton.setEnabled(false);
                    }
                }
                maxCount.setText(countText);
            }
        });
    }

    private void initInviteButtonListener() {
        inviteButton.setOnClickListener(v -> {
            View currentFocus = requireActivity().getCurrentFocus();
            if (currentFocus != null) currentFocus.clearFocus();
            int maxPeople = Integer.parseInt(Objects.requireNonNull(maxParticipantsInput.getText()).toString());

            var dialog = InvitePeopleDialogFragment.newInstance(invitedPeopleEmails, maxPeople);
            dialog.setCancelable(false);
            dialog.show(getChildFragmentManager(), "InvitePeopleDialog");
            dialog.setOnConfirmClickListener(newEmails -> {
                invitedPeopleEmails.clear();
                invitedPeopleEmails.addAll(newEmails);
                currentCount.setText(String.format(Locale.getDefault(), "%d", invitedPeopleEmails.size()));
            });
        });
    }

    private void tryToSubmit() {
        // if (invalid) return;
        // call eventService
        // if event creation succeeds, call invitation service and show budget dialog like in Angular
    }

}