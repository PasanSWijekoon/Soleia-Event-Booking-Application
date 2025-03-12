package com.strawhats.soleia.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.strawhats.soleia.Activity.DetailActivity;
import com.strawhats.soleia.Activity.SingleTicketActivity;
import com.strawhats.soleia.Domain.TicketModel;
import com.strawhats.soleia.databinding.ViewholderTicketItemsBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserTicketAdapter extends RecyclerView.Adapter<UserTicketAdapter.Viewholder> {

    private List<TicketModel> ticketList;
    private Context context;

    public UserTicketAdapter(List<TicketModel> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public UserTicketAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderTicketItemsBinding binding = ViewholderTicketItemsBinding.inflate(
                LayoutInflater.from(context), parent, false
        );
        return new Viewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserTicketAdapter.Viewholder holder, int position) {
        TicketModel ticket = ticketList.get(position);

        // Load event image using Glide
        Glide.with(context)
                .load(ticket.getImageUrl()) // Fetch event image URL from TicketModel
                .centerCrop()
                .into(holder.binding.eventImage);

        // Set event name
        holder.binding.eventNameText.setText(ticket.getTitle()); // Fetch event title from TicketModel

        // Set event date & time
        holder.binding.eventDateTime.setText(formatDate(ticket.getEventDate()));

        // Set event location (Venue Name)
        holder.binding.eventLocation.setText(ticket.getVenueName());


        // Handle "View Ticket" button click
        holder.binding.viewTicketButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SingleTicketActivity.class);
            intent.putExtra("ticket", ticket);
            context.startActivity(intent);
        });

        // Handle "Get Directions" button click
        holder.binding.getDirectionButton.setOnClickListener(v -> {
            if (ticket.getVenue() != null) {
                openGoogleMaps(ticket.getVenue().getLatitude(), ticket.getVenue().getLongitude());
            }
        });
    }



    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    private void openGoogleMaps(double latitude, double longitude) {
        String mapUri = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude + "(Event Location)";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUri));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        private final ViewholderTicketItemsBinding binding;

        public Viewholder(ViewholderTicketItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "TBA";
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());
        return dateFormat.format(date);
    }

    // Update adapter with new data
    public void updateTickets(List<TicketModel> newTickets) {
        this.ticketList.clear();
        this.ticketList.addAll(newTickets);
        notifyDataSetChanged();
    }
}
