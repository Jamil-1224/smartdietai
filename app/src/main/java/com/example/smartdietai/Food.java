package com.example.smartdietai;

public class Food {
    private int id;
    private String name;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;
    private String serving;

    public Food(int id, String name, double calories, double protein, double carbs, double fat, String serving) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.serving = serving;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public String getServing() {
        return serving;
    }
}

