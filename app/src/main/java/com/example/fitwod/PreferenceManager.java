package com.example.fitwod;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private SharedPreferences prefs;

    public PreferenceManager(Context context) {
        this.prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    public boolean isPremiumPurchased() {
        return prefs.getBoolean("is_premium", false);
    }

    public boolean isAnalyticsPurchased() {
        return prefs.getBoolean("is_analytics", false);
    }

    public void setPremiumPurchased(boolean value) {
        prefs.edit().putBoolean("is_premium", value).apply();
    }

    public void setAnalyticsPurchased(boolean value) {
        prefs.edit().putBoolean("is_analytics", value).apply();
    }

    public void setCustomBackground(String imageUri) {
        prefs.edit().putString("custom_background", imageUri).apply();
    }

    public String getCustomBackground() {
        return prefs.getString("custom_background", null);
    }

    public boolean hasCustomBackground() {
        return prefs.contains("custom_background");
    }

}
