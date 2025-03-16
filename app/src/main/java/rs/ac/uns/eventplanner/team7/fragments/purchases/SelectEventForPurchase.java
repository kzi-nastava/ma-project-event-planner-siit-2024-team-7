package rs.ac.uns.eventplanner.team7.fragments.purchases;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textview.MaterialTextView;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.product.GetProductResponseDTO;

public class SelectEventForPurchase extends Fragment {

    private GetProductResponseDTO productDTO;

    private MaterialTextView purchaseWelcome;

    public SelectEventForPurchase() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productDTO = getArguments().getParcelable("productDTO", GetProductResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_event_for_purchase, container, false);
        purchaseWelcome = view.findViewById(R.id.purchase_welcome);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        purchaseWelcome.setText(String.format("Purchase %s", productDTO.getName()));
    }
}