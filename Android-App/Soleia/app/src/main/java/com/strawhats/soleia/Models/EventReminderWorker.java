package com.strawhats.soleia.Models;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.strawhats.soleia.R;

public class EventReminderWorker extends Worker {
    private static final String CHANNEL_ID = "event_reminders";

    public EventReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String eventTitle = getInputData().getString("eventTitle");

        sendNotification(eventTitle);
        return Result.success();
    }

    private void sendNotification(String eventTitle) {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Event Reminders", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("Upcoming Event Reminder")
                .setContentText("Your event " + eventTitle + " is happening soon!")
                .setAutoCancel(true);

        manager.notify(2, builder.build());
    }
}