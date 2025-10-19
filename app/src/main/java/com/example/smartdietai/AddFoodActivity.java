package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

public class AddFoodActivity extends AppCompatActivity {

    EditText nameInput, calorieInput, proteinInput, carbInput, fatInput, servingInput;
    Button addButton;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        db = new DatabaseHelper(this);
        nameInput = findViewById(R.id.nameInput);
        calorieInput = findViewById(R.id.calorieInput);
        proteinInput = findViewById(R.id.proteinInput);
        carbInput = findViewById(R.id.carbInput);
        fatInput = findViewById(R.id.fatInput);
        servingInput = findViewById(R.id.servingInput);
        addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(v -> {
            try {
                String name = nameInput.getText().toString().trim();
                double calories = Double.parseDouble(calorieInput.getText().toString().trim());
                double protein = Double.parseDouble(proteinInput.getText().toString().trim());
                double carbs = Double.parseDouble(carbInput.getText().toString().trim());
                double fat = Double.parseDouble(fatInput.getText().toString().trim());
                String serving = servingInput.getText().toString().trim();

                long id = db.addFood(name, calories, protein, carbs, fat, serving);
                if (id == -1) Toast.makeText(this, "Food already exists or error", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(this, "Food added", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (Exception ex) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
