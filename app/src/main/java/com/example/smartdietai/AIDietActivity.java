package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import java.util.*;

public class AIDietActivity extends AppCompatActivity {

    TextView suggestionText;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_diet);
        suggestionText = findViewById(R.id.suggestionText);
        db = new DatabaseHelper(this);
        generatePlan();
    }

    private void generatePlan() {
        Cursor c = db.fetchProfile();
        StringBuilder sb = new StringBuilder();
        if (c != null && c.moveToFirst()) {
            int age = c.getInt(c.getColumnIndexOrThrow("age"));
            double height = c.getDouble(c.getColumnIndexOrThrow("height"));
            double weight = c.getDouble(c.getColumnIndexOrThrow("weight"));
            String gender = c.getString(c.getColumnIndexOrThrow("gender"));
            String activity = c.getString(c.getColumnIndexOrThrow("activity_level"));
            String goal = c.getString(c.getColumnIndexOrThrow("goal"));

            double target = CalorieCalculator.dailyCalorieTarget(age, height, weight, gender, activity, goal);
            sb.append("Daily target: ").append(Math.round(target)).append(" kcal\n\n");
            // simple plan
            sb.append("Sample Plan (approx):\n");
            sb.append("Breakfast: Oats + Banana (30%)\n");
            sb.append("Lunch: Rice + Chicken + Veggies (40%)\n");
            sb.append("Dinner: Light protein + salad (25%)\n");
            sb.append("Snacks: Nuts / Yogurt (5%)\n\n");
            sb.append("Tips:\n - Drink water regularly\n - Prefer whole grains\n - Increase protein for muscle\n");
        } else {
            sb.append("Set your profile first in BMI screen.");
        }
        suggestionText.setText(sb.toString());
    }
}
