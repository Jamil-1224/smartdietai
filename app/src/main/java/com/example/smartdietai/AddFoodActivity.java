package com.example.smartdietai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import org.json.JSONObject;
import okhttp3.*;
import java.io.IOException;

public class AddFoodActivity extends AppCompatActivity {

    EditText nameInput, calorieInput, proteinInput, carbInput, fatInput, servingInput;
    Button addButton, scanQRButton;
    DatabaseHelper db;

    // Barcode scanner launcher
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null && !result.getContents().trim().isEmpty()) {
                    String scannedData = result.getContents().trim();
                    String format = result.getFormatName();

                    // Log scanned data for debugging
                    android.util.Log.d("QRScanner", "=== SCAN RESULT ===");
                    android.util.Log.d("QRScanner", "Format: " + format);
                    android.util.Log.d("QRScanner", "Data: " + scannedData);
                    android.util.Log.d("QRScanner", "Data length: " + scannedData.length());

                    // Check if it's a custom JSON QR code or a product barcode
                    if (isCustomFoodJSON(scannedData)) {
                        // Custom JSON QR code
                        android.util.Log.d("QRScanner", "Type: Custom JSON QR Code");
                        Toast.makeText(this, "Custom food QR detected!", Toast.LENGTH_SHORT).show();
                        parseCustomFoodQR(scannedData);
                    } else {
                        // Product barcode - fetch from API
                        android.util.Log.d("QRScanner", "Type: Product Barcode");
                        Toast.makeText(this, "Product barcode detected: " + scannedData, Toast.LENGTH_SHORT).show();
                        fetchFoodDataFromBarcode(scannedData);
                    }
                } else {
                    android.util.Log.d("QRScanner", "Scan cancelled or empty result");
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    /**
     * Check if scanned data is a custom food JSON
     */
    private boolean isCustomFoodJSON(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        // Try to parse as JSON and check for foodName field
        try {
            JSONObject json = new JSONObject(data);
            return json.has("foodName");
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        db = new DatabaseHelper(this);
        nameInput = findViewById(R.id.nameInput);
        calorieInput = findViewById(R.id.calorieInput);
        proteinInput = findViewById(R.id.proteinInput);
        carbInput = findViewById(R.id.carbInput);
        fatInput = findViewById(R.id.fatInput);
        servingInput = findViewById(R.id.servingInput);
        addButton = findViewById(R.id.addButton);
        scanQRButton = findViewById(R.id.scanQRButton);

        // QR Scanner button click
        scanQRButton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE, ScanOptions.CODE_128,
                    ScanOptions.CODE_39, ScanOptions.EAN_13, ScanOptions.EAN_8,
                    ScanOptions.UPC_A, ScanOptions.UPC_E);
            options.setPrompt("Place QR code/barcode in the frame");
            options.setCameraId(0);
            options.setBeepEnabled(true);
            options.setBarcodeImageEnabled(true);
            options.setOrientationLocked(false); // Allow natural orientation (portrait/vertical)
            options.setTimeout(30000); // 30 second timeout
            barcodeLauncher.launch(options);
        });

        // Manual add button click
        addButton.setOnClickListener(v -> {
            try {
                String name = nameInput.getText().toString().trim();
                double calories = Double.parseDouble(calorieInput.getText().toString().trim());
                double protein = Double.parseDouble(proteinInput.getText().toString().trim());
                double carbs = Double.parseDouble(carbInput.getText().toString().trim());
                double fat = Double.parseDouble(fatInput.getText().toString().trim());
                String serving = servingInput.getText().toString().trim();

                if (name.isEmpty() || serving.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                long id = db.addFood(name, calories, protein, carbs, fat, serving);
                if (id == -1) Toast.makeText(this, "Food already exists or error", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(this, "Food added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (Exception ex) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Parse custom JSON QR code with food data
     */
    private void parseCustomFoodQR(String jsonData) {
        try {
            android.util.Log.d("QRScanner", "Parsing JSON: " + jsonData);

            JSONObject json = new JSONObject(jsonData);

            // Extract data with flexible type handling
            String name = json.optString("foodName", "");
            String serving = json.optString("servingSize", "");

            // Handle both string and number types for nutritional values
            double calories = getDoubleValue(json, "calories");
            double protein = getDoubleValue(json, "protein");
            double carbs = getDoubleValue(json, "carbs");
            double fat = getDoubleValue(json, "fat");

            android.util.Log.d("QRScanner", "Parsed - Name: " + name + ", Calories: " + calories +
                    ", Protein: " + protein + ", Carbs: " + carbs + ", Fat: " + fat);

            // Validate that we have at least a name
            if (name.isEmpty()) {
                Toast.makeText(this, "Error: Food name is missing", Toast.LENGTH_SHORT).show();
                return;
            }

            // Auto-fill the fields directly (we're already on UI thread)
            nameInput.setText(name);
            servingInput.setText(serving.isEmpty() ? "1" : serving);

            // Set values, showing "0" if they are 0 (for your Apple data)
            calorieInput.setText(String.valueOf((int)calories));
            proteinInput.setText(String.valueOf((int)protein));
            carbInput.setText(String.valueOf((int)carbs));
            fatInput.setText(String.valueOf((int)fat));

            android.util.Log.d("QRScanner", "Fields updated successfully!");
            Toast.makeText(this, "Custom QR data loaded! Review and add.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            android.util.Log.e("QRScanner", "Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error parsing QR code: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Helper method to extract double value from JSON, handling both string and number types
     */
    private double getDoubleValue(JSONObject json, String key) {
        try {
            if (json.has(key)) {
                Object value = json.get(key);
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else if (value instanceof String) {
                    String strValue = (String) value;
                    if (!strValue.isEmpty()) {
                        return Double.parseDouble(strValue);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("QRScanner", "Error getting value for " + key + ": " + e.getMessage());
        }
        return 0;
    }

    /**
     * Fetch food data from Open Food Facts API using barcode
     */
    private void fetchFoodDataFromBarcode(String barcode) {
        Toast.makeText(this, "Searching product database...", Toast.LENGTH_SHORT).show();

        android.util.Log.d("BarcodeScanner", "Fetching data for barcode: " + barcode);

        OkHttpClient client = new OkHttpClient();
        String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";

        android.util.Log.d("BarcodeScanner", "API URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "SmartDietAI - Android App")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                android.util.Log.e("BarcodeScanner", "Network error: " + e.getMessage());
                runOnUiThread(() -> {
                    // Pre-fill the barcode as name so user can search manually
                    nameInput.setText("Product " + barcode);
                    servingInput.setText("100g");
                    Toast.makeText(AddFoodActivity.this,
                            "Network error. Barcode filled - please add details manually.",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        android.util.Log.d("BarcodeScanner", "API Response: " + responseData);

                        JSONObject json = new JSONObject(responseData);

                        if (json.getInt("status") == 1) {
                            JSONObject product = json.getJSONObject("product");

                            android.util.Log.d("BarcodeScanner", "Product found!");

                            // Extract food information with better fallbacks
                            String name = product.optString("product_name", "");
                            if (name.isEmpty()) {
                                name = product.optString("product_name_en", "");
                            }
                            if (name.isEmpty()) {
                                name = product.optString("generic_name", "Product " + barcode);
                            }

                            // Better serving size handling
                            String servingQty = product.optString("serving_quantity", "100");
                            String servingUnit = product.optString("serving_quantity_unit", "g");
                            String serving = servingQty + servingUnit;

                            // If no serving info, default to 100g
                            if (servingQty.isEmpty() || servingQty.equals("null")) {
                                serving = "100g";
                            }

                            // Get nutriments (per 100g)
                            JSONObject nutriments = product.optJSONObject("nutriments");
                            double calories = 0, protein = 0, carbs = 0, fat = 0;

                            if (nutriments != null) {
                                // Try multiple fields for calories
                                calories = nutriments.optDouble("energy-kcal_100g", 0);
                                if (calories == 0) {
                                    double energyKj = nutriments.optDouble("energy-kj_100g", 0);
                                    if (energyKj > 0) {
                                        calories = energyKj / 4.184; // Convert kJ to kcal
                                    }
                                }
                                if (calories == 0) {
                                    double energy = nutriments.optDouble("energy_100g", 0);
                                    if (energy > 100) { // Likely in kJ
                                        calories = energy / 4.184;
                                    } else {
                                        calories = energy;
                                    }
                                }

                                protein = nutriments.optDouble("proteins_100g", 0);
                                carbs = nutriments.optDouble("carbohydrates_100g", 0);
                                fat = nutriments.optDouble("fat_100g", 0);

                                android.util.Log.d("BarcodeScanner", String.format(
                                    "Nutrition: Cal=%.1f, Protein=%.1f, Carbs=%.1f, Fat=%.1f",
                                    calories, protein, carbs, fat));
                            }

                            final String finalName = name;
                            final String finalServing = serving;
                            final double finalCalories = calories;
                            final double finalProtein = protein;
                            final double finalCarbs = carbs;
                            final double finalFat = fat;

                            // Update UI on main thread
                            runOnUiThread(() -> {
                                nameInput.setText(finalName);
                                servingInput.setText(finalServing);
                                calorieInput.setText(String.format("%.0f", finalCalories));
                                proteinInput.setText(String.format("%.1f", finalProtein));
                                carbInput.setText(String.format("%.1f", finalCarbs));
                                fatInput.setText(String.format("%.1f", finalFat));

                                Toast.makeText(AddFoodActivity.this,
                                        "Product found! Review and add.",
                                        Toast.LENGTH_LONG).show();

                                android.util.Log.d("BarcodeScanner", "UI updated successfully!");
                            });
                        } else {
                            android.util.Log.w("BarcodeScanner", "Product not found in database (status=0)");
                            runOnUiThread(() -> {
                                // Pre-fill with barcode so user can add manually
                                nameInput.setText("Product " + barcode);
                                servingInput.setText("100g");
                                Toast.makeText(AddFoodActivity.this,
                                        "Product not found in database.\nBarcode filled - please add nutrition details manually.",
                                        Toast.LENGTH_LONG).show();
                            });
                        }
                    } catch (Exception e) {
                        android.util.Log.e("BarcodeScanner", "Parse error: " + e.getMessage());
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            nameInput.setText("Product " + barcode);
                            servingInput.setText("100g");
                            Toast.makeText(AddFoodActivity.this,
                                    "Error reading product data.\nBarcode filled - please add details manually.",
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    android.util.Log.e("BarcodeScanner", "HTTP error: " + response.code());
                    runOnUiThread(() -> {
                        nameInput.setText("Product " + barcode);
                        servingInput.setText("100g");
                        Toast.makeText(AddFoodActivity.this,
                                "Server error. Barcode filled - please add details manually.",
                                Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }
}
