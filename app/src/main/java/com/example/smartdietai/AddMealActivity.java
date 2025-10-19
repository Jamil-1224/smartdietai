package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddMealActivity extends AppCompatActivity {

    Spinner spFoods, spMealType;
    EditText etQty, etNotes;
    Button btnAdd;
    DatabaseHelper db;
    ArrayList<Integer> foodIds = new ArrayList<>();
    ArrayList<String> foodNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);
        db = new DatabaseHelper(this);

        spFoods = findViewById(R.id.spFoods);
        spMealType = findViewById(R.id.spMealType);
        etQty = findViewById(R.id.etQty);
        etNotes = findViewById(R.id.etNotes);
        btnAdd = findViewById(R.id.btnAddMeal);

        loadFoods();
        ArrayAdapter<CharSequence> mtAdapter = ArrayAdapter.createFromResource(this, R.array.meal_types, android.R.layout.simple_spinner_item);
        spMealType.setAdapter(mtAdapter);

        btnAdd.setOnClickListener(v -> {
            try {
                int pos = spFoods.getSelectedItemPosition();
                if (pos < 0) { Toast.makeText(this, "Select food", Toast.LENGTH_SHORT).show(); return; }
                long fid = foodIds.get(pos);
                double qty = Double.parseDouble(etQty.getText().toString().trim());
                String notes = etNotes.getText().toString().trim();
                String mealType = spMealType.getSelectedItem().toString().toLowerCase();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                db.addMeal(fid, qty, date, mealType, notes);
                Toast.makeText(this, "Meal added", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception ex) {
                Toast.makeText(this, "Fill quantity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFoods() {
        Cursor c = db.getAllFoods();
        while (c.moveToNext()) {
            foodIds.add(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_ID)));
            foodNames.add(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.FOOD_NAME)));
        }
        spFoods.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, foodNames));
    }
}
