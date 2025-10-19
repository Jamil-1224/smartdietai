package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;

public class BMIActivity extends AppCompatActivity {

    EditText weightInput, heightInput, ageInput;
    RadioGroup genderGroup;
    Button calculateBtn, saveProfileBtn;
    TextView resultText;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        db = new DatabaseHelper(this);
        weightInput = findViewById(R.id.weightInput);
        heightInput = findViewById(R.id.heightInput);
        ageInput = findViewById(R.id.ageInput);
        genderGroup = findViewById(R.id.genderGroup);
        calculateBtn = findViewById(R.id.calculateBtn);
        saveProfileBtn = findViewById(R.id.saveProfileBtn);
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

                resultText.setText(String.format("BMI: %.2f (%s)\nEstimated target: %.0f kcal/day", bmi, category, calories));
            } catch (Exception ex) {
                Toast.makeText(this, "Please fill valid numbers", Toast.LENGTH_SHORT).show();
            }
        });

        saveProfileBtn.setOnClickListener(v -> {
            try {
                String name = "You";
                int age = Integer.parseInt(ageInput.getText().toString());
                double height = Double.parseDouble(heightInput.getText().toString());
                double weight = Double.parseDouble(weightInput.getText().toString());
                int selectedId = genderGroup.getCheckedRadioButtonId();
                String gender = (selectedId == R.id.maleRadio) ? "male" : "female";
                String activity = "moderate";
                String goal = "maintain";
                db.saveProfile(name, age, height, weight, gender, activity, goal);
                Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(this, "Please enter valid values to save profile", Toast.LENGTH_SHORT).show();
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
