package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import java.util.Locale;

public class BMIActivity extends AppCompatActivity {

    EditText weightInput, heightInput, ageInput;
    RadioGroup genderGroup;
    Button calculateBtn;
    TextView resultText;
    DatabaseHelper db;
    FoodDatabaseHelper foodDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        db = new DatabaseHelper(this);
        foodDb = new FoodDatabaseHelper(this);
        weightInput = findViewById(R.id.weightInput);
        heightInput = findViewById(R.id.heightInput);
        ageInput = findViewById(R.id.ageInput);
        genderGroup = findViewById(R.id.genderGroup);
        calculateBtn = findViewById(R.id.calculateBtn);
        resultText = findViewById(R.id.resultText);

        loadProfileIfExists();

        calculateBtn.setOnClickListener(v -> {
            try {
                double weight = Double.parseDouble(weightInput.getText().toString());
                double height = Double.parseDouble(heightInput.getText().toString()) / 100.0;
                int age = Integer.parseInt(ageInput.getText().toString());
                int selectedId = genderGroup.getCheckedRadioButtonId();
                String gender = (selectedId == R.id.maleRadio) ? "male" : "female";

                double bmi = weight / (height * height);
                String category = (bmi < 18.5) ? "Underweight" :
                        (bmi < 24.9) ? "Normal" :
                                (bmi < 29.9) ? "Overweight" : "Obese";

                double calories = CalorieCalculator.dailyCalorieTarget(age, (height*100), weight, gender, "moderate", "maintain");

                // Save the calculated calorie target to both databases
                int calorieTarget = (int) Math.round(calories);
                db.updateCalorieTarget(calorieTarget);
                foodDb.updateCalorieTarget(calorieTarget);

                // Calculate weight recommendations
                double healthyMinWeight = 18.5 * (height * height);
                double healthyMaxWeight = 24.9 * (height * height);

                StringBuilder result = new StringBuilder();
                result.append(String.format(Locale.getDefault(), "BMI: %.2f (%s)\n", bmi, category));
                result.append(String.format(Locale.getDefault(), "Estimated target: %.0f kcal/day\n", calories));

                if (bmi < 18.5) {
                    // Underweight - need to gain
                    double weightToGain = healthyMinWeight - weight;
                    result.append(String.format(Locale.getDefault(), "\n⬆️ Gain %.1f kg to reach healthy weight", weightToGain));
                } else if (bmi >= 25) {
                    // Overweight or Obese - need to lose
                    double weightToLose = weight - healthyMaxWeight;
                    result.append(String.format(Locale.getDefault(), "\n⬇️ Lose %.1f kg to reach healthy weight", weightToLose));
                } else {
                    // Normal weight
                    result.append("\n✅ You are in the healthy weight range!");
                }

                resultText.setText(result.toString());
            } catch (Exception ex) {
                Toast.makeText(this, "Please fill valid numbers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileIfExists() {
        Cursor c = db.fetchProfile();
        if (c != null && c.moveToFirst()) {
            ageInput.setText(String.valueOf(c.getInt(c.getColumnIndexOrThrow("age"))));
            heightInput.setText(String.valueOf(c.getDouble(c.getColumnIndexOrThrow("height"))));
            weightInput.setText(String.valueOf(c.getDouble(c.getColumnIndexOrThrow("weight"))));
            String gender = c.getString(c.getColumnIndexOrThrow("gender"));
            if ("male".equalsIgnoreCase(gender)) genderGroup.check(R.id.maleRadio);
            else genderGroup.check(R.id.femaleRadio);
        }
    }
}
