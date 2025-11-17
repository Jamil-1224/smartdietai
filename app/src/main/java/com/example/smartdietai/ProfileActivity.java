package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName, etAge, etHeight, etWeight;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private Button btnSave;
    FoodDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new FoodDatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        btnSave = findViewById(R.id.btnSave);

        loadProfile();

        btnSave.setOnClickListener(v -> {
            String nameStr = etName.getText().toString().trim();
            String ageStr = etAge.getText().toString().trim();
            String genderStr = rbMale.isChecked() ? "Male" : "Female";
            String heightStr = etHeight.getText().toString().trim();
            String weightStr = etWeight.getText().toString().trim();
            
            // Default values for removed fields
            String goalStr = "Maintain";
            String waterStr = "2000";

            if (nameStr.isEmpty() || ageStr.isEmpty() || genderStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int age = Integer.parseInt(ageStr);
                double height = Double.parseDouble(heightStr);
                double weight = Double.parseDouble(weightStr);
                int water = Integer.parseInt(waterStr);

                dbHelper.saveOrUpdateProfile(nameStr, age, genderStr, height, weight, goalStr, water);
                Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
            } catch (NumberFormatException ex) {
                Toast.makeText(this, "Please enter valid numbers for age, height, weight, and water", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile() {
        Cursor cursor = dbHelper.getProfile();
        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            etAge.setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("age"))));
            String gender = cursor.getString(cursor.getColumnIndexOrThrow("gender"));
            if ("Male".equals(gender)) {
                rbMale.setChecked(true);
            } else {
                rbFemale.setChecked(true);
            }
            etHeight.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("height"))));
            etWeight.setText(String.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow("weight"))));
            // Goal and water fields removed from UI
        }
        if (cursor != null) cursor.close();
    }
}
