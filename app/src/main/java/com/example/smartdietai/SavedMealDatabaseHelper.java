package com.example.smartdietai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SavedMealDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "saved_meals.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    public static final String TABLE_SAVED_MEALS = "saved_meals";

    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_MEAL_TYPE = "meal_type";
    public static final String COLUMN_READY_IN_MINUTES = "ready_in_minutes";
    public static final String COLUMN_SERVINGS = "servings";
    public static final String COLUMN_DATE = "date";

    // Create table SQL query
    private static final String CREATE_TABLE_SAVED_MEALS =
            "CREATE TABLE " + TABLE_SAVED_MEALS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_MEAL_TYPE + " TEXT,"
                    + COLUMN_READY_IN_MINUTES + " INTEGER,"
                    + COLUMN_SERVINGS + " INTEGER,"
                    + COLUMN_DATE + " TEXT"
                    + ")";

    public SavedMealDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SAVED_MEALS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_MEALS);
        onCreate(db);
    }

    // Insert a new meal
    public long insertMeal(SavedMeal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, meal.getTitle());
        values.put(COLUMN_MEAL_TYPE, meal.getMealType());
        values.put(COLUMN_READY_IN_MINUTES, meal.getReadyInMinutes());
        values.put(COLUMN_SERVINGS, meal.getServings());
        values.put(COLUMN_DATE, meal.getDate());

        long id = db.insert(TABLE_SAVED_MEALS, null, values);
        db.close();
        return id;
    }

    // Get all saved meals
    public List<SavedMeal> getAllSavedMeals() {
        List<SavedMeal> meals = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SAVED_MEALS + " ORDER BY " + COLUMN_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SavedMeal meal = new SavedMeal(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_TYPE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_READY_IN_MINUTES)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERVINGS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                );
                meals.add(meal);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return meals;
    }

    // Delete a meal
    public void deleteMeal(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SAVED_MEALS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }
}