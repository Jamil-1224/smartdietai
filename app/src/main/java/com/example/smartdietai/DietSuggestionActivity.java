package com.example.smartdietai;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import okhttp3.*;
import org.json.*;
import android.database.Cursor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DietSuggestionActivity extends AppCompatActivity implements SavedMealAdapter.OnMealDeletedListener {
    EditText inputCalories, inputExclude;
    Spinner spinnerDiet;
    Button btnSuggest;
    TextView txtResult;
    TextView headerName, headerMeta;
    ProgressBar progressBar;
    LinearLayout mealCardsContainer;
    TextView breakfastText, lunchText, dinnerText;
    RecyclerView savedMealsRecyclerView;
    TextView noSavedMealsText;
    Button btnSaveBreakfast, btnSaveLunch, btnSaveDinner;

    OkHttpClient client = new OkHttpClient();
    SpoonacularApiClient spoonacularClient;
    DatabaseHelper db;
    FoodDatabaseHelper altDb;
    SavedMealDatabaseHelper savedMealDb;
    SavedMealAdapter savedMealAdapter;
    List<SavedMeal> savedMeals = new ArrayList<>();
    
    // Store current meal data for saving
    private JSONObject currentBreakfast;
    private JSONObject currentLunch;
    private JSONObject currentDinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet_suggestion);

        // Initialize UI components
        inputCalories = findViewById(R.id.inputCalories);
        spinnerDiet = findViewById(R.id.spinnerDiet);
        inputExclude = findViewById(R.id.inputExclude);
        btnSuggest = findViewById(R.id.btnSuggest);
        txtResult = findViewById(R.id.txtResult);
        headerName = findViewById(R.id.headerName);
        headerMeta = findViewById(R.id.headerMeta);
        progressBar = findViewById(R.id.progressBar);
        mealCardsContainer = findViewById(R.id.mealCardsContainer);
        breakfastText = findViewById(R.id.breakfastText);
        lunchText = findViewById(R.id.lunchText);
        dinnerText = findViewById(R.id.dinnerText);
        savedMealsRecyclerView = findViewById(R.id.savedMealsRecyclerView);
        noSavedMealsText = findViewById(R.id.noSavedMealsText);

        // Initialize database helpers
        db = new DatabaseHelper(this);
        altDb = new FoodDatabaseHelper(this);
        savedMealDb = new SavedMealDatabaseHelper(this);
        spoonacularClient = new SpoonacularApiClient(client);

        // Setup diet spinner
        setupDietSpinner();
        
        // Load profile header
        loadProfileHeader();
        
        // Load calorie target from database
        loadCalorieTarget();

        // Setup saved meals recycler view
        setupSavedMeals();
        
        // Load saved meals
        loadSavedMeals();

        btnSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String calories = inputCalories.getText().toString().trim();
                String diet = spinnerDiet.getSelectedItem().toString();
                String exclude = inputExclude.getText().toString().trim();
                
                if (calories.isEmpty()) {
                    Toast.makeText(DietSuggestionActivity.this, "Please enter calories", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Show progress and hide previous results
                progressBar.setVisibility(View.VISIBLE);
                txtResult.setVisibility(View.GONE);
                mealCardsContainer.setVisibility(View.GONE);
                
                // Make API call
                getMealPlan(calories, diet, exclude);
            }
        });
    }
    
    private void loadProfileHeader() {
        Cursor cursor = altDb.getProfile();
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int nameIndex = cursor.getColumnIndexOrThrow("name");
                int ageIndex = cursor.getColumnIndexOrThrow("age");
                int genderIndex = cursor.getColumnIndexOrThrow("gender");
                int heightIndex = cursor.getColumnIndexOrThrow("height");
                int weightIndex = cursor.getColumnIndexOrThrow("weight");

                String name = cursor.getString(nameIndex);
                int age = cursor.getInt(ageIndex);
                String gender = cursor.getString(genderIndex);
                double height = cursor.getDouble(heightIndex);
                double weight = cursor.getDouble(weightIndex);

                headerName.setText(name);
                headerName.setTextSize(18);
                headerName.setTextColor(getResources().getColor(android.R.color.black));

                headerMeta.setText(String.format(
                        Locale.getDefault(),
                        "Age: %d | Gender: %s | Height: %.1f cm | Weight: %.1f kg",
                        age, gender, height, weight
                ));
                headerMeta.setTextSize(14);

            } catch (Exception e) {
                headerName.setText("Profile Not Complete");
                headerMeta.setText("Please update your profile information");
            }
        } else {
            headerName.setText("Profile Not Complete");
            headerMeta.setText("Please update your profile information");
        }
        if (cursor != null) cursor.close();
    }

    private void loadCalorieTarget() {
        // Try DatabaseHelper first
        Cursor cursor = db.fetchProfile();
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int calorieIndex = cursor.getColumnIndex("calorie_target");
                if (calorieIndex != -1) {
                    int calorieTarget = cursor.getInt(calorieIndex);
                    if (calorieTarget > 0) {
                        inputCalories.setText(String.valueOf(calorieTarget));
                        if (cursor != null) cursor.close();
                        return;
                    }
                }
            } catch (Exception e) {
                // Continue to try FoodDatabaseHelper
            }
        }
        if (cursor != null) cursor.close();

        // Try FoodDatabaseHelper as fallback
        Cursor cursor2 = altDb.getProfile();
        if (cursor2 != null && cursor2.moveToFirst()) {
            try {
                int calorieIndex = cursor2.getColumnIndex("calorie_target");
                if (calorieIndex != -1) {
                    int calorieTarget = cursor2.getInt(calorieIndex);
                    if (calorieTarget > 0) {
                        inputCalories.setText(String.valueOf(calorieTarget));
                    }
                }
            } catch (Exception e) {
                // If column doesn't exist or error occurs, just leave field empty
            }
        }
        if (cursor2 != null) cursor2.close();
    }

    private void setupDietSpinner() {
        String[] dietTypes = {"None", "Vegetarian", "Vegan", "Gluten Free", "Ketogenic", "Paleo"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dietTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiet.setAdapter(adapter);
    }
    
    private void setupSavedMeals() {
        savedMealAdapter = new SavedMealAdapter(this, savedMeals, this);
        savedMealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        savedMealsRecyclerView.setAdapter(savedMealAdapter);
    }
    
    private void loadSavedMeals() {
        savedMeals = savedMealDb.getAllSavedMeals();
        if (savedMeals == null || savedMeals.isEmpty()) {
            noSavedMealsText.setVisibility(View.VISIBLE);
            savedMealsRecyclerView.setVisibility(View.GONE);
        } else {
            noSavedMealsText.setVisibility(View.GONE);
            savedMealsRecyclerView.setVisibility(View.VISIBLE);
            savedMealAdapter.updateData(savedMeals);
        }
    }
    
    private void getMealPlan(String calories, String diet, String exclude) {
        // Format diet parameter
        if (diet.equals("None")) {
            diet = "";
        } else {
            diet = diet.toLowerCase().replace(" ", "");
        }
        
        // Parse calories to int
        int targetCalories;
        try {
            targetCalories = Integer.parseInt(calories);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid calorie number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Build request using the existing method
        Request request = spoonacularClient.buildGenerateMealPlanRequest(targetCalories, diet, exclude);
        
        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        txtResult.setVisibility(View.VISIBLE);
                        txtResult.setText("Error: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseData = response.body().string();
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            JSONArray meals = jsonResponse.getJSONArray("meals");
                            
                            // Clear previous meal data
                            currentBreakfast = null;
                            currentLunch = null;
                            currentDinner = null;
                            
                            if (meals.length() > 0) {
                                mealCardsContainer.setVisibility(View.VISIBLE);
                                
                                // Breakfast
                                if (meals.length() >= 1) {
                                    JSONObject breakfast = meals.getJSONObject(0);
                                    currentBreakfast = breakfast;
                                    String breakfastTitle = breakfast.getString("title");
                                    int breakfastReadyTime = breakfast.getInt("readyInMinutes");
                                    int breakfastServings = breakfast.getInt("servings");
                                    
                                    breakfastText.setText(String.format(
                                        "Title: %s\nReady in: %d minutes\nServings: %d",
                                        breakfastTitle, breakfastReadyTime, breakfastServings
                                    ));
                                }
                                
                                // Lunch
                                if (meals.length() >= 2) {
                                    JSONObject lunch = meals.getJSONObject(1);
                                    currentLunch = lunch;
                                    String lunchTitle = lunch.getString("title");
                                    int lunchReadyTime = lunch.getInt("readyInMinutes");
                                    int lunchServings = lunch.getInt("servings");
                                    
                                    lunchText.setText(String.format(
                                        "Title: %s\nReady in: %d minutes\nServings: %d",
                                        lunchTitle, lunchReadyTime, lunchServings
                                    ));
                                }
                                
                                // Dinner
                                if (meals.length() >= 3) {
                                    JSONObject dinner = meals.getJSONObject(2);
                                    currentDinner = dinner;
                                    String dinnerTitle = dinner.getString("title");
                                    int dinnerReadyTime = dinner.getInt("readyInMinutes");
                                    int dinnerServings = dinner.getInt("servings");
                                    
                                    dinnerText.setText(String.format(
                                        "Title: %s\nReady in: %d minutes\nServings: %d",
                                        dinnerTitle, dinnerReadyTime, dinnerServings
                                    ));
                                }
                                
                                // Add save buttons if not already added
                                addSaveButtons();
                                
                            } else {
                                txtResult.setVisibility(View.VISIBLE);
                                txtResult.setText("No meal plan found. Try different parameters.");
                            }
                            
                        } catch (JSONException e) {
                            txtResult.setVisibility(View.VISIBLE);
                            txtResult.setText("Error parsing response: " + e.getMessage());
                        }
                    }
                });
            }
        });
    }
    
    private void addSaveButtons() {
        // Check if buttons already exist
        if (btnSaveBreakfast == null) {
            // Create and add save buttons for each meal
            LinearLayout breakfastLayout = (LinearLayout) ((androidx.cardview.widget.CardView) 
                mealCardsContainer.getChildAt(0)).getChildAt(0);
            
            btnSaveBreakfast = new Button(this);
            btnSaveBreakfast.setText("Save Breakfast");
            btnSaveBreakfast.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveMeal(currentBreakfast, "Breakfast");
                }
            });
            breakfastLayout.addView(btnSaveBreakfast);
            
            LinearLayout lunchLayout = (LinearLayout) ((androidx.cardview.widget.CardView) 
                mealCardsContainer.getChildAt(1)).getChildAt(0);
            
            btnSaveLunch = new Button(this);
            btnSaveLunch.setText("Save Lunch");
            btnSaveLunch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveMeal(currentLunch, "Lunch");
                }
            });
            lunchLayout.addView(btnSaveLunch);
            
            LinearLayout dinnerLayout = (LinearLayout) ((androidx.cardview.widget.CardView) 
                mealCardsContainer.getChildAt(2)).getChildAt(0);
            
            btnSaveDinner = new Button(this);
            btnSaveDinner.setText("Save Dinner");
            btnSaveDinner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveMeal(currentDinner, "Dinner");
                }
            });
            dinnerLayout.addView(btnSaveDinner);
        }
    }
    
    private void saveMeal(JSONObject meal, String mealType) {
        if (meal == null) {
            Toast.makeText(this, "No " + mealType + " data to save", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String title = meal.getString("title");
            int readyInMinutes = meal.optInt("readyInMinutes", 0);
            int servings = meal.optInt("servings", 1);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            
            SavedMeal toSave = new SavedMeal(title, mealType, readyInMinutes, servings, date);
            long id = savedMealDb.insertMeal(toSave);
            
            if (id > 0) {
                Toast.makeText(this, mealType + " saved successfully!", Toast.LENGTH_SHORT).show();
                loadSavedMeals(); // Refresh the list
            } else {
                Toast.makeText(this, "Failed to save " + mealType, Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error saving meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    // Implement OnMealDeletedListener
    @Override
    public void onMealDeleted() {
        loadSavedMeals(); // Refresh the list after deletion
    }
}
