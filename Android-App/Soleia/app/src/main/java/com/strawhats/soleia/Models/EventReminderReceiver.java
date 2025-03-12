package com.strawhats.soleia.Models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.strawhats.soleia.Activity.MainActivity;
import com.strawhats.soleia.R;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.strawhats.soleia.Activity.MainActivity;
import com.strawhats.soleia.Models.NotificationDatabaseHelper;
import com.strawhats.soleia.R;

public class EventReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "event_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String eventTitle = intent.getStringExtra("eventTitle");
        String notificationMessage = "Reminder: " + eventTitle + " is happening soon!";

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 🔹 Create Notification Channel (For Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Event Reminders", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // 🔹 Open App When Notification Clicked
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔹 Build the Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.applogo) // Change to your notification icon
                .setContentTitle("Upcoming Event!")
                .setContentText(notificationMessage)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // 🔔 Show the Notification
        manager.notify(eventTitle.hashCode(), builder.build());

        // 🔹 Save Notification in SQLite
        NotificationDatabaseHelper dbHelper = new NotificationDatabaseHelper(context);
        dbHelper.saveNotification("Upcoming Event!", notificationMessage);
    }
}