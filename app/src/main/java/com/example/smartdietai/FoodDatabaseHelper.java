package com.example.smartdietai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class FoodDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smartdietai.db";
    private static final int DATABASE_VERSION = 2; // Incremented for calorie_target column

    public static final String TABLE_PROFILE = "profile";

    public FoodDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createProfile = "CREATE TABLE " + TABLE_PROFILE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT,"
                + "age INTEGER,"
                + "gender TEXT,"
                + "height REAL,"
                + "weight REAL,"
                + "goal TEXT,"
                + "water_target INTEGER,"
                + "calorie_target INTEGER DEFAULT 2000"
                + ");";
        db.execSQL(createProfile);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add calorie_target column if upgrading from version 1
            try {
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN calorie_target INTEGER DEFAULT 2000");
            } catch (Exception e) {
                // Column might already exist, ignore
            }
        }
    }

    /**
     * Returns a Cursor for the profile row (latest row if multiple exist).
     * Caller is responsible for closing the Cursor.
     */
    public Cursor getProfile() {
        SQLiteDatabase db = getReadableDatabase();
        // Return the most recent profile (if any)
        return db.query(TABLE_PROFILE, null, null, null, null, null, "id DESC", "1");
    }

    /**
     * Inserts or updates a single profile row. If a profile row exists, update it; otherwise insert.
     */
    public long saveOrUpdateProfile(String name, int age, String gender, double height, double weight, String goal, int waterTarget) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("age", age);
        values.put("gender", gender);
        values.put("height", height);
        values.put("weight", weight);
        values.put("goal", goal);
        values.put("water_target", waterTarget);

        // Check if a profile exists
        Cursor cursor = db.query(TABLE_PROFILE, new String[]{"id"}, null, null, null, null, "id DESC", "1");
        long affectedId;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            affectedId = db.update(TABLE_PROFILE, values, "id = ?", new String[]{String.valueOf(id)});
        } else {
            affectedId = db.insert(TABLE_PROFILE, null, values);
        }
        if (cursor != null) cursor.close();
        return affectedId;
    }

    /**
     * Updates only the calorie target for the existing profile
     */
    public void updateCalorieTarget(int calorieTarget) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("calorie_target", calorieTarget);

        // Update the most recent profile
        Cursor cursor = db.query(TABLE_PROFILE, new String[]{"id"}, null, null, null, null, "id DESC", "1");
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            db.update(TABLE_PROFILE, values, "id = ?", new String[]{String.valueOf(id)});
        }
        if (cursor != null) cursor.close();
    }
}

