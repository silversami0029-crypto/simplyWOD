package com.bessadi.fitwod;

import com.google.gson.annotations.SerializedName;

// FoodNutrient.java
public class FoodNutrient {
    @SerializedName("nutrientId")
    private int nutrientId;

    @SerializedName("nutrientName")
    private String name;

    @SerializedName("value")
    private float value;

    // Getters and setters
}

