package com.bessadi.fitwod;

import com.google.gson.annotations.SerializedName;

public class BarcodeNutritionResponse {
    @SerializedName("product")
    private Product product;

    public Product getProduct() {
        return product;
    }

    public static class Product {
        @SerializedName("nutriments")
        private Nutriments nutriments;

        public Nutriments getNutriments() {
            return nutriments;
        }
    }

    public static class Nutriments {
        @SerializedName("energy-kcal_100g")
        private float calories;

        @SerializedName("proteins_100g")
        private float protein;

        @SerializedName("carbohydrates_100g")
        private float carbs;

        @SerializedName("fat_100g")
        private float fat;

        // Getters
        public float getCalories() { return calories; }
        public float getProtein() { return protein; }
        public float getCarbs() { return carbs; }
        public float getFat() { return fat; }
    }
}
