package com.example.smartdietai;

/**
 * Model class for saved meals
 */
public class SavedMeal {
    private int id;
    private String title;
    private String mealType; // breakfast, lunch, dinner
    private int readyInMinutes;
    private int servings;
    private String date;

    // Constructor for creating a new meal
    public SavedMeal(String title, String mealType, int readyInMinutes, int servings, String date) {
        this.title = title;
        this.mealType = mealType;
        this.readyInMinutes = readyInMinutes;
        this.servings = servings;
        this.date = date;
    }

    // Constructor for retrieving from database
    public SavedMeal(int id, String title, String mealType, int readyInMinutes, int servings, String date) {
        this.id = id;
        this.title = title;
        this.mealType = mealType;
        this.readyInMinutes = readyInMinutes;
        this.servings = servings;
        this.date = date;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public void setReadyInMinutes(int readyInMinutes) {
        this.readyInMinutes = readyInMinutes;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}