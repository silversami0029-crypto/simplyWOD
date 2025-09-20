package com.bessadi.fitwod;

import java.util.List;

public class FoodSearchResponse {
    private List<FoodItem> foods;

    // Getters and setters
    public List<FoodItem> getFoods() {
        return foods;
    }

    public void setFoods(List<FoodItem> foods) {
        this.foods = foods;
    }

    public static class FoodItem {
        private String fdcId;
        private String description;
        private String dataType;

        // Getters and setters
        public String getFdcId() { return fdcId; }
        public String getDescription() { return description; }
        public String getDataType() { return dataType; }
    }
}
