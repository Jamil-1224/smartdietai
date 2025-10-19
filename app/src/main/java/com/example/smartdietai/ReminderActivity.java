package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import java.util.ArrayList;
import java.util.Calendar;

public class ReminderActivity extends AppCompatActivity {

    DatabaseHelper db;
    Button btnAdd;
    ListView lvReminders;
    ArrayAdapter<String> adapter;
    ArrayList<String> items = new ArrayList<>();
    ArrayList<Integer> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        db = new DatabaseHelper(this);
        btnAdd = findViewById(R.id.btnAddReminder);
        lvReminders = findViewById(R.id.lvReminders);

        btnAdd.setOnClickListener(v -> showAddDialog());
        lvReminders.setOnItemClickListener((parent, view, position, id) -> {
            int reminderId = ids.get(position);
            // toggle enabled state
            Cursor c = db.getReminders();
            c.moveToPosition(position);
            int enabled = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.REM_ENABLED));
            if (enabled == 1) {
                db.setReminderEnabled(reminderId, 0);
                ReminderHelper.cancelReminder(this, reminderId);
                Toast.makeText(this, "Reminder disabled", Toast.LENGTH_SHORT).show();
            } else {
                db.setReminderEnabled(reminderId, 1);
                Cursor cur = db.getReminders();
                cur.moveToPosition(position);
                int hour = cur.getInt(cur.getColumnIndexOrThrow(DatabaseHelper.REM_HOUR));
                int minute = cur.getInt(cur.getColumnIndexOrThrow(DatabaseHelper.REM_MINUTE));
                String title = cur.getString(cur.getColumnIndexOrThrow(DatabaseHelper.REM_TITLE));
                ReminderHelper.scheduleReminder(this, reminderId, title, hour, minute);
                Toast.makeText(this, "Reminder enabled", Toast.LENGTH_SHORT).show();
            }
            refreshList();
        });

        lvReminders.setOnItemLongClickListener((parent, view, position, id) -> {
            int reminderId = ids.get(position);
            db.deleteReminder(reminderId);
            ReminderHelper.cancelReminder(this, reminderId);
            refreshList();
            return true;
        });

        refreshList();
    }

    private void showAddDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_reminder, null);
        final EditText etTitle = view.findViewById(R.id.etRemTitle);
        final Button btnTime = view.findViewById(R.id.btnPickTime);
        final TextView tvTime = view.findViewById(R.id.tvPickedTime);
        final int[] picked = {-1, -1};
        btnTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (tp, hour, minute) -> {
                tvTime.setText(String.format("%02d:%02d", hour, minute));
                picked[0] = hour; picked[1] = minute;
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        new android.app.AlertDialog.Builder(this)
                .setTitle("Add Reminder")
                .setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty() || picked[0]==-1) { Toast.makeText(this, "Enter title and time", Toast.LENGTH_SHORT).show(); return; }
                    long id = db.addReminder(title, picked[0], picked[1], 1);
                    if (id != -1) {
                        ReminderHelper.scheduleReminder(this, (int)id, title, picked[0], picked[1]);
                        refreshList();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshList() {
        items.clear(); ids.clear();
        Cursor c = db.getReminders();
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.REM_ID));
            String title = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.REM_TITLE));
            int hour = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.REM_HOUR));
            int min = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.REM_MINUTE));
            int enabled = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.REM_ENABLED));
            ids.add(id);
            items.add(String.format("%s - %02d:%02d [%s]", title, hour, min, enabled==1?"On":"Off"));
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lvReminders.setAdapter(adapter);
    }
}
