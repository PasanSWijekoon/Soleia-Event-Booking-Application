package com.strawhats.soleia.Adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.strawhats.soleia.Activity.MainActivity;
import com.strawhats.soleia.Domain.CategoryModel;
import com.strawhats.soleia.Fragment.ExploreFragment;
import com.strawhats.soleia.R;
import com.strawhats.soleia.databinding.ViewholderCatergoryBinding;
import java.util.List;
import java.util.Random;

public class CatergoryAdapter extends RecyclerView.Adapter<CatergoryAdapter.Viewholder> {
    private List<CategoryModel> items;
    private final GradientPair[] gradientPairs = {
            new GradientPair("#4ECDC4", "#FF6B6B"),
            new GradientPair("#A8E6CF", "#FF8B94"),
            new GradientPair("#6C63FF", "#FF6584"),
            new GradientPair("#00B4DB", "#0083B0"),
            new GradientPair("#FF9A9E", "#FAD0C4")
    };

    private static class GradientPair {
        final int startColor;
        final int endColor;

        GradientPair(String startHex, String endHex) {
            this.startColor = Color.parseColor(startHex);
            this.endColor = Color.parseColor(endHex);
        }
    }

    public CatergoryAdapter(List<CategoryModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderCatergoryBinding binding = ViewholderCatergoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        CategoryModel item = items.get(position);
        holder.binding.titleCat.setText(item.getName());

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(item.getPicture())
                .into(holder.binding.picCat);

        // Apply random gradient background
        GradientPair randomPair = gradientPairs[new Random().nextInt(gradientPairs.length)];
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{randomPair.startColor, randomPair.endColor}
        );

        // Set corner radius (16dp converted to pixels)
        float cornerRadius = holder.itemView.getContext().getResources()
                .getDisplayMetrics().density * 16;
        gradient.setCornerRadius(cornerRadius);

        // Apply gradient to the LinearLayout inside CardView
        holder.binding.contentLayout.setBackground(gradient);

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            // Pass the clicked category to ExploreFragment
            if (v.getContext() instanceof MainActivity) {
                MainActivity activity = (MainActivity) v.getContext();
                ExploreFragment exploreFragment = new ExploreFragment();

                // Create a Bundle to send the CategoryModel data
                Bundle bundle = new Bundle();
                bundle.putSerializable("category", item); // Send CategoryModel
                exploreFragment.setArguments(bundle);

                // Switch to ExploreFragment
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.mainframe_layout, exploreFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        ViewholderCatergoryBinding binding;

        public Viewholder(ViewholderCatergoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}