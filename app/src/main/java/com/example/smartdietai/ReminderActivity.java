package com.example.smartdietai;

import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import android.database.Cursor;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;
import android.provider.Settings;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import android.content.pm.PackageManager;
import android.app.TimePickerDialog;

public class ReminderActivity extends AppCompatActivity {
    private Button btnAddReminder;
    private ListView lvReminders;
    private DatabaseHelper db;
    private List<ReminderRow> rows = new ArrayList<>();

    private static class ReminderRow {
        long id;
        String title;
        int hour;
        int minute;
        String days;
        ReminderRow(long id, String title, int hour, int minute, String days) {
            this.id = id;
            this.title = title;
            this.hour = hour;
            this.minute = minute;
            this.days = days != null ? days : "Sun,Mon,Tue,Wed,Thu,Fri,Sat";
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        // Init UI and DB
        btnAddReminder = findViewById(R.id.btnAddReminder);
        lvReminders = findViewById(R.id.lvReminders);
        db = new DatabaseHelper(this);

        // Load existing reminders into the list
        loadReminders();

        // Long-press to delete a reminder
        lvReminders.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < 0 || position >= rows.size()) return true;
            ReminderRow r = rows.get(position);
            String label = r.title + " — " + String.format(Locale.getDefault(), "%02d:%02d", r.hour, r.minute);
            new AlertDialog.Builder(this)
                    .setTitle("Delete reminder?")
                    .setMessage("Remove " + label + "")
                    .setPositiveButton("Delete", (d, w) -> {
                        // Cancel alarm and delete from DB
                        int alarmId = (r.hour * 100) + r.minute;
                        ReminderHelper.cancelReminder(ReminderActivity.this, alarmId);
                        db.deleteReminder(r.id);
                        Toast.makeText(ReminderActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                        loadReminders();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        btnAddReminder.setOnClickListener(v -> {
            // Inflate dialog with title, time picker, and day selection
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_reminder, null);
            final EditText etTitle = dialogView.findViewById(R.id.etRemTitle);
            final Button btnPickTime = dialogView.findViewById(R.id.btnPickTime);
            final TextView tvPickedTime = dialogView.findViewById(R.id.tvPickedTime);

            // Day checkboxes
            final CheckBox cbSun = dialogView.findViewById(R.id.cbSunday);
            final CheckBox cbMon = dialogView.findViewById(R.id.cbMonday);
            final CheckBox cbTue = dialogView.findViewById(R.id.cbTuesday);
            final CheckBox cbWed = dialogView.findViewById(R.id.cbWednesday);
            final CheckBox cbThu = dialogView.findViewById(R.id.cbThursday);
            final CheckBox cbFri = dialogView.findViewById(R.id.cbFriday);
            final CheckBox cbSat = dialogView.findViewById(R.id.cbSaturday);

            final int[] selectedHour = {-1};
            final int[] selectedMinute = {-1};

            btnPickTime.setOnClickListener(v2 -> {
                TimePickerDialog tpd = new TimePickerDialog(
                        ReminderActivity.this,
                        (view, hourOfDay, minute) -> {
                            selectedHour[0] = hourOfDay;
                            selectedMinute[0] = minute;
                            tvPickedTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
                        },
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE),
                        true
                );
                tpd.show();
            });

            new AlertDialog.Builder(this)
                    .setTitle("Add Reminder")
                    .setView(dialogView)
                    .setPositiveButton("Save", (d, w) -> {
                        String title = etTitle.getText().toString().trim();
                        if (title.isEmpty()) title = "Diet Reminder";

                        if (selectedHour[0] < 0 || selectedMinute[0] < 0) {
                            Toast.makeText(ReminderActivity.this, "Please pick a time", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Collect selected days
                        List<String> daysList = new ArrayList<>();
                        if (cbSun.isChecked()) daysList.add("Sun");
                        if (cbMon.isChecked()) daysList.add("Mon");
                        if (cbTue.isChecked()) daysList.add("Tue");
                        if (cbWed.isChecked()) daysList.add("Wed");
                        if (cbThu.isChecked()) daysList.add("Thu");
                        if (cbFri.isChecked()) daysList.add("Fri");
                        if (cbSat.isChecked()) daysList.add("Sat");

                        if (daysList.isEmpty()) {
                            Toast.makeText(ReminderActivity.this, "Please select at least one day", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String days = String.join(",", daysList);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 2001);
                            }
                        }

                        if (!canScheduleExactAlarms()) {
                            promptExactAlarmPermission();
                            return;
                        }

                        setReminder(title, selectedHour[0], selectedMinute[0], days);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setReminder(String title, int hour, int minute, String days) {
        int id = (hour * 100) + minute; // stable id per time
        // Persist in DB with days
        db.addReminder(title, hour, minute, 1, days);
        // Schedule alarm
        ReminderHelper.scheduleReminder(this, id, title, hour, minute);
        Toast.makeText(this, "Reminder set for " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute) + " on " + days, Toast.LENGTH_LONG).show();
        // Refresh list
        loadReminders();
    }

    private boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            return am != null && am.canScheduleExactAlarms();
        }
        return true;
    }

    private void promptExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            new AlertDialog.Builder(this)
                    .setTitle("Allow exact alarms")
                    .setMessage("To trigger reminders precisely, please allow exact alarms in settings.")
                    .setPositiveButton("Open Settings", (d, w) -> {
                        Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(i);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private void loadReminders() {
        List<String> items = new ArrayList<>();
        rows.clear();
        Cursor c = db.getReminders();
        if (c != null) {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(DatabaseHelper.REM_ID));
                String title = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.REM_TITLE));
                int hour = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.REM_HOUR));
                int minute = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.REM_MINUTE));

                // Get days, default to all days if not present
                String days = "Sun,Mon,Tue,Wed,Thu,Fri,Sat";
                try {
                    int daysIndex = c.getColumnIndexOrThrow(DatabaseHelper.REM_DAYS);
                    String dbDays = c.getString(daysIndex);
                    if (dbDays != null && !dbDays.isEmpty()) {
                        days = dbDays;
                    }
                } catch (Exception e) {
                    // Column might not exist in older DB versions
                }

                rows.add(new ReminderRow(id, title, hour, minute, days));

                // Format display with days
                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                String displayDays = days.replace(",", ", ");
                items.add(title + " — " + timeStr + "\n📅 " + displayDays);
            }
            c.close();
        }
        lvReminders.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
    }
}
