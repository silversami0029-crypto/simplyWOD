package com.example.fitwod;

import com.google.gson.annotations.SerializedName;

// FoodItem.java
public class FoodItem {
    @SerializedName("food_name")
    public String foodName;

    @SerializedName("nf_calories")
    public float calories;

    @SerializedName("nf_protein")
    public float protein;

    @SerializedName("nf_total_fat")
    public float fat;

    @SerializedName("nf_total_carbohydrate")
    public float carbs;

    public String getFoodName() {
        return foodName;
    }
    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public float getCalories() {
        return calories;
    }

    public void setCalories(float calories) {
        this.calories = calories;
    }

    public float getProtein() {
        return protein;
    }

    public void setProtein(float protein) {
        this.protein = protein;
    }

    public float getCarbs() {
        return carbs;
    }

    public void setCarbs(float carbs) {
        this.carbs = carbs;
    }

    public float getFat() {
        return fat;
    }

}

