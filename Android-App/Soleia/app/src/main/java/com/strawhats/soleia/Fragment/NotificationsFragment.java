package com.strawhats.soleia.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.strawhats.soleia.Adapters.NotificationAdapter;
import com.strawhats.soleia.Domain.NotificationModel;
import com.strawhats.soleia.Models.NotificationDatabaseHelper;
import com.strawhats.soleia.R;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private NotificationDatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dbHelper = new NotificationDatabaseHelper(getContext());

        // Load notifications from the database
        List<NotificationModel> notifications = dbHelper.getAllNotifications();

        // Set up the adapter
        notificationAdapter = new NotificationAdapter(notifications);
        notificationsRecyclerView.setAdapter(notificationAdapter);

        // Set up the Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null) {
            // Set the Toolbar as the support action bar for the activity
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            // Enable the back button
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Notifications");
            }
        }



        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();  // Handle the back button press
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}