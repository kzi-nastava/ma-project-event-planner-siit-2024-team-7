package rs.ac.uns.eventplanner.team7.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.SimpleCarouselAdapter;
import rs.ac.uns.eventplanner.team7.utils.DrawableComparator;

public class UserProfileFragment extends Fragment {

    public boolean isEO;

    public UserProfileFragment() {
        isEO = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        setupRole(view);
        setupInputs(view);
        setupAllCarousels(view);
        return view;
    }

    private void setupRole(View view) {
        MaterialTextView role = view.findViewById(R.id.user_role);
        if (isEO) {
            role.setText(R.string.you_eo);
            view.findViewById(R.id.eo_update_inputs).setVisibility(View.VISIBLE);
        }
        else {
            role.setText(R.string.you_spp);
            view.findViewById(R.id.spp_update_inputs).setVisibility(View.VISIBLE);
        }
    }

    private void setupInputs(View view) {
        int editIconRes = R.drawable.ic_edit;
        int checkIconRes = R.drawable.ic_check;

        List<Pair<TextInputLayout, TextInputEditText>> fields = new ArrayList<>(); // Use ArrayList for a mutable list

        fields.add(new Pair<>(view.findViewById(R.id.change_phone_layout), view.findViewById(R.id.change_phone)));
        fields.add(new Pair<>(view.findViewById(R.id.change_country_layout), view.findViewById(R.id.change_country)));
        fields.add(new Pair<>(view.findViewById(R.id.change_city_layout), view.findViewById(R.id.change_city)));
        fields.add(new Pair<>(view.findViewById(R.id.change_street_layout), view.findViewById(R.id.change_street)));
        fields.add(new Pair<>(view.findViewById(R.id.change_house_number_layout), view.findViewById(R.id.change_house_number)));

        if (isEO) {
            fields.add(new Pair<>(view.findViewById(R.id.change_first_name_layout), view.findViewById(R.id.change_first_name)));
            fields.add(new Pair<>(view.findViewById(R.id.change_last_name_layout), view.findViewById(R.id.change_last_name)));
        } else {
            fields.add(new Pair<>(view.findViewById(R.id.change_org_name_layout), view.findViewById(R.id.change_org_name)));
            fields.add(new Pair<>(view.findViewById(R.id.change_org_desc_layout), view.findViewById(R.id.change_org_desc)));
        }

        for (Pair<TextInputLayout, TextInputEditText> field : fields) {
            TextInputLayout layout = field.first;
            TextInputEditText input = field.second;

            layout.setTag(editIconRes); // Initialize with edit icon
            layout.setEndIconOnClickListener(v -> {
                if ((int) layout.getTag() == editIconRes) {
                    layout.setEndIconDrawable(checkIconRes);
                    input.setEnabled(true);
                    layout.setTag(checkIconRes);
                } else if ((int) layout.getTag() == checkIconRes) {
                    layout.setEndIconDrawable(editIconRes);
                    input.setEnabled(false);
                    layout.setTag(editIconRes);
                }
            });
        }
    }

    private void setupAllCarousels(View view) {
        setupCarousel(view, R.id.favouriteEventsCarousel);
        setupCarousel(view, R.id.favouriteServicesCarousel);
    }

    private void setupCarousel(View view, int carouselId) {
        RecyclerView favoritesCarousel = view.findViewById(carouselId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        favoritesCarousel.setLayoutManager(layoutManager);

        SimpleCarouselAdapter adapter = new SimpleCarouselAdapter(R.drawable.image_placeholder, 4);
        favoritesCarousel.setAdapter(adapter);
    }

}