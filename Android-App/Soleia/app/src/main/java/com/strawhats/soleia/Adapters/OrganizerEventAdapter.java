package com.strawhats.soleia.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.strawhats.soleia.Activity.DetailActivity;
import com.strawhats.soleia.Activity.EditEventsActivity;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.R;
import com.strawhats.soleia.databinding.ViewholderOrganizerEventsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.Viewholder> {

    private List<EventModel> items;
    private Context context;

    private OnItemClickListener onItemClickListener; // Interface for click listener

    public OrganizerEventAdapter(List<EventModel> items) {
        this.items = new ArrayList<>(items);
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public OrganizerEventAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderOrganizerEventsBinding binding = ViewholderOrganizerEventsBinding.inflate(
                LayoutInflater.from(context), parent, false
        );
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerEventAdapter.Viewholder holder, int position) {

        EventModel eventModel = items.get(position);

        // Load event image
        if (eventModel.getImageUrl() != null && !eventModel.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(eventModel.getImageUrl())
                    .centerCrop()
                    .into(holder.binding.imageEvent);
        }

        // Set event category
        holder.binding.textCategory.setText(eventModel.getCategory());

        // Set event title
        holder.binding.textTitle.setText(eventModel.getTitle());

        // Set event date
        holder.binding.textDate.setText(formatDate(eventModel.getEventDate()));

        // Set available tickets sold
        String ticketsText = String.format(Locale.getDefault(),
                "%d Tickets Sold", eventModel.getAttendeesCount());
        holder.binding.textTickets.setText(ticketsText);

        // Calculate money collected (Price * Tickets Sold)
        double moneyCollected = eventModel.getTicketPrice() * eventModel.getAttendeesCount();
        String moneyText = String.format(Locale.getDefault(), "Rs: %.2f", moneyCollected);
        holder.binding.textMoneyCollected.setText(moneyText);

        // Set status background
        if ("approved".equalsIgnoreCase(eventModel.getStatus())) {
            holder.binding.textStatus.setText("Approved");
            holder.binding.textStatus.setBackgroundResource(R.drawable.approve_background);  // Approved background
        } else {
            holder.binding.textStatus.setText("Not Approved");
            holder.binding.textStatus.setBackgroundResource(R.drawable.noapprovebackground);  // Not Approved background
        }

        holder.binding.getRoot().setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private final ViewholderOrganizerEventsBinding binding;

        public Viewholder(ViewholderOrganizerEventsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String formatDate(Date eventDate) {
        if (eventDate == null) return "TBA";

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(eventDate);
    }

    // Method to update events in the adapter when data changes
    public void updateEvents(List<EventModel> newEventList) {
        this.items.clear();
        this.items.addAll(newEventList);
        notifyDataSetChanged(); // Notify RecyclerView that the data has changed
    }


}
