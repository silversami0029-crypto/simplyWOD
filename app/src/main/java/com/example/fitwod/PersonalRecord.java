package com.example.fitwod;

public class PersonalRecord {
    public String workoutType;
    public String metric;
    public String value;
    public String date;

    // Default constructor needed for database operations
    public PersonalRecord() {}

    // Parameterized constructor for convenience
    public PersonalRecord(String workoutType, String metric, String value, String date) {
        this.workoutType = workoutType;
        this.metric = metric;
        this.value = value;
        this.date = date;
    }
}