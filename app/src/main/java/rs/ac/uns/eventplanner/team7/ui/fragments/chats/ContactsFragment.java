package rs.ac.uns.eventplanner.team7.ui.fragments.chats;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatContactDTO;
import rs.ac.uns.eventplanner.team7.data.services.ChatService;
import rs.ac.uns.eventplanner.team7.ui.adapters.ContactAdapter;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class ContactsFragment extends Fragment {

    private final ChatService chatService = ClientUtils.injectService(ChatService.class);

    private RecyclerView contactsView;
    private MaterialTextView messageTextView;
    private ContactAdapter adapter;
    private String bearerToken;

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        messageTextView = view.findViewById(R.id.no_contacts_message_view);
        contactsView = view.findViewById(R.id.contacts_recycler_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bearerToken = AuthUtil.getAuthorizationValue(requireContext());

        adapter = new ContactAdapter(requireContext(), new ArrayList<>(),
                c -> onCardClicked((ChatContactDTO) c));
        contactsView.setAdapter(adapter);

        setContent();
    }

    private void onCardClicked(ChatContactDTO contact) {
        Bundle bundle = new Bundle();
        bundle.putInt("contact", contact.getUserId());
        Navigation.findNavController(requireView()).navigate(R.id.navigate_to_chat_from_contacts, bundle);
    }

    private void setContent() {
        chatService.findContacts(bearerToken).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatContactDTO>> call,
                                   @NonNull Response<List<ChatContactDTO>> response) {
                if (!isAdded()) return;
                adapter.clear();
                if (response.body() == null || !response.isSuccessful()) {
                    messageTextView.setText(R.string.unable_to_contact_server);
                    return;
                }
                adapter.addAll(response.body());
                messageTextView.setText("");
            }

            @Override
            public void onFailure(@NonNull Call<List<ChatContactDTO>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                messageTextView.setText(R.string.unable_to_contact_server);
            }
        });
    }
}