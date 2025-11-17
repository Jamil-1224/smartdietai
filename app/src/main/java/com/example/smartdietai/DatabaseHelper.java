package com.example.smartdietai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smartdiet.db";
    private static final int DATABASE_VERSION = 4; // incremented version for days column in reminders

    // ---------------- Profile ----------------
    public static final String TABLE_PROFILE = "profile";
    public static final String PROFILE_ID = "id";
    public static final String PROFILE_NAME = "name";
    public static final String PROFILE_AGE = "age";
    public static final String PROFILE_HEIGHT = "height";
    public static final String PROFILE_WEIGHT = "weight";
    public static final String PROFILE_GENDER = "gender";
    public static final String PROFILE_ACTIVITY = "activity_level";
    public static final String PROFILE_GOAL = "goal";
    public static final String PROFILE_WATER = "water_target"; // new field
    public static final String PROFILE_CALORIE_TARGET = "calorie_target"; // calculated calorie target

    // ---------------- Foods ----------------
    public static final String TABLE_FOODS = "foods";
    public static final String FOOD_ID = "id";
    public static final String FOOD_NAME = "name";
    public static final String FOOD_CAL = "calories";
    public static final String FOOD_PROTEIN = "protein";
    public static final String FOOD_CARBS = "carbs";
    public static final String FOOD_FAT = "fat";
    public static final String FOOD_SERVING = "serving";

    // ---------------- Meals ----------------
    public static final String TABLE_MEALS = "meals";
    public static final String MEAL_ID = "id";
    public static final String MEAL_FOOD_ID = "food_id";
    public static final String MEAL_QTY = "quantity";
    public static final String MEAL_DATE = "date";
    public static final String MEAL_TYPE = "meal_type";
    public static final String MEAL_NOTES = "notes";

    // ---------------- Reminders ----------------
    public static final String TABLE_REMINDERS = "reminders";
    public static final String REM_ID = "id";
    public static final String REM_TITLE = "title";
    public static final String REM_HOUR = "hour";
    public static final String REM_MINUTE = "minute";
    public static final String REM_ENABLED = "enabled";
    public static final String REM_DAYS = "days"; // Stores days as comma-separated string (e.g., "Mon,Wed,Fri")

    public DatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // PROFILE TABLE
        String createProfile = "CREATE TABLE " + TABLE_PROFILE + " (" +
                PROFILE_ID + " INTEGER PRIMARY KEY, " +
                PROFILE_NAME + " TEXT, " +
                PROFILE_AGE + " INTEGER, " +
                PROFILE_HEIGHT + " REAL, " +
                PROFILE_WEIGHT + " REAL, " +
                PROFILE_GENDER + " TEXT, " +
                PROFILE_ACTIVITY + " TEXT, " +
                PROFILE_GOAL + " TEXT, " +
                PROFILE_WATER + " INTEGER DEFAULT 2000, " + // Default 2L water target
                PROFILE_CALORIE_TARGET + " INTEGER DEFAULT 2000)"; // Default calorie target

        // FOODS TABLE
        String createFoods = "CREATE TABLE " + TABLE_FOODS + " (" +
                FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FOOD_NAME + " TEXT UNIQUE, " +
                FOOD_CAL + " REAL, " +
                FOOD_PROTEIN + " REAL, " +
                FOOD_CARBS + " REAL, " +
                FOOD_FAT + " REAL, " +
                FOOD_SERVING + " TEXT)";

        // MEALS TABLE
        String createMeals = "CREATE TABLE " + TABLE_MEALS + " (" +
                MEAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEAL_FOOD_ID + " INTEGER, " +
                MEAL_QTY + " REAL, " +
                MEAL_DATE + " TEXT, " +
                MEAL_TYPE + " TEXT, " +
                MEAL_NOTES + " TEXT)";

        // REMINDERS TABLE
        String createReminders = "CREATE TABLE " + TABLE_REMINDERS + " (" +
                REM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                REM_TITLE + " TEXT, " +
                REM_HOUR + " INTEGER, " +
                REM_MINUTE + " INTEGER, " +
                REM_ENABLED + " INTEGER, " +
                REM_DAYS + " TEXT DEFAULT 'Sun,Mon,Tue,Wed,Thu,Fri,Sat')";

        db.execSQL(createProfile);
        db.execSQL(createFoods);
        db.execSQL(createMeals);
        db.execSQL(createReminders);

        insertSampleFoods(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 4) {
            // Add days column to reminders table
            try {
                db.execSQL("ALTER TABLE " + TABLE_REMINDERS + " ADD COLUMN " + REM_DAYS + " TEXT DEFAULT 'Sun,Mon,Tue,Wed,Thu,Fri,Sat'");
            } catch (Exception e) {
                // Column might already exist, ignore
            }
        }
        if (oldV < 3) {
            // Add calorie_target column if upgrading from version 2 or earlier
            try {
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN " + PROFILE_CALORIE_TARGET + " INTEGER DEFAULT 2000");
            } catch (Exception e) {
                // Column might already exist, ignore
            }
        }
        if (oldV < 2) {
            // Add water_target column if upgrading from version 1
            try {
                db.execSQL("ALTER TABLE " + TABLE_PROFILE + " ADD COLUMN " + PROFILE_WATER + " INTEGER DEFAULT 2000");
            } catch (Exception e) {
                // Column might already exist, ignore
            }
        }
    }

    // ---------- SAMPLE FOODS ----------
    private void insertSampleFoods(SQLiteDatabase db) {
        insertFood(db, "Boiled Rice (100g)", 130, 2.4, 28.0, 0.3, "100g");
        insertFood(db, "Boiled Egg (1)", 78, 6.0, 0.6, 5.0, "1");
        insertFood(db, "Chicken Breast (100g)", 165, 31.0, 0.0, 3.6, "100g");
        insertFood(db, "Banana (1 medium)", 105, 1.3, 27.0, 0.4, "1");
        insertFood(db, "Apple (1 medium)", 95, 0.5, 25.0, 0.3, "1");
    }

    private void insertFood(SQLiteDatabase db, String name, double cal, double protein, double carbs, double fat, String serving) {
        ContentValues cv = new ContentValues();
        cv.put(FOOD_NAME, name);
        cv.put(FOOD_CAL, cal);
        cv.put(FOOD_PROTEIN, protein);
        cv.put(FOOD_CARBS, carbs);
        cv.put(FOOD_FAT, fat);
        cv.put(FOOD_SERVING, serving);
        db.insert(TABLE_FOODS, null, cv);
    }

    // ---------- PROFILE ----------
    public long saveProfile(String name, int age, double height, double weight, String gender, String activity, String goal, int waterTarget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(PROFILE_ID, 1);
        cv.put(PROFILE_NAME, name);
        cv.put(PROFILE_AGE, age);
        cv.put(PROFILE_HEIGHT, height);
        cv.put(PROFILE_WEIGHT, weight);
        cv.put(PROFILE_GENDER, gender);
        cv.put(PROFILE_ACTIVITY, activity);
        cv.put(PROFILE_GOAL, goal);
        cv.put(PROFILE_WATER, waterTarget);
        long res = db.insertWithOnConflict(TABLE_PROFILE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return res;
    }

    // New method to update just the calorie target
    public void updateCalorieTarget(int calorieTarget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(PROFILE_CALORIE_TARGET, calorieTarget);
        db.update(TABLE_PROFILE, cv, PROFILE_ID + "=1", null);
        db.close();
    }

    public Cursor fetchProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PROFILE + " WHERE " + PROFILE_ID + "=1", null);
    }

    // ---------- FOODS ----------
    public long addFood(String name, double calories, double protein, double carbs, double fat, String serving) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FOOD_NAME, name);
        cv.put(FOOD_CAL, calories);
        cv.put(FOOD_PROTEIN, protein);
        cv.put(FOOD_CARBS, carbs);
        cv.put(FOOD_FAT, fat);
        cv.put(FOOD_SERVING, serving);
        long res = db.insert(TABLE_FOODS, null, cv);
        db.close();
        return res;
    }

    public Cursor getAllFoods() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_FOODS + " ORDER BY " + FOOD_NAME, null);
    }

    public Cursor getFoodById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_FOODS + " WHERE " + FOOD_ID + "=" + id, null);
    }

    public int updateFood(long id, String name, double calories, double protein, double carbs, double fat, String serving) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FOOD_NAME, name);
        cv.put(FOOD_CAL, calories);
        cv.put(FOOD_PROTEIN, protein);
        cv.put(FOOD_CARBS, carbs);
        cv.put(FOOD_FAT, fat);
        cv.put(FOOD_SERVING, serving);
        int r = db.update(TABLE_FOODS, cv, FOOD_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return r;
    }

    public int deleteFood(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int r = db.delete(TABLE_FOODS, FOOD_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return r;
    }

    // ---------- MEALS ----------
    public long addMeal(long foodId, double qty, String date, String mealType, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(MEAL_FOOD_ID, foodId);
        cv.put(MEAL_QTY, qty);
        cv.put(MEAL_DATE, date);
        cv.put(MEAL_TYPE, mealType);
        cv.put(MEAL_NOTES, notes);
        long res = db.insert(TABLE_MEALS, null, cv);
        db.close();
        return res;
    }

    public Cursor getMealsByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT m." + MEAL_ID + ", f." + FOOD_NAME + ", f." + FOOD_CAL + ", m." + MEAL_QTY + ", m." + MEAL_TYPE + ", m." + MEAL_NOTES +
                " FROM " + TABLE_MEALS + " m JOIN " + TABLE_FOODS + " f ON m." + MEAL_FOOD_ID + " = f." + FOOD_ID +
                " WHERE m." + MEAL_DATE + " = ? ORDER BY m." + MEAL_TYPE;
        return db.rawQuery(query, new String[]{date});
    }

    public int deleteMeal(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int r = db.delete(TABLE_MEALS, MEAL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return r;
    }

    // ---------- REMINDERS ----------
    public long addReminder(String title, int hour, int minute, int enabled, String days) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(REM_TITLE, title);
        cv.put(REM_HOUR, hour);
        cv.put(REM_MINUTE, minute);
        cv.put(REM_ENABLED, enabled);
        cv.put(REM_DAYS, days);
        long res = db.insert(TABLE_REMINDERS, null, cv);
        db.close();
        return res;
    }

    public Cursor getReminders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_REMINDERS + " ORDER BY " + REM_HOUR + "," + REM_MINUTE, null);
    }

    public int setReminderEnabled(long id, int enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(REM_ENABLED, enabled);
        int r = db.update(TABLE_REMINDERS, cv, REM_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return r;
    }

    public int deleteReminder(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int r = db.delete(TABLE_REMINDERS, REM_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return r;
    }

    // ---------- CLEANUP METHODS ----------
    public void cleanupInvalidFoods() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Remove invalid single-word foods & duplicates
        String[] invalidNames = {"cat", "eat", "dog", "apple", "banana", "root", "meat", "fish"};
        for (String invalid : invalidNames) {
            db.delete(TABLE_FOODS, "LOWER(" + FOOD_NAME + ") = ?", new String[]{invalid});
        }
        // Remove empty or too short names
        db.delete(TABLE_FOODS, FOOD_NAME + " IS NULL OR TRIM(" + FOOD_NAME + ") = ''", null);
        db.delete(TABLE_FOODS, "LENGTH(TRIM(" + FOOD_NAME + ")) < 3", null);
        db.close();
    }

    public void resetFoodsToDefault() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Clear all foods
        db.delete(TABLE_FOODS, null, null);
        // Insert only valid default foods
        insertSampleFoods(db);
        db.close();
    }
}
