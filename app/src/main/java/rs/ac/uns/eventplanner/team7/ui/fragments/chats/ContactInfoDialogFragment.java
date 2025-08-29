package rs.ac.uns.eventplanner.team7.ui.fragments.chats;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;


public class ContactInfoDialogFragment extends BottomSheetDialogFragment {

    public interface OnActionClickListener {
        void OnBlockUserClicked();
        void OnViewProfileClicked();
        void OnReportUserClicked();
    }

    private static final String ARG_CONTACT_NAME = "contactName";
    private static final String ARG_CONTACT_IMAGE_URL = "contactImageUrl";
    private static final String ARG_CONTACT_ROLE = "contactRole";

    private String contactName, contactImageUrl;
    private UserRole contactRole;

    @Setter
    private OnActionClickListener onActionClickListener;

    private ImageView profilePicView;

    public ContactInfoDialogFragment() {
        // Required empty public constructor
    }

    public static ContactInfoDialogFragment newInstance(String contactName, String contactImageUrl, UserRole contactRole) {
        ContactInfoDialogFragment fragment = new ContactInfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CONTACT_NAME, contactName);
        args.putString(ARG_CONTACT_IMAGE_URL, contactImageUrl);
        args.putString(ARG_CONTACT_ROLE, contactRole.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            contactName = args.getString(ARG_CONTACT_NAME);
            contactImageUrl = args.getString(ARG_CONTACT_IMAGE_URL);
            contactRole = UserRole.valueOf(args.getString(ARG_CONTACT_ROLE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_info_dialog, container, false);
        profilePicView = view.findViewById(R.id.chat_contract_profile_pic);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String backendUrl = "http://" + BuildConfig.IP_ADDR + ":8080/api/images?imageUrl=" + contactImageUrl;
        if (contactImageUrl != null && !contactImageUrl.isEmpty()) {
            Picasso.get()
                    .load(backendUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(profilePicView);
        }
        MaterialTextView contactNameView = view.findViewById(R.id.chat_contact_name);
        contactNameView.setText(contactName);

        MaterialButton closeDialogButton = view.findViewById(R.id.close_dialog_button);
        closeDialogButton.setOnClickListener(v -> dismiss());

        MaterialButton blockAccountButton = view.findViewById(R.id.block_account_button);
        blockAccountButton.setOnClickListener(v -> {
            onActionClickListener.OnBlockUserClicked();
            dismiss();
        });

        MaterialButton reportButton = view.findViewById(R.id.report_account_button);
        reportButton.setVisibility(contactRole == UserRole.AUTH ? View.VISIBLE : View.GONE);
        reportButton.setOnClickListener(v -> {
            onActionClickListener.OnReportUserClicked();
            dismiss();
        });

        boolean showProfileButton = contactRole == UserRole.SPP || contactRole == UserRole.EVENT_ORG;
        MaterialButton viewProfileButton = view.findViewById(R.id.view_profile_button);
        viewProfileButton.setVisibility(showProfileButton ? View.VISIBLE : View.GONE);
        viewProfileButton.setOnClickListener(v -> {
            onActionClickListener.OnViewProfileClicked();
            dismiss();
        });

    }
}