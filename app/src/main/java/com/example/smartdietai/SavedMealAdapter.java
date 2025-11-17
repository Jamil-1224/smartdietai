package com.example.smartdietai;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SavedMealAdapter extends RecyclerView.Adapter<SavedMealAdapter.MealViewHolder> {
    private List<SavedMeal> mealList;
    private Context context;
    private SavedMealDatabaseHelper dbHelper;
    private OnMealDeletedListener listener;

    public interface OnMealDeletedListener {
        void onMealDeleted();
    }

    public SavedMealAdapter(Context context, List<SavedMeal> mealList, OnMealDeletedListener listener) {
        this.context = context;
        this.mealList = mealList;
        this.dbHelper = new SavedMealDatabaseHelper(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.saved_meal_item, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        SavedMeal meal = mealList.get(position);
        
        holder.mealTitle.setText(meal.getTitle());
        holder.mealType.setText(meal.getMealType());
        holder.mealInfo.setText("Ready in: " + meal.getReadyInMinutes() + " minutes • Servings: " + meal.getServings());
        holder.mealDate.setText("Saved on: " + meal.getDate());
        
        holder.deleteButton.setOnClickListener(v -> {
            // Show confirmation dialog
            new AlertDialog.Builder(context)
                    .setTitle("Delete Meal")
                    .setMessage("Are you sure you want to delete \"" + meal.getTitle() + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // User confirmed deletion
                        int mealId = meal.getId();
                        dbHelper.deleteMeal(mealId);
                        mealList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mealList.size());

                        if (listener != null) {
                            listener.onMealDeleted();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public void updateData(List<SavedMeal> newMeals) {
        this.mealList = newMeals;
        notifyDataSetChanged();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView mealTitle, mealType, mealInfo, mealDate;
        Button deleteButton;

        MealViewHolder(View itemView) {
            super(itemView);
            mealTitle = itemView.findViewById(R.id.saved_meal_title);
            mealType = itemView.findViewById(R.id.saved_meal_type);
            mealInfo = itemView.findViewById(R.id.saved_meal_info);
            mealDate = itemView.findViewById(R.id.saved_meal_date);
            deleteButton = itemView.findViewById(R.id.btn_delete_meal);
        }
    }
}