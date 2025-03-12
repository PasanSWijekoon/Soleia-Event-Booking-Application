package com.strawhats.soleia.Adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.strawhats.soleia.Activity.DetailActivity;
import com.strawhats.soleia.Domain.EventModel;
import com.strawhats.soleia.databinding.ViewholderFeaturedEventsBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeaturedEventsAdapter extends RecyclerView.Adapter<FeaturedEventsAdapter.Viewholder> {

    private List<EventModel> items;
    private Context context;

    public FeaturedEventsAdapter(List<EventModel> items) {
        this.items = new ArrayList<>(items);
        filterAndSortEvents();
    }

    // This method filters and sorts the events by event date
    private void filterAndSortEvents() {
        Date currentDate = new Date(); // Current system date
        List<EventModel> upcomingEvents = new ArrayList<>();

        for (EventModel event : items) {
            if (event.getEventDate() != null && event.getEventDate().after(currentDate)) {
                upcomingEvents.add(event); // Add to upcoming events if the event date is in the future
            }
        }

        // Sort events by event date (ascending order)
        Collections.sort(upcomingEvents, (event1, event2) -> event1.getEventDate().compareTo(event2.getEventDate()));

        items = upcomingEvents; // Update the list with filtered and sorted events
    }

    @NonNull
    @Override
    public FeaturedEventsAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderFeaturedEventsBinding binding = ViewholderFeaturedEventsBinding.inflate(
                LayoutInflater.from(context), parent, false
        );
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedEventsAdapter.Viewholder holder, int position) {
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

        // Set venue name
        holder.binding.textVenue.setText(eventModel.getVenueName());

        // Set ticket price
        String formattedPrice = String.format(Locale.getDefault(), "Rs %.2f", eventModel.getTicketPrice());
        holder.binding.textPrice.setText(formattedPrice);

        // Set available tickets
        String ticketsText = String.format(Locale.getDefault(),
                "%d tickets left", eventModel.getAvailableTickets());
        holder.binding.textTickets.setText(ticketsText);

        // In onBindViewHolder:
        if (eventModel.getAvailableTickets() < 10) {
            holder.binding.textTickets.setTextColor(
                    ContextCompat.getColor(context, android.R.color.holo_red_dark)
            );
        } else {
            holder.binding.textTickets.setTextColor(
                    ContextCompat.getColor(context, android.R.color.darker_gray)
            );
        }

        // Set click listener
        holder.binding.getRoot().setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            Log.d("FeaturedEventsAdapter", "Passing event: " + eventModel);

            intent.putExtra("event", eventModel);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        private final ViewholderFeaturedEventsBinding binding;

        public Viewholder(ViewholderFeaturedEventsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String formatDate(Date eventDate) {
        if (eventDate == null) return "TBA";

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return dateFormat.format(eventDate);
    }

    // Call this method to update the events in the adapter when data changes
    public void updateEvents(List<EventModel> newEventList) {
        this.items.clear();
        this.items.addAll(newEventList);
        filterAndSortEvents(); // Re-filter and re-sort the events
        notifyDataSetChanged(); // Notify adapter about the changes
    }
}
