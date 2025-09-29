package rs.ac.uns.eventplanner.team7.ui.fragments.events;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.ui.adapters.InvitationEmailsAdapter;

public class InvitePeopleDialogFragment extends DialogFragment {
    public interface OnConfirmClickListener {
        void onConfirmClicked(ArrayList<String> emails);
    }

    private static final String ARG_EMAILS = "emails";
    private static final String ARG_MAX_PEOPLE = "maxPeople";

    @Setter
    private OnConfirmClickListener onConfirmClickListener;

    private TextInputLayout emailInputLayout;
    private TextInputEditText emailInput;
    private MaterialTextView currentCountView, blankEmailError, emailFormatError,
            emailAlreadyAddedError, capacityReachedError;
    private MaterialButton confirmButton;
    private InvitationEmailsAdapter adapter;

    private final ArrayList<String> emails = new ArrayList<>();
    private int maxPeople;

    public InvitePeopleDialogFragment() {
        // Required empty public constructor
    }

    public static InvitePeopleDialogFragment newInstance(ArrayList<String> emails, int maxPeople) {
        InvitePeopleDialogFragment fragment = new InvitePeopleDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_EMAILS, emails);
        args.putInt(ARG_MAX_PEOPLE, maxPeople);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.AppTheme_MaterialDialogStyle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            emails.addAll(Objects.requireNonNull(requireArguments().getStringArrayList(ARG_EMAILS)));
            maxPeople = getArguments().getInt(ARG_MAX_PEOPLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_people_dialog, container, false);
        emailInputLayout = view.findViewById(R.id.email_input_layout);
        emailInput = view.findViewById(R.id.email_input);
        currentCountView = view.findViewById(R.id.current_count_view);
        blankEmailError = view.findViewById(R.id.blank_email_error);
        emailFormatError = view.findViewById(R.id.invalid_format_error);
        emailAlreadyAddedError = view.findViewById(R.id.already_added_error);
        capacityReachedError = view.findViewById(R.id.capacity_reached_error);
        confirmButton = view.findViewById(R.id.confirm_btn);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialTextView maxCountView = view.findViewById(R.id.max_count_view);
        maxCountView.setText(String.format(Locale.getDefault(), "%d", maxPeople));

        MaterialButton cancelButton = view.findViewById(R.id.cancel_btn);
        cancelButton.setOnClickListener(v -> dismiss());

        if (!emails.isEmpty()) updateItemCount();

        RecyclerView inputtedEmailsView = view.findViewById(R.id.inputted_emails_recycler_view);
        adapter = new InvitationEmailsAdapter(emails);
        inputtedEmailsView.setAdapter(adapter);
        inputtedEmailsView.setItemAnimator(null);

        emailInputLayout.setEndIconOnClickListener(this::tryToAddEmail);

        emailInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                tryToAddEmail(v);
                emailInput.clearFocus();
            }
            return false;
        });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateItemCount();
            }
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateItemCount();
            }
        });

        confirmButton.setOnClickListener(v -> {
            onConfirmClickListener.onConfirmClicked(emails);
            dismiss();
        });
    }

    private void updateItemCount() {
        boolean validCount = emails.size() <= maxPeople;
        emailInputLayout.setEndIconVisible(validCount);
        currentCountView.setText(String.format(Locale.getDefault(), "%d", emails.size()));
        confirmButton.setEnabled(validCount);
    }

    private void tryToAddEmail(View textView) {
        String email = getEmail();
        if (email != null) {
            adapter.add(email);
            emailInput.setText("");
        }
    }

    private String getEmail() {
        var text = emailInput.getText();
        String email = text != null? text.toString().trim() : "";
        return !validateEmail(email) ? email : null;
    }

    private boolean validateEmail(String email) {
        boolean isEmpty = email.isEmpty();
        blankEmailError.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        boolean invalidFormat = !Patterns.EMAIL_ADDRESS.matcher(email).matches();
        emailFormatError.setVisibility(invalidFormat ? View.VISIBLE : View.GONE);
        boolean alreadyAdded = emails.contains(email);
        emailAlreadyAddedError.setVisibility(alreadyAdded ? View.VISIBLE : View.GONE);
        return isEmpty || invalidFormat || alreadyAdded || isFullCapacity();
    }

    private boolean isFullCapacity() {
        boolean capacityReached = emails.size() >= maxPeople;
        capacityReachedError.setVisibility(capacityReached ? View.VISIBLE : View.GONE);
        return capacityReached;
    }
}