package com.example.smartdietai;

import android.Manifest;
import android.content.*;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.smartdietai.R;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null) return;

            String title = intent.getStringExtra("title");
            if (title == null || title.isEmpty()) title = "Diet Reminder";
            int id = intent.getIntExtra("id", 100);
            int hour = intent.getIntExtra("hour", -1);
            int minute = intent.getIntExtra("minute", -1);

            // Ensure channel exists even if app wasn't launched
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("dietReminder", "Diet Reminder", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Reminders for healthy diet and meals");
                NotificationManager manager = (NotificationManager) context.getSystemService(NotificationManager.class);
                if (manager != null) manager.createNotificationChannel(channel);
            }

            Intent tapIntent = new Intent(context, ReminderActivity.class);
            PendingIntent contentPI = PendingIntent.getActivity(context, id, tapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "dietReminder")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText("Time to eat healthy! Don’t skip your meal.")
                    .setAutoCancel(true)
                    .setContentIntent(contentPI)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            try {
                NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                manager.notify(id, builder.build());
            } catch (SecurityException se) {
                // Ignore if notification permission not granted yet
            }

            // Reschedule for next day at the same time
            if (hour >= 0 && minute >= 0) {
                ReminderHelper.scheduleReminder(context, id, title, hour, minute);
            }
        } catch (Exception e) {
            // defensively catch any unexpected errors so the app won't crash
        }
    }
}
