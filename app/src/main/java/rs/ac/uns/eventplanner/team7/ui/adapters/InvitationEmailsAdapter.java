package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;


public class InvitationEmailsAdapter extends RecyclerView.Adapter<InvitationEmailsAdapter.ViewHolder> {

    private final Object mutex = new Object();
    private final List<String> emails;

    public InvitationEmailsAdapter(List<String> emails) {
        this.emails = emails;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.invitation_horizontal_button_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(emails.get(position));
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    public void add(String email) {
        int position;
        synchronized (mutex) {
            emails.add(email);
            position = getLastItemIndex();
        }
        notifyItemInserted(position);
        if (position > 0) {
            notifyItemChanged(position - 1); // Redraw the item that was previously last
        }
    }

    public void remove(int pos) {
        synchronized (mutex) {
            emails.remove(pos);
        }
        notifyItemRemoved(pos);
        // Redraw the item on its new position
        if (pos < getItemCount()) {
            notifyItemChanged(pos);
        }

        // If first item changed
        if (pos == 0 && getItemCount() > 0) {
            notifyItemChanged(0);
        }

        // If last item changed
        int lastIndex = getItemCount() - 1;
        if (pos == lastIndex + 1) {
            notifyItemChanged(lastIndex);
        }

    }


    private int getLastItemIndex() {
        return getItemCount()-1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView rootView;
        private final MaterialTextView email;
        private final ImageView removeIcon;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.invitation_card);
            email = itemView.findViewById(R.id.email_display);
            removeIcon = itemView.findViewById(R.id.remove_email_icon);
        }

        public void bind(String email) {
            this.email.setText(email);
            updateShape();
            removeIcon.setOnClickListener(v -> remove(getAdapterPosition()));
        }

        public void updateShape() {
            float radius = 32;
            ShapeAppearanceModel.Builder shapeBuilder = rootView.getShapeAppearanceModel().toBuilder();
            int totalCount = getItemCount();
            int lastIndex = getLastItemIndex();
            int position = getAdapterPosition();

            if (totalCount == 1) {
                shapeBuilder.setAllCorners(CornerFamily.ROUNDED, radius);
            } else if (position == 0) {
                shapeBuilder
                        .setTopLeftCorner(CornerFamily.ROUNDED, radius)
                        .setTopRightCorner(CornerFamily.ROUNDED, radius)
                        .setBottomLeftCorner(CornerFamily.ROUNDED, 0)
                        .setBottomRightCorner(CornerFamily.ROUNDED, 0);
            } else if (position == lastIndex) {
                shapeBuilder
                        .setTopLeftCorner(CornerFamily.ROUNDED, 0)
                        .setTopRightCorner(CornerFamily.ROUNDED, 0)
                        .setBottomLeftCorner(CornerFamily.ROUNDED, radius)
                        .setBottomRightCorner(CornerFamily.ROUNDED, radius);
            } else {
                shapeBuilder.setAllCorners(CornerFamily.ROUNDED, 0);
            }

            rootView.setShapeAppearanceModel(shapeBuilder.build());
        }
    }
}
