package com.example.fitwod;

public class ProgressDataPoint {
    private String date;
    private float value;

    public ProgressDataPoint(String date, float value) {
        this.date = date;
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public float getValue() {
        return value;
    }
}