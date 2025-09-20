package com.bessadi.fitwod;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bessadi.fitwod.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// Other MPAndroidChart imports...

public class PerformanceAnalyticsActivity extends AppCompatActivity {
    // ... existing view declarations
    WorkoutDbHelper dbHelper = new WorkoutDbHelper(PerformanceAnalyticsActivity.this);
    private AsyncTask<Void, Void, AnalyticsData> analyticsTask; // Add this

    private RecyclerView achievementsRecyclerView;

    RecordsAdapter  recordsAdapter;
    TextView tvTotalDuration,  tvTotalWorkouts;
    ImageButton btnBack;
    PieChart chartWorkoutTypes;
    LineChart chartProgress;
    RecyclerView rvRecords;
    AnalyticsData analyticsdata;
    public Context context;
   // String workout_Types = context.getString(R.string.workout_types);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // --- CORRECTED CODE FOR EDGE-TO-EDGE LAYOUT ---
        // 1. Request to draw behind the system bars
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // 2. Handle the insets for the root view of your activity
        View rootView = findViewById(android.R.id.content);
        View mainContentView = ((ViewGroup) rootView).getChildAt(0); // Gets the first child of the root (usually your layout)

        if (mainContentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContentView, (v, insets) -> {
                // Get the insets for the system bars (status bar + navigation bar)
                // 'insets' is already a WindowInsetsCompat object!
                int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

                // Apply the top inset as padding to push the content down below the status bar
                v.setPadding(v.getPaddingLeft(),
                        systemBarsTop, // This is the crucial padding for the top
                        v.getPaddingRight(),
                        systemBarsBottom); // And for the bottom nav bar if needed

                // Return the insets, consuming the parts we've used for padding
                return insets;
            });
        }
        // --- END OF CORRECTED CODE ---

        setContentView(R.layout.activity_performance_analytics);


        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView);

        String timeperiod ="";
        recordsAdapter = new RecordsAdapter();
        // Initialize views
        chartWorkoutTypes = findViewById(R.id.chartWorkoutTypes);
        chartProgress = findViewById(R.id.chartProgress);
        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        rvRecords = findViewById(R.id.rvRecords);
        btnBack = findViewById(R.id.ib_back_per);

        //Compatibility setup check
        setupChartsCompatibility();
        rvRecords.setAdapter(recordsAdapter);
        rvRecords.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goback_to_mainMenu();
            }
        });

        loadAnalyticsData(timeperiod);
    }

    private void setupSamsungCompatibility() {
        if (Build.MANUFACTURER.equalsIgnoreCase("samsung") &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {

            Log.d("Device", "Applying Samsung S9 workarounds");

            // Samsung-specific rendering fixes
            chartWorkoutTypes.getRenderer().getPaintRender().setAntiAlias(false);
            chartProgress.getRenderer().getPaintRender().setAntiAlias(false);

            // Reduce chart quality for compatibility
            chartWorkoutTypes.setMinOffset(0f);
            chartWorkoutTypes.setDrawEntryLabels(false);
            chartProgress.setDrawGridBackground(false);
        }
    }

    private void setupChartsCompatibility() {

        // Check if device is known to have issues (e.g., S9)
       // Boolean isProblematicDevice = Build.MODEL.startsWith("SM-G96"); // S9 series
        if (chartWorkoutTypes == null) {
            Log.e("Charts", "PieChart is null in compatibility setup!");
            return;
        }
        if (chartProgress == null) {
            Log.e("Charts", "LineChart is null in compatibility setup!");
            return;
        }

        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // Apply software rendering
            chartWorkoutTypes.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            chartProgress.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            // Simplify charts
            chartWorkoutTypes.setDrawEntryLabels(false);
            chartWorkoutTypes.setRotationEnabled(false);
            chartProgress.setRenderer(new LineChartRenderer(chartProgress, chartProgress.getAnimator(), chartProgress.getViewPortHandler()));
        }
    }
    private void goback_to_mainMenu() {
        startActivity(new Intent(PerformanceAnalyticsActivity.this, Menu_Activity.class));
    }
    private void setupWorkoutTypeChart(Map<String, Integer> distribution) {
        setupSamsungCompatibility();

        // Get formatted string from resources
        String workoutTypes = getString(R.string.workout_types);
        String noWorkout = getString(R.string.No_workout_data);


        chartWorkoutTypes.setEntryLabelColor(Color.BLACK);
        chartWorkoutTypes.setEntryLabelTextSize(12f);
        chartWorkoutTypes.setDrawEntryLabels(true);

        Legend legend = chartWorkoutTypes.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        if (distribution == null || distribution.isEmpty()) {
            chartWorkoutTypes.setNoDataText(noWorkout);
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, null); // Set label to null
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new PercentFormatter(chartWorkoutTypes));
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        chartWorkoutTypes.setData(pieData);
        chartWorkoutTypes.getDescription().setEnabled(false);

        // Set center text with HTML formatting
        chartWorkoutTypes.setCenterText(Html.fromHtml(workoutTypes));
        chartWorkoutTypes.setCenterTextSize(16f);
        chartWorkoutTypes.setCenterTextColor(Color.RED); // Ensure text is red
        chartWorkoutTypes.animateY(1000);
        chartWorkoutTypes.invalidate();
    }
    /*
    private void setupWorkoutTypeChart(Map<String, Integer> distribution) {
        // Get formatted string from resources
        String workoutTypes = getString(R.string.workout_types);
        setupSamsungCompatibility();
        // In setupWorkoutTypeChart() improve appearce
        chartWorkoutTypes.setEntryLabelColor(Color.BLACK);
        chartWorkoutTypes.setEntryLabelTextSize(12f);
        chartWorkoutTypes.setDrawEntryLabels(true); // Show labels on slices

        //add legend
        Legend legend = chartWorkoutTypes.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        if (distribution == null || distribution.isEmpty()) {
           // chartProgress.setNoDataText("No progress data available");
            // chartWorkoutTypes.clear();
             chartWorkoutTypes.setNoDataText("No workout data available");
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {

            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
       // PieDataSet dataSet = new PieDataSet(entries, workout_Types);
        PieDataSet dataSet = new PieDataSet(entries, null); // Set label to null
        //PieDataSet dataSet = new PieDataSet(entries, "Workout Types");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);
        // add percentage values
         dataSet.setValueFormatter(new PercentFormatter(chartWorkoutTypes));
         dataSet.setValueTextSize(14f);
         dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        chartWorkoutTypes.setData(pieData);
        chartWorkoutTypes.getDescription().setEnabled(false);
        //  read from string.xml - workout_Types'
        //chartWorkoutTypes.setCenterText(workout_Types);
        // Set center text with HTML formatting
        chartWorkoutTypes.setCenterText(Html.fromHtml(workoutTypes));
        //chartWorkoutTypes.setCenterText("Workout Types");
        chartWorkoutTypes.animateY(1000);
        chartWorkoutTypes.invalidate(); // refresh
    }*/
    private void updateStatistics(AnalyticsData data) {
        // Assuming data has totalWorkouts and totalDuration (in seconds or minutes?)
        tvTotalWorkouts.setText(String.valueOf(data.totalWorkouts));
        // Format duration: if in seconds, convert to minutes or HH:MM:SS
        long totalSeconds = data.totalDuration;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        String durationText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        tvTotalDuration.setText(durationText);
    }

    private void loadAnalyticsData(String period) {
        // Cancel previous task if running
        if (analyticsTask != null && analyticsTask.getStatus() != AsyncTask.Status.FINISHED) {
            analyticsTask.cancel(true);
        }
        new AsyncTask<Void, Void, AnalyticsData>() {

            @Override
            protected AnalyticsData doInBackground(Void... voids) {
                try {
                    return dbHelper.getAnalyticsData(period);
                } catch (Exception e) {
                    Log.e("Analytics", "Error loading data", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(AnalyticsData data) {
                if (data != null && !isFinishing()) {
                    setupWorkoutTypeChart(data.workoutTypeDistribution);
                    setupProgressChart(data.workoutProgress);
                    updateStatistics(data);
                    if (recordsAdapter != null){
                    recordsAdapter.setRecords(data.personalRecords);
                    }else {
                        Log.e("Analytics", "RecordAdapteris null!");
                    }
                }
            }
        }.execute();

    }


   /* private void checkAchievements(AnalyticsData data) {
        List<Achievement> unlockedAchievements = new ArrayList<>();

        // Example achievements:
        if (data.totalWorkouts >= 10) {
            unlockedAchievements.add(new Achievement("First 10", "Complete 10 workouts", R.drawable.badge_10));
        }
        if (data.totalDuration >= 3600) { // 1 hour
            unlockedAchievements.add(new Achievement("Hour of Power", "Total 1 hour of workouts", R.drawable.badge_1hour));
        }
        // ... more achievements

        // Display the achievements (e.g., in a RecyclerView)
        if (!unlockedAchievements.isEmpty()) {
            AchievementAdapter adapter = new AchievementAdapter(unlockedAchievements);
            RecyclerView rvAchievements = findViewById(R.id.achievementsRecyclerView);
            rvAchievements.setAdapter(adapter);
            rvAchievements.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        }
    }*/

    /*private void displayAchievements() {
        AchievementManager achievementManager = new AchievementManager(this);
        List<Achievement> achievements = achievementManager.checkAchievements();
        Log.d("Achievements", "Number of achievements: " + achievements.size());
       TextView achievementsTitle = findViewById(R.id.achievementsTitle);
        RecyclerView achievementsView = findViewById(R.id.achievementsRecyclerView);

        // Log the achievements for debugging
        Log.d("Achievements", "Number of achievements: " + achievements.size());
        for (Achievement achievement : achievements) {
            Log.d("Achievements", "Achievement: " + achievement.getTitle());
        }

        if (achievements.isEmpty()) {
            Log.d("Achievements", "No achievements to display. Hiding section.");
            achievementsTitle.setVisibility(View.GONE);
            achievementsView.setVisibility(View.GONE);
        } else {
            Log.d("Achievements", "Displaying achievements. Count: " + achievements.size());
            achievementsTitle.setVisibility(View.VISIBLE);
            achievementsView.setVisibility(View.VISIBLE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(
                    this, LinearLayoutManager.HORIZONTAL, false);
            achievementsView.setLayoutManager(layoutManager);

            AchievementAdapter adapter = new AchievementAdapter(achievements);
            achievementsView.setAdapter(adapter);
        }
    }*/
    private void setupProgressChart(List<ProgressDataPoint> progress) {

        if (progress == null || progress.isEmpty()) {
            chartProgress.clear();
            chartProgress.setNoDataText("No progress data available");
            return;
        }

        List<com.github.mikephil.charting.data.Entry> entries = new ArrayList<>();
        for (int i = 0; i < progress.size(); i++) {
            ProgressDataPoint point = progress.get(i);
            // Assuming entry has a date (as String) and a value (float). We use index as x, but we can use date if we convert to timestamp.
            // Alternatively, we can use the index and then set the x-axis labels to the dates.
            entries.add(new com.github.mikephil.charting.data.Entry(i, point.getValue()));
            // Add marker
            //chartProgress.setMarker(new YourMarkerView(this, R.layout.custom_marker, dates));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Progress Over Time");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextSize(10f);

        // Enhance ProgressChart
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleRadius(5f);
        dataSet.setFillAlpha(65);
        dataSet.setFillColor(Color.BLUE);

        //Add labels
        // In setupProgressChart()
        chartProgress.setDrawMarkers(true);
       // chartProgress.setMarker(new YourMarkerView(this, R.layout.custom_marker));

        LineData lineData = new LineData(dataSet);
        chartProgress.setData(lineData);

        // Customize x-axis to show dates (if you have a list of dates for each entry)
        // We assume the progress list is ordered by date and the same size as the entries.
        String[] dates = new String[progress.size()];
        for (int i = 0; i < progress.size(); i++) {
            dates[i] = progress.get(i).getDate(); // format as needed (e.g., short date)
            // Add marker

            chartProgress.setMarker(new YourMarkerView(this,R.layout.custom_marker,dates));
            chartWorkoutTypes.animateY(1500, Easing.EaseInOutQuad);
            chartProgress.animateXY(2000, 2000);

        }

        XAxis xAxis = chartProgress.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(progress.size());

        chartProgress.getDescription().setEnabled(false);
        chartProgress.animateX(1000);
        chartProgress.invalidate();
    }

    // ... rest of your chart setup methods (setupWorkoutTypeChart, setupProgressChart, etc.)
    // ... updateStatistics method
    // ... onDestroy method



    protected void onDestroy() {
        // Cancel async task
        if (analyticsTask != null && !analyticsTask.isCancelled()) {
            analyticsTask.cancel(true);
        }

        // Close database
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }


}