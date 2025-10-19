package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnBMI, btnAddFood, btnViewFoods, btnAIPlan, btnAddMeal, btnViewMeals, btnReminders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnBMI = findViewById(R.id.btnBMI);
        btnAddFood = findViewById(R.id.btnAddFood);
        btnViewFoods = findViewById(R.id.btnViewFoods);
        btnAIPlan = findViewById(R.id.btnAIPlan);
        btnAddMeal = findViewById(R.id.btnAddMeal);
        btnViewMeals = findViewById(R.id.btnViewMeals);
        btnReminders = findViewById(R.id.btnReminders);

        btnBMI.setOnClickListener(v -> startActivity(new Intent(this, BMIActivity.class)));
        btnAddFood.setOnClickListener(v -> startActivity(new Intent(this, AddFoodActivity.class)));
        btnViewFoods.setOnClickListener(v -> startActivity(new Intent(this, FoodListActivity.class)));
        btnAIPlan.setOnClickListener(v -> startActivity(new Intent(this, AIDietActivity.class)));
        btnAddMeal.setOnClickListener(v -> startActivity(new Intent(this, AddMealActivity.class)));
        btnViewMeals.setOnClickListener(v -> startActivity(new Intent(this, MealsListActivity.class)));
        btnReminders.setOnClickListener(v -> startActivity(new Intent(this, ReminderActivity.class)));
    }
}
