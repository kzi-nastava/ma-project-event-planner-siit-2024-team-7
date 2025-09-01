package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.Collection;
import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatContactDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.utils.ImageLoader;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private final Object mutex = new Object();
    private final Context context;
    private final List<ChatContactDTO> contacts;
    private final CardClickListener cardClickListener;

    public ContactAdapter(Context context, List<ChatContactDTO> contacts, CardClickListener cardClickListener) {
        this.context = context;
        this.contacts = contacts;
        this.cardClickListener = cardClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatContactDTO contact = contacts.get(position);
        holder.bindData(contact);
        holder.itemView.setOnClickListener(v -> {
            cardClickListener.onCardClicked(contact);
            v.setEnabled(false);
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void addAll(@NonNull Collection<ChatContactDTO> contacts) {
        int initialSize;
        synchronized (mutex) {
            initialSize = this.contacts.size();
            this.contacts.addAll(contacts);
        }
        notifyItemRangeInserted(initialSize, contacts.size());
    }

    public void clear() {
        if (getItemCount() == 0) return;
        int size;
        synchronized (mutex) {
            size = contacts.size();
            contacts.clear();
        }
        notifyItemRangeRemoved(0, size);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView contactTextView;
        ImageView profilePic, unreadDot;

        public ViewHolder(View itemView) {
            super(itemView);
            contactTextView = itemView.findViewById(R.id.contact_email);
            profilePic = itemView.findViewById(R.id.profile_pic);
            unreadDot = itemView.findViewById(R.id.unread_dot);
        }

        public void bindData(ChatContactDTO contactDTO) {
            boolean read = contactDTO.isRead();
            contactTextView.setText(contactDTO.getUserEmail());
            profilePic.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(),R.drawable.image_placeholder));
            if (contactDTO.getPhotoUrl() != null && !contactDTO.getPhotoUrl().isEmpty()) {
                ImageLoader.loadImage(contactDTO.getPhotoUrl(), profilePic);
            }
            int style = read ? Typeface.NORMAL : Typeface.BOLD;
            Typeface tf = Typeface.create("sans-serif-medium", style);
            contactTextView.setTypeface(tf);
            unreadDot.setVisibility(read ? View.INVISIBLE : View.VISIBLE);
        }
    }
}
