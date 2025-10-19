package com.example.smartdietai;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "smartdiet.db";
    private static final int DATABASE_VERSION = 1;

    // Profile
    public static final String TABLE_PROFILE = "profile";

    // Foods
    public static final String TABLE_FOODS = "foods";
    public static final String FOOD_ID = "id";
    public static final String FOOD_NAME = "name";
    public static final String FOOD_CAL = "calories";
    public static final String FOOD_PROTEIN = "protein";
    public static final String FOOD_CARBS = "carbs";
    public static final String FOOD_FAT = "fat";
    public static final String FOOD_SERVING = "serving";

    // Meals
    public static final String TABLE_MEALS = "meals";
    public static final String MEAL_ID = "id";
    public static final String MEAL_FOOD_ID = "food_id";
    public static final String MEAL_QTY = "quantity";
    public static final String MEAL_DATE = "date";
    public static final String MEAL_TYPE = "meal_type";
    public static final String MEAL_NOTES = "notes";

    // Reminders
    public static final String TABLE_REMINDERS = "reminders";
    public static final String REM_ID = "id";
    public static final String REM_TITLE = "title";
    public static final String REM_HOUR = "hour";
    public static final String REM_MINUTE = "minute";
    public static final String REM_ENABLED = "enabled";

    public DatabaseHelper(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createProfile = "CREATE TABLE " + TABLE_PROFILE + " (id INTEGER PRIMARY KEY, name TEXT, age INTEGER, height REAL, weight REAL, gender TEXT, activity_level TEXT, goal TEXT)";
        String createFoods = "CREATE TABLE " + TABLE_FOODS + " (" +
                FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FOOD_NAME + " TEXT UNIQUE, " +
                FOOD_CAL + " REAL, " +
                FOOD_PROTEIN + " REAL, " +
                FOOD_FAT + " REAL, " +
                FOOD_CARBS + " REAL, " +
                FOOD_SERVING + " TEXT)";
        String createMeals = "CREATE TABLE " + TABLE_MEALS + " (" +
                MEAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MEAL_FOOD_ID + " INTEGER, " +
                MEAL_QTY + " REAL, " +
                MEAL_DATE + " TEXT, " +
                MEAL_TYPE + " TEXT, " +
                MEAL_NOTES + " TEXT)";
        String createRem = "CREATE TABLE " + TABLE_REMINDERS + " (" +
                REM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                REM_TITLE + " TEXT, " +
                REM_HOUR + " INTEGER, " +
                REM_MINUTE + " INTEGER, " +
                REM_ENABLED + " INTEGER)";

        db.execSQL(createProfile);
        db.execSQL(createFoods);
        db.execSQL(createMeals);
        db.execSQL(createRem);

        // insert some sample foods
        insertSampleFoods(db);
    }

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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOODS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        onCreate(db);
    }

    // -------- Profile --------
    public long saveProfile(String name, int age, double height, double weight, String gender, String activity, String goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", 1);
        cv.put("name", name);
        cv.put("age", age);
        cv.put("height", height);
        cv.put("weight", weight);
        cv.put("gender", gender);
        cv.put("activity_level", activity);
        cv.put("goal", goal);
        long res = db.insertWithOnConflict(TABLE_PROFILE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return res;
    }
    public Cursor fetchProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PROFILE + " WHERE id=1", null);
    }

    // -------- Foods --------
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

    // -------- Meals --------
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
        String q = "SELECT m." + MEAL_ID + " as id, f." + FOOD_NAME + " as name, f." + FOOD_CAL + " as calories, m." + MEAL_QTY + " as quantity, m." + MEAL_TYPE + " as meal_type, m." + MEAL_NOTES + " as notes " +
                "FROM " + TABLE_MEALS + " m JOIN " + TABLE_FOODS + " f ON m." + MEAL_FOOD_ID + " = f." + FOOD_ID +
                " WHERE m." + MEAL_DATE + " = ? ORDER BY m." + MEAL_TYPE;
        return db.rawQuery(q, new String[]{date});
    }
    public int deleteMeal(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int r = db.delete(TABLE_MEALS, MEAL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return r;
    }

    // -------- Reminders --------
    public long addReminder(String title, int hour, int minute, int enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(REM_TITLE, title);
        cv.put(REM_HOUR, hour);
        cv.put(REM_MINUTE, minute);
        cv.put(REM_ENABLED, enabled);
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
}
