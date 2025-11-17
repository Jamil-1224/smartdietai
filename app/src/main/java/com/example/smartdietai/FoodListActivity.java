package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import android.app.AlertDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodListActivity extends AppCompatActivity {

    DatabaseHelper db;
    ListView lvFoods;
    FoodAdapter adapter;
    List<Food> foodList = new ArrayList<>();
    List<Food> filteredList = new ArrayList<>();
    EditText searchFood;
    TextView tvFoodCount;
    LinearLayout emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        db = new DatabaseHelper(this);
        lvFoods = findViewById(R.id.lvFoods);
        searchFood = findViewById(R.id.searchFood);
        tvFoodCount = findViewById(R.id.tvFoodCount);
        emptyState = findViewById(R.id.emptyState);

        // Initialize adapter
        adapter = new FoodAdapter(this, filteredList);
        lvFoods.setAdapter(adapter);

        refreshList();

        // Search functionality
        searchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFoods(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Long press to delete
        lvFoods.setOnItemLongClickListener((parent, view, position, id) -> {
            Food food = filteredList.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Food")
                    .setMessage("Are you sure you want to delete \"" + food.getName() + "\"?")
                    .setPositiveButton("Delete", (d, w) -> {
                        db.deleteFood(food.getId());
                        refreshList();
                        Toast.makeText(this, "Food deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    private void refreshList() {
        foodList.clear();
        Cursor c = db.getAllFoods();
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_ID));
            String name = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_NAME));
            double cal = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_CAL));
            double pro = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_PROTEIN));
            double carbs = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_CARBS));
            double fat = c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_FAT));
            String serving = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_SERVING));

            foodList.add(new Food(id, name, cal, pro, carbs, fat, serving));
        }
        c.close();

        // Update food count
        updateFoodCount(foodList.size());

        // Apply current search filter
        filterFoods(searchFood.getText().toString());
    }

    private void filterFoods(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(foodList);
        } else {
            String lowerQuery = query.toLowerCase(Locale.getDefault());
            for (Food food : foodList) {
                if (food.getName().toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                    filteredList.add(food);
                }
            }
        }

        adapter.notifyDataSetChanged();

        // Show/hide empty state
        if (filteredList.isEmpty()) {
            lvFoods.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            lvFoods.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void updateFoodCount(int count) {
        tvFoodCount.setText(String.format(Locale.getDefault(), "%d food%s available", count, count == 1 ? "" : "s"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
}
