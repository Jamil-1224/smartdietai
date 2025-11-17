package com.example.smartdietai;

public class MealItem {
    private int id;
    private String foodName;
    private String mealType;
    private double quantity;
    private int calories;
    private String notes;

    public MealItem(int id, String foodName, String mealType, double quantity, int calories, String notes) {
        this.id = id;
        this.foodName = foodName;
        this.mealType = mealType;
        this.quantity = quantity;
        this.calories = calories;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getMealType() {
        return mealType;
    }

    public double getQuantity() {
        return quantity;
    }

    public int getCalories() {
        return calories;
    }

    public String getNotes() {
        return notes;
    }
}

