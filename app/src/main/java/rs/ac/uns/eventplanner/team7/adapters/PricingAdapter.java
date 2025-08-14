package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.pricing.PricingResponseDTO;

public class PricingAdapter extends RecyclerView.Adapter<PricingAdapter.ViewHolder> {
    private final Context context;
    private final Object mutex = new Object();
    private final List<PricingResponseDTO> pricings;

    private final OnItemSelectedListener onItemSelectedListener;
    private Integer selectedItemId = null;

    public interface OnItemSelectedListener {
        void onItemSelected(Integer itemId, String type);
    }

    public PricingAdapter(Context context, List<PricingResponseDTO> pricings, OnItemSelectedListener listener) {
        this.context = context;
        this.pricings = pricings;
        this.onItemSelectedListener = listener;
    }

    public void addAll(@NonNull List<PricingResponseDTO> newPricings) {
        int initialSize;
        synchronized (mutex) {
            initialSize = pricings.size();
            pricings.addAll(newPricings);
        }
        notifyItemRangeInserted(initialSize, newPricings.size());
    }

    public void clear() {
        if (getItemCount() == 0) return;
        int size;
        synchronized (mutex) {
            size = pricings.size();
            pricings.clear();
        }
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pricing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PricingResponseDTO pricing = pricings.get(position);
        holder.index.setText(String.valueOf(position+1));
        holder.name.setText(pricing.getItemName());
        holder.price.setText(String.valueOf(pricing.getPrice()));
        holder.discount.setText(String.valueOf(pricing.getDiscount()));
        holder.discountedPrice.setText(calculateDiscountedPrice(pricing.getPrice(), pricing.getDiscount()));

        holder.name.setTypeface(holder.name.getTypeface(), Typeface.BOLD);
        holder.discountedPrice.setTypeface(holder.discountedPrice.getTypeface(), Typeface.BOLD);

        if (selectedItemId != null && selectedItemId.equals(pricing.getItemId())) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            selectedItemId = pricing.getItemId();
            notifyDataSetChanged();
            onItemSelectedListener.onItemSelected(selectedItemId, pricing.getItemType());
        });

    }

    @Override
    public int getItemCount() {
        return pricings.size();
    }

    private String calculateDiscountedPrice(double price, double discount){
        return String.format("%.2f", price * (1 - discount/100));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView index, name, price, discount, discountedPrice;

        public ViewHolder(View itemView) {
            super(itemView);
            index = itemView.findViewById(R.id.item_index);
            name = itemView.findViewById(R.id.item_name);
            price = itemView.findViewById(R.id.item_price);
            discount = itemView.findViewById(R.id.item_discount);
            discountedPrice = itemView.findViewById(R.id.item_discounted_price);
        }
    }
}
