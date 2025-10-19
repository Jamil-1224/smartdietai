package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import android.app.AlertDialog;

public class FoodListActivity extends AppCompatActivity {

    DatabaseHelper db;
    ListView lvFoods;
    ArrayAdapter<String> adapter;
    java.util.ArrayList<String> items = new java.util.ArrayList<>();
    java.util.ArrayList<Integer> ids = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);
        db = new DatabaseHelper(this);
        lvFoods = findViewById(R.id.lvFoods);
        refreshList();

        lvFoods.setOnItemLongClickListener((parent, view, position, id) -> {
            int foodId = ids.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete food?")
                    .setMessage("Are you sure to delete this food?")
                    .setPositiveButton("Yes", (d, w) -> {
                        db.deleteFood(foodId);
                        refreshList();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }

    private void refreshList() {
        items.clear(); ids.clear();
        Cursor c = db.getAllFoods();
        while (c.moveToNext()) {
            ids.add(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_ID)));
            String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_NAME));
            double cal = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_CAL));
            double pro = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_PROTEIN));
            double carbs = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_CARBS));
            double fat = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_FAT));
            String serving = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_SERVING));
            items.add(name + " (" + serving + ") - " + Math.round(cal) + " kcal | P:" + pro + "C:" + carbs + "F:" + fat);
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lvFoods.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
}
