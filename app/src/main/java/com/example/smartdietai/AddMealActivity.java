package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddMealActivity extends AppCompatActivity {

    SwitchCompat switchManualInput;
    LinearLayout layoutSelectFood, layoutManualInput;
    Spinner spFoods, spMealType;
    EditText etQty, etNotes;
    EditText etFoodName, etCalories, etProtein, etCarbs, etFat;
    Button btnAdd;
    DatabaseHelper db;
    ArrayList<Integer> foodIds = new ArrayList<>();
    ArrayList<String> foodNames = new ArrayList<>();
    boolean isManualMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        db = new DatabaseHelper(this);

        // Clean invalid foods from database
        db.cleanupInvalidFoods();

        // Initialize views
        switchManualInput = findViewById(R.id.switchManualInput);
        layoutSelectFood = findViewById(R.id.layoutSelectFood);
        layoutManualInput = findViewById(R.id.layoutManualInput);
        spFoods = findViewById(R.id.spFoods);
        spMealType = findViewById(R.id.spMealType);
        etQty = findViewById(R.id.etQty);
        etNotes = findViewById(R.id.etNotes);
        etFoodName = findViewById(R.id.etFoodName);
        etCalories = findViewById(R.id.etCalories);
        etProtein = findViewById(R.id.etProtein);
        etCarbs = findViewById(R.id.etCarbs);
        etFat = findViewById(R.id.etFat);
        btnAdd = findViewById(R.id.btnAddMeal);

        loadFoods();
        setupMealTypes();

        etQty.setText("1");

        // Toggle between select and manual input
        switchManualInput.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isManualMode = isChecked;
            if (isChecked) {
                layoutSelectFood.setVisibility(View.GONE);
                layoutManualInput.setVisibility(View.VISIBLE);
            } else {
                layoutSelectFood.setVisibility(View.VISIBLE);
                layoutManualInput.setVisibility(View.GONE);
            }
        });

        btnAdd.setOnClickListener(v -> addMeal());
    }


    private void loadFoods() {
        foodIds.clear();
        foodNames.clear();

        Cursor c = db.getAllFoods();
        if (c != null) {
            while (c.moveToNext()) {
                foodIds.add(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_ID)));
                foodNames.add(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_NAME)));
            }
            c.close();
        }

        if (foodIds.isEmpty()) {
            foodNames.add("No foods available - Use manual input");
            switchManualInput.setChecked(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, foodNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFoods.setAdapter(adapter);
    }

    private void setupMealTypes() {
        String[] mealTypes = {"Breakfast", "Lunch", "Dinner", "Snack"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mealTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMealType.setAdapter(adapter);
    }

    private void addMeal() {
        try {
            // Validate quantity
            String qtyText = etQty.getText().toString().trim();
            if (qtyText.isEmpty()) {
                Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            double qty = Double.parseDouble(qtyText);
            if (qty <= 0) {
                Toast.makeText(this, "Quantity must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            String notes = etNotes.getText().toString().trim();
            String mealType = spMealType.getSelectedItem().toString().toLowerCase();
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            long foodId;

            if (isManualMode) {
                // Manual input mode - create new food first
                String foodName = etFoodName.getText().toString().trim();
                String calText = etCalories.getText().toString().trim();
                String proteinText = etProtein.getText().toString().trim();
                String carbsText = etCarbs.getText().toString().trim();
                String fatText = etFat.getText().toString().trim();

                if (foodName.isEmpty()) {
                    Toast.makeText(this, "Please enter food name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (calText.isEmpty()) {
                    Toast.makeText(this, "Please enter calories", Toast.LENGTH_SHORT).show();
                    return;
                }

                double calories = Double.parseDouble(calText);
                double protein = proteinText.isEmpty() ? 0 : Double.parseDouble(proteinText);
                double carbs = carbsText.isEmpty() ? 0 : Double.parseDouble(carbsText);
                double fat = fatText.isEmpty() ? 0 : Double.parseDouble(fatText);

                // Add food to database
                foodId = db.addFood(foodName, calories, protein, carbs, fat, "1 serving");

                if (foodId == -1) {
                    Toast.makeText(this, "Food already exists or failed to add", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Select from list mode
                int pos = spFoods.getSelectedItemPosition();
                if (pos < 0 || foodIds.isEmpty()) {
                    Toast.makeText(this, "Please select a food or use manual input", Toast.LENGTH_SHORT).show();
                    return;
                }
                foodId = foodIds.get(pos);
            }

            // Add meal
            long result = db.addMeal(foodId, qty, date, mealType, notes);

            if (result != -1) {
                Toast.makeText(this, "Meal added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add meal", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }
}
