package rs.ac.uns.eventplanner.team7.utils;

import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;

public class ImageLoader {
    private static final String backendUrl = "http://" + BuildConfig.IP_ADDR + ":8080/api/images?imageUrl=";

    public static void loadImage(String imageUrl, ImageView element) {
        Picasso.get()
                .load(backendUrl + imageUrl)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(element);
    }
}
