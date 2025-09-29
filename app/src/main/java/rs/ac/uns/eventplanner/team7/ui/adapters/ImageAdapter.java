package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.utils.ImageLoader;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private final Context context;
    private final List<String> images;

    public ImageAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String backendUrl = "http://" + BuildConfig.IP_ADDR + ":8080/api/images?imageUrl=" + images.get(position);
        holder.bindData(backendUrl);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }

        public void bindData(String image) {
            ImageLoader.loadImage(image, imageView);
        }
    }
}