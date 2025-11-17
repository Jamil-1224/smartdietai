package com.example.smartdietai;

public class Meal {
    private int id;
    private String foodName;
    private double quantity;
    private String mealType;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private String date;
    private String notes;

    public Meal(int id, String foodName, double quantity, String mealType,
                double calories, double protein, double carbs, double fat, String date, String notes) {
        this.id = id;
        this.foodName = foodName;
        this.quantity = quantity;
        this.mealType = mealType;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.date = date;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public String getFoodName() {
        return foodName;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getMealType() {
        return mealType;
    }

    public double getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public double getFat() {
        return fat;
    }

    public String getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
    }

    public double getTotalCalories() {
        return calories * quantity;
    }

    public double getTotalProtein() {
        return protein * quantity;
    }

    public double getTotalCarbs() {
        return carbs * quantity;
    }

    public double getTotalFat() {
        return fat * quantity;
    }

    public String getMealTypeEmoji() {
        switch (mealType.toLowerCase()) {
            case "breakfast":
                return "🌅";
            case "lunch":
                return "🌞";
            case "dinner":
                return "🌙";
            case "snack":
                return "🍎";
            default:
                return "🍽️";
        }
    }
}

