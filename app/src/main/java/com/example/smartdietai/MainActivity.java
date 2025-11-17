package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    private CardView cardBMI;
    private CardView cardAddFood;
    private CardView cardFoodList;
    private CardView cardAIPlan;
    private CardView cardAddMeal;
    private CardView cardViewMeals;
    private CardView cardReminders;
    private CardView cardProfile;

    private FoodDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean usingFallback = false;
        try {
            setContentView(R.layout.activity_main);
        } catch (Throwable t) {
            usingFallback = true;
            setContentView(R.layout.activity_main_basic);
            Toast.makeText(this, "Running in compatibility mode", Toast.LENGTH_SHORT).show();
        }

        if (usingFallback) {
            setupFallbackUI();
        } else {
            setupCardUI();
        }
        
        initializeDatabase();

        // Check and request notification permission
        checkNotificationPermission();

        // Create notification channel for reminders
        createNotificationChannel();
    }

    // -------- Fallback Buttons (when layout fails) --------
    private void setupFallbackUI() {
        Button btnBMI = findViewById(R.id.btnBMI);
        Button btnAddFood = findViewById(R.id.btnAddFood);
        Button btnFoodList = findViewById(R.id.btnFoodList);
        Button btnAddMeal = findViewById(R.id.btnAddMeal);
        Button btnViewMeals = findViewById(R.id.btnViewMeals);
        Button btnAIPlan = findViewById(R.id.btnAIPlan);
        Button btnReminders = findViewById(R.id.btnReminders);
        Button btnProfile = findViewById(R.id.btnSave);

        if (btnBMI != null) btnBMI.setOnClickListener(v -> startActivity(new Intent(this, BMIActivity.class)));
        if (btnAddFood != null) btnAddFood.setOnClickListener(v -> startActivity(new Intent(this, AddFoodActivity.class)));
        if (btnFoodList != null) btnFoodList.setOnClickListener(v -> startActivity(new Intent(this, FoodListActivity.class)));
        if (btnAddMeal != null) btnAddMeal.setOnClickListener(v -> startActivity(new Intent(this, AddMealActivity.class)));
        if (btnViewMeals != null) btnViewMeals.setOnClickListener(v -> startActivity(new Intent(this, MealsListActivity.class)));
        if (btnAIPlan != null) btnAIPlan.setOnClickListener(v -> startActivity(new Intent(this, DietSuggestionActivity.class)));
        if (btnReminders != null) btnReminders.setOnClickListener(v -> startActivity(new Intent(this, ReminderActivity.class)));
        if (btnProfile != null) btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    // -------- Main Card Layout (for modern UI) --------
    private void setupCardUI() {
        cardBMI = findViewById(R.id.cardBMI);
        cardAddFood = findViewById(R.id.cardAddFood);
        cardFoodList = findViewById(R.id.cardFoodList);
        cardAddMeal = findViewById(R.id.cardAddMeal);
        cardViewMeals = findViewById(R.id.cardMealsList);
        cardAIPlan = findViewById(R.id.cardAIPlan);
        cardReminders = findViewById(R.id.cardReminders);
        cardProfile = findViewById(R.id.cardProfile);

        if (cardBMI != null) cardBMI.setOnClickListener(v -> startActivity(new Intent(this, BMIActivity.class)));
        if (cardAddFood != null) cardAddFood.setOnClickListener(v -> startActivity(new Intent(this, AddFoodActivity.class)));
        if (cardFoodList != null) cardFoodList.setOnClickListener(v -> startActivity(new Intent(this, FoodListActivity.class)));
        if (cardAddMeal != null) cardAddMeal.setOnClickListener(v -> startActivity(new Intent(this, AddMealActivity.class)));
        if (cardViewMeals != null) cardViewMeals.setOnClickListener(v -> startActivity(new Intent(this, MealsListActivity.class)));
        if (cardAIPlan != null) cardAIPlan.setOnClickListener(v -> startActivity(new Intent(this, DietSuggestionActivity.class)));
        if (cardReminders != null) cardReminders.setOnClickListener(v -> startActivity(new Intent(this, ReminderActivity.class)));
        if (cardProfile != null) cardProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    // Initialize database helper
    private void initializeDatabase() {
        dbHelper = new FoodDatabaseHelper(this);
    }

    // -------- Permission --------
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    // -------- Notification Channel --------
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel channel = new NotificationChannel(
                        "dietReminder",
                        "Diet Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Reminders to eat healthy, drink water, and stay fit");
                manager.createNotificationChannel(channel);
            }
        }
    }
}
