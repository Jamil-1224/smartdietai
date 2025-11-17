package com.example.smartdietai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

public class FoodAdapter extends ArrayAdapter<Food> {
    private Context context;
    private List<Food> foods;

    public FoodAdapter(@NonNull Context context, @NonNull List<Food> foods) {
        super(context, 0, foods);
        this.context = context;
        this.foods = foods;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.food_item_card, parent, false);
            holder = new ViewHolder();
            holder.foodName = convertView.findViewById(R.id.food_name);
            holder.foodServing = convertView.findViewById(R.id.food_serving);
            holder.foodCalories = convertView.findViewById(R.id.food_calories);
            holder.foodProtein = convertView.findViewById(R.id.food_protein);
            holder.foodCarbs = convertView.findViewById(R.id.food_carbs);
            holder.foodFat = convertView.findViewById(R.id.food_fat);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Food food = foods.get(position);

        holder.foodName.setText(food.getName());
        holder.foodServing.setText(food.getServing());
        holder.foodCalories.setText(String.format(Locale.getDefault(), "%.0f", food.getCalories()));
        holder.foodProtein.setText(String.format(Locale.getDefault(), "%.1f", food.getProtein()));
        holder.foodCarbs.setText(String.format(Locale.getDefault(), "%.1f", food.getCarbs()));
        holder.foodFat.setText(String.format(Locale.getDefault(), "%.1f", food.getFat()));

        return convertView;
    }

    static class ViewHolder {
        TextView foodName;
        TextView foodServing;
        TextView foodCalories;
        TextView foodProtein;
        TextView foodCarbs;
        TextView foodFat;
    }
}

