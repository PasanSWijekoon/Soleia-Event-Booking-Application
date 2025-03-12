package com.strawhats.soleia.Activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.strawhats.soleia.Adapters.NotificationAdapter;
import com.strawhats.soleia.Domain.NotificationModel;
import com.strawhats.soleia.Models.NotificationDatabaseHelper;
import com.strawhats.soleia.R;
import com.strawhats.soleia.databinding.ActivityShowNotificationBinding;

import java.util.List;

public class ShowNotificationActivity extends AppCompatActivity {

    private ActivityShowNotificationBinding binding;

    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private NotificationDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShowNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the Toolbar
        setSupportActionBar(binding.toolbar);

        // Enable the back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        notificationsRecyclerView = binding.notificationsRecyclerView;
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new NotificationDatabaseHelper(this);

        // Load notifications from the database
        List<NotificationModel> notifications = dbHelper.getAllNotifications();

        // Set up the adapter
        notificationAdapter = new NotificationAdapter(notifications);
        notificationsRecyclerView.setAdapter(notificationAdapter);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}