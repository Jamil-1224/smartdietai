package com.example.smartdietai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MealAdapter extends ArrayAdapter<MealItem> {

    private final Context context;

    public MealAdapter(Context context, ArrayList<MealItem> meals) {
        super(context, 0, meals);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MealItem meal = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.meal_list_item, parent, false);
        }

        TextView tvMealIcon = convertView.findViewById(R.id.tvMealIcon);
        TextView tvMealType = convertView.findViewById(R.id.tvMealType);
        TextView tvFoodName = convertView.findViewById(R.id.tvFoodName);
        TextView tvQuantity = convertView.findViewById(R.id.tvQuantity);
        TextView tvCalories = convertView.findViewById(R.id.tvCalories);
        TextView tvNotes = convertView.findViewById(R.id.tvNotes);

        if (meal != null) {
            // Set meal type and icon
            String mealType = meal.getMealType().toUpperCase();
            tvMealType.setText(mealType);

            // Set appropriate emoji based on meal type
            String icon = getMealIcon(meal.getMealType());
            tvMealIcon.setText(icon);

            // Set food name
            tvFoodName.setText(meal.getFoodName());

            // Set quantity
            tvQuantity.setText(String.valueOf(meal.getQuantity()));

            // Set calories
            tvCalories.setText(String.valueOf(meal.getCalories()));

            // Set notes if available
            if (meal.getNotes() != null && !meal.getNotes().trim().isEmpty()) {
                tvNotes.setVisibility(View.VISIBLE);
                tvNotes.setText(" • " + meal.getNotes());
            } else {
                tvNotes.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private String getMealIcon(String mealType) {
        switch (mealType.toLowerCase()) {
            case "breakfast":
                return "🌅";
            case "lunch":
                return "🍽️";
            case "dinner":
                return "🌙";
            case "snack":
                return "🍎";
            default:
                return "🍴";
        }
    }
}
