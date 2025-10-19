package com.example.smartdietai;

public class CalorieCalculator {

    // Mifflin-St Jeor simplified (height in cm, weight in kg)
    public static double calculateBMR(int age, double heightCm, double weightKg, String gender) {
        if (gender == null) gender = "male";
        if (gender.equalsIgnoreCase("male")) {
            return 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
        } else {
            return 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
        }
    }

    public static double applyActivityFactor(double bmr, String activityLevel) {
        if (activityLevel == null) return bmr * 1.2;
        switch (activityLevel.toLowerCase()) {
            case "sedentary": return bmr * 1.2;
            case "light": return bmr * 1.375;
            case "moderate": return bmr * 1.55;
            case "active": return bmr * 1.725;
            default: return bmr * 1.2;
        }
    }

    public static double adjustForGoal(double maintenanceCalories, String goal) {
        if (goal == null) return maintenanceCalories;
        if ("lose".equalsIgnoreCase(goal)) return maintenanceCalories - 500;
        if ("gain".equalsIgnoreCase(goal)) return maintenanceCalories + 500;
        return maintenanceCalories;
    }

    public static double dailyCalorieTarget(int age, double heightCm, double weightKg, String gender, String activity, String goal) {
        double bmr = calculateBMR(age, heightCm, weightKg, gender);
        double maintenance = applyActivityFactor(bmr, activity);
        return adjustForGoal(maintenance, goal);
    }
}
