package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import java.text.SimpleDateFormat;
import java.util.*;

public class MealsListActivity extends AppCompatActivity {

    DatabaseHelper db;
    ListView lvMeals;
    ArrayList<String> items = new ArrayList<>();
    ArrayList<Integer> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals_list);

        db = new DatabaseHelper(this);
        lvMeals = findViewById(R.id.lvMeals);
        refreshList();

        lvMeals.setOnItemLongClickListener((parent, view, position, id) -> {
            int mealId = ids.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete meal?")
                    .setPositiveButton("Yes", (d, w) -> {
                        db.deleteMeal(mealId);
                        refreshList();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }

    private void refreshList() {
        items.clear(); ids.clear();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor c = db.getMealsByDate(today);
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndexOrThrow("id"));
            String name = c.getString(c.getColumnIndexOrThrow("name"));
            double cal = c.getDouble(c.getColumnIndexOrThrow("calories"));
            double qty = c.getDouble(c.getColumnIndexOrThrow("quantity"));
            String mt = c.getString(c.getColumnIndexOrThrow("meal_type"));
            items.add(mt + ": " + name + " x" + qty + " (" + Math.round(cal * qty) + " kcal)");
            ids.add(id);
        }
        lvMeals.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
}
