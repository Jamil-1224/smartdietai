package com.example.smartdietai;

import okhttp3.*;
import org.json.*;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SpoonacularApiClient {
    private final OkHttpClient client;
    private final String baseUrl;
    private final String apiKey;
    private final String username;

    public SpoonacularApiClient(OkHttpClient client) {
        this.client = client;
        this.baseUrl = BuildConfig.SPOONACULAR_BASE_URL;
        this.apiKey = BuildConfig.SPOONACULAR_API_KEY;
        this.username = BuildConfig.SPOONACULAR_USERNAME;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    /**
     * Generate a meal plan for a specific calorie target
     */
    public Request buildGenerateMealPlanRequest(int targetCalories, String diet, String exclude) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + "/mealplanner/generate").newBuilder()
                .addQueryParameter("apiKey", apiKey)
                .addQueryParameter("timeFrame", "day")
                .addQueryParameter("targetCalories", String.valueOf(targetCalories));
        
        if (diet != null && !diet.isEmpty()) {
            urlBuilder.addQueryParameter("diet", diet);
        }
        
        if (exclude != null && !exclude.isEmpty()) {
            urlBuilder.addQueryParameter("exclude", exclude);
        }
        
        return new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();
    }

    /**
     * Connect a user to Spoonacular
     */
    public Request buildConnectUserRequest() {
        JSONObject body = new JSONObject();
        try {
            body.put("username", username);
            // We don't need to set a password for the demo
        } catch (JSONException ignored) {
        }

        return new Request.Builder()
                .url(baseUrl + "/users/connect?apiKey=" + apiKey)
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();
    }

    /**
     * Add a meal to a user's meal plan
     */
    public Request buildAddToMealPlanRequest(String username, String hash, int mealId, int slot) {
        // Get today's date in timestamp format
        Calendar calendar = Calendar.getInstance();
        long timestamp = calendar.getTimeInMillis() / 1000;
        
        JSONObject body = new JSONObject();
        try {
            body.put("date", timestamp);
            body.put("slot", slot);
            body.put("position", 0);
            body.put("type", "RECIPES");
            
            JSONObject value = new JSONObject();
            value.put("id", mealId);
            value.put("servings", 1);
            value.put("title", "Meal");
            value.put("imageType", "jpg");
            
            body.put("value", value);
        } catch (JSONException ignored) {
        }

        return new Request.Builder()
                .url(baseUrl + "/mealplanner/" + username + "/items?apiKey=" + apiKey + "&hash=" + hash)
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();
    }
    
    /**
     * Get a user's meal plan for today
     */
    public Request buildGetMealPlanRequest(String username, String hash) {
        // Format today's date as YYYY-MM-DD
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String today = sdf.format(new Date());
        
        return new Request.Builder()
                .url(baseUrl + "/mealplanner/" + username + "/day/" + today + "?apiKey=" + apiKey + "&hash=" + hash)
                .get()
                .build();
    }
}