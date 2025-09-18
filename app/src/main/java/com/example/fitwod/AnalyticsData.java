package com.example.fitwod;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bessadi.fitwod.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


public class AnalyticsData {

    public int totalWorkouts;
    public long totalDuration;
    PieChart chartWorkoutTypes;
    LineChart chartProgress;
   // public Map<String, Integer> workoutTypeDistribution;
    public List<ProgressDataPoint> workoutProgress;
    public List<PersonalRecord> personalRecords;
    public  Map<String,Integer> workoutTypeDistribution;

    public Context context;
   // String workout_Types = context.getString(R.string.workout_types);

    public AnalyticsData() {
        // Initialize all collections
        workoutTypeDistribution = new HashMap<>();
        workoutProgress = new ArrayList<>();
        personalRecords = new List<PersonalRecord>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(@Nullable Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<PersonalRecord> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] a) {
                return null;
            }

            @Override
            public boolean add(PersonalRecord personalRecord) {
                return false;
            }

            @Override
            public boolean remove(@Nullable Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends PersonalRecord> c) {
                return false;
            }

            @Override
            public boolean addAll(int index, @NonNull Collection<? extends PersonalRecord> c) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public PersonalRecord get(int index) {
                return null;
            }

            @Override
            public PersonalRecord set(int index, PersonalRecord element) {
                return null;
            }

            @Override
            public void add(int index, PersonalRecord element) {

            }

            @Override
            public PersonalRecord remove(int index) {
                return null;
            }

            @Override
            public int indexOf(@Nullable Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(@Nullable Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<PersonalRecord> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<PersonalRecord> listIterator(int index) {
                return null;
            }

            @NonNull
            @Override
            public List<PersonalRecord> subList(int fromIndex, int toIndex) {
                return Collections.emptyList();
            }
        };
    }



    // private void setupWorkoutTypeChart(WorkoutTypeDistribution distribution) { /* ... */ }
   public void setupWorkoutTypeChart(List<ProgressDataPoint> progress) {
        //String workoutTypes = context.getString(R.string.workout_types);

       if (progress == null || progress.isEmpty()) {
           chartProgress.setNoDataText("No progress data available");
           chartWorkoutTypes.clear();
           chartWorkoutTypes.setNoDataText("No workout data available");
           return;
       }

       List<PieEntry> entries = new ArrayList<>();
       for (int i = 0; i < progress.size(); i++) {
           ProgressDataPoint point = progress.get(i);
           entries.add(new PieEntry(i,point.getValue()));
       }

       PieDataSet dataSet = new PieDataSet(entries, "Workout Types");
       dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
       dataSet.setValueTextSize(12f);

       PieData pieData = new PieData(dataSet);
       chartWorkoutTypes.setData(pieData);
       chartWorkoutTypes.getDescription().setEnabled(false);
       chartWorkoutTypes.setCenterText("Workout Types");
       chartWorkoutTypes.animateY(1000);
       chartWorkoutTypes.invalidate(); // refresh
   }
// added for achievements

    //private int totalWorkouts;
    private int amrapCount;
    private int tabataCount;
    private int emomCount;
    private int consecutiveDays;
    private int longestWorkoutDuration;
    private int daysSinceFirstWorkout;
    private boolean hasPersonalBest;

    // Getters and setters
    public int getTotalWorkouts() { return totalWorkouts; }
    public int getAmrapCount() { return amrapCount; }
    public int getTabataCount() { return tabataCount; }
    public int getEmomCount() { return emomCount; }
    public int getConsecutiveDays() { return consecutiveDays; }
    public int getLongestWorkoutDuration() { return longestWorkoutDuration; }
    public int getDaysSinceFirstWorkout() { return daysSinceFirstWorkout; }
    public boolean hasPersonalBest() { return hasPersonalBest; }


}