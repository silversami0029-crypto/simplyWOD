package com.example.fitwod;

import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bessadi.fitwod.R;

public class WorkoutHistoryActivity extends AppCompatActivity {
   /* private LineChart lineChart;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        lineChart = findViewById(R.id.chartWorkoutHistory);
        barChart = findViewById(R.id.chartWorkRestComparison);

        loadWorkoutHistoryData();
    }

    private void loadWorkoutHistoryData() {
        WorkoutDbHelper dbHelper = new WorkoutDbHelper(this);
        Cursor cursor = dbHelper.getAllWorkouts();

        if (cursor != null && cursor.moveToFirst()) {
            List<Entry> workTimeEntries = new ArrayList<>();
            List<Entry> restTimeEntries = new ArrayList<>();
            List<BarEntry> workRestEntries = new ArrayList<>();
            List<String> dates = new ArrayList<>();

            int index = 0;
            do {
                // Get data from cursor
                int workTime = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TIME));
                int restTime = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_REST_TIME));
                long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT));

                // Add to chart data
                workTimeEntries.add(new Entry(index, workTime));
                restTimeEntries.add(new Entry(index, restTime));
                workRestEntries.add(new BarEntry(index, new float[]{workTime, restTime}));

                // Format date for labels
                dates.add(new SimpleDateFormat("MM/dd", Locale.getDefault()).format(new Date(createdAt)));

                index++;
            } while (cursor.moveToNext());

            setupLineChart(workTimeEntries, restTimeEntries, dates);
            setupBarChart(workRestEntries, dates);

            cursor.close();
        }
        dbHelper.close();
    }

    private void setupLineChart(List<Entry> workTimes, List<Entry> restTimes, List<String> dates) {
        // Create data sets
        LineDataSet workDataSet = new LineDataSet(workTimes, "Work Time (sec)");
        workDataSet.setColor(Color.BLUE);
        workDataSet.setCircleColor(Color.BLUE);

        LineDataSet restDataSet = new LineDataSet(restTimes, "Rest Time (sec)");
        restDataSet.setColor(Color.RED);
        restDataSet.setCircleColor(Color.RED);

        // Combine data sets
        LineData lineData = new LineData(workDataSet, restDataSet);

        // Configure chart
        lineChart.setData(lineData);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        lineChart.getDescription().setText("Workout Progress Over Time");
        lineChart.animateY(1000);
        lineChart.invalidate(); // Refresh
    }

    private void setupBarChart(List<BarEntry> entries, List<String> dates) {
        BarDataSet dataSet = new BarDataSet(entries, "Work/Rest Comparison");
        dataSet.setColors(new int[]{Color.BLUE, Color.RED});
        dataSet.setStackLabels(new String[]{"Work", "Rest"});

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
        barChart.getDescription().setText("Work vs Rest Time");
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private class LineChart {
    }*/
}