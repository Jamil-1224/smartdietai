package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import java.text.SimpleDateFormat;
import java.util.*;

public class MealsListActivity extends AppCompatActivity {

    DatabaseHelper db;
    ListView lvMeals;
    TextView tvTotalCalories, tvMealCount, tvDate;
    ArrayList<MealItem> mealItems = new ArrayList<>();
    MealAdapter mealAdapter;
    Button btnAddMeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals_list);

        db = new DatabaseHelper(this);
        lvMeals = findViewById(R.id.lvMeals);
        btnAddMeal = findViewById(R.id.btnAddMeal);
        tvTotalCalories = findViewById(R.id.tvTotalCalories);
        tvMealCount = findViewById(R.id.tvMealCount);
        tvDate = findViewById(R.id.tvDate);

        // Set today's date
        String formattedDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(formattedDate);

        // Initialize adapter
        mealAdapter = new MealAdapter(this, mealItems);
        lvMeals.setAdapter(mealAdapter);

        refreshList();

        lvMeals.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < mealItems.size()) {
                MealItem meal = mealItems.get(position);
                new AlertDialog.Builder(this)
                        .setTitle("Delete Meal")
                        .setMessage("Are you sure you want to delete '" + meal.getFoodName() + "'?")
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deleteMeal(meal.getId());
                            Toast.makeText(this, "Meal deleted", Toast.LENGTH_SHORT).show();
                            refreshList();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            return true;
        });

        if (btnAddMeal != null) {
            btnAddMeal.setOnClickListener(v -> startActivity(new Intent(this, AddMealActivity.class)));
        }
    }

    private void refreshList() {
        mealItems.clear();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        int totalCalories = 0;
        int mealCount = 0;

        try {
            Cursor c = db.getMealsByDate(today);
            if (c != null) {
                while (c.moveToNext()) {
                    int id = c.getInt(c.getColumnIndexOrThrow("id"));
                    String name = c.getString(c.getColumnIndexOrThrow("name"));
                    double cal = c.getDouble(c.getColumnIndexOrThrow("calories"));
                    double qty = c.getDouble(c.getColumnIndexOrThrow("quantity"));
                    String mt = c.getString(c.getColumnIndexOrThrow("meal_type"));
                    String notes = "";

                    // Try to get notes if column exists
                    try {
                        notes = c.getString(c.getColumnIndexOrThrow("notes"));
                        if (notes == null) notes = "";
                    } catch (Exception e) {
                        notes = "";
                    }

                    int mealCalories = (int) Math.round(cal * qty);
                    totalCalories += mealCalories;
                    mealCount++;

                    mealItems.add(new MealItem(id, name, mt, qty, mealCalories, notes));
                }
                c.close();
            }

            // Update summary
            tvTotalCalories.setText(String.valueOf(totalCalories));
            tvMealCount.setText(String.valueOf(mealCount));

            mealAdapter.notifyDataSetChanged();

            // Show empty state if no meals
            if (mealItems.isEmpty()) {
                Toast.makeText(this, "No meals logged today. Add your first meal!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading meals: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
}
