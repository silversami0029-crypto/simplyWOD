package com.example.fitwod;


import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bessadi.fitwod.R;

public class WorkoutFragment extends Fragment {

    private Spinner workoutTypeSpinner;
    private EditText durationEditText;
    private RatingBar intensityRatingBar;
    private EditText notesEditText;
    private Button startWorkoutButton;
    private Button saveWorkoutButton;
    private Chronometer workoutChronometer;

    private WorkoutDbHelper dbHelper;
    private boolean isWorkoutActive = false;
    private long startTime = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workout, container, false);

        dbHelper = new WorkoutDbHelper(requireContext());
        initializeViews(view);
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        workoutTypeSpinner = view.findViewById(R.id.workoutTypeSpinner);
        durationEditText = view.findViewById(R.id.durationEditText);
        intensityRatingBar = view.findViewById(R.id.intensityRatingBar);
        notesEditText = view.findViewById(R.id.notesEditText);
        startWorkoutButton = view.findViewById(R.id.startWorkoutButton);
        saveWorkoutButton = view.findViewById(R.id.saveWorkoutButton);
        workoutChronometer = view.findViewById(R.id.workoutChronometer);
    }

    private void setupListeners() {
        startWorkoutButton.setOnClickListener(v -> {
            if (!isWorkoutActive) {
                startWorkout();
            } else {
                stopWorkout();
            }
        });

       // saveWorkoutButton.setOnClickListener(v -> saveWorkout());
    }

    private void startWorkout() {
        String workoutType = workoutTypeSpinner.getSelectedItem().toString();
        if (workoutType.equals("Select Workout Type")) {
            Toast.makeText(getContext(), "Please select a workout type", Toast.LENGTH_SHORT).show();
            return;
        }

        isWorkoutActive = true;
        startTime = System.currentTimeMillis();

        // Update UI for active workout
        startWorkoutButton.setText("Stop Workout");
        saveWorkoutButton.setVisibility(View.VISIBLE);
        workoutChronometer.setVisibility(View.VISIBLE);
        workoutChronometer.setBase(SystemClock.elapsedRealtime());
        workoutChronometer.start();

        // Disable input fields during workout
        setInputsEnabled(false);
    }

    private void stopWorkout() {
        isWorkoutActive = false;
        workoutChronometer.stop();

        // Update UI
        startWorkoutButton.setText("Start Workout");
        setInputsEnabled(true);
    }

   /* private void saveWorkout() {
        String workoutType = workoutTypeSpinner.getSelectedItem().toString();
        String durationText = durationEditText.getText().toString();
        float intensity = intensityRatingBar.getRating();
        String notes = notesEditText.getText().toString();

        if (workoutType.isEmpty() || durationText.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration = Integer.parseInt(durationText);

        // Save workout to database
       // long result = dbHelper.addWorkout(workoutType, duration, intensity, notes, startTime);

        if (result != -1) {
            Toast.makeText(getContext(), "Workout saved successfully!", Toast.LENGTH_SHORT).show();
            resetForm();
        } else {
            Toast.makeText(getContext(), "Error saving workout", Toast.LENGTH_SHORT).show();
        }
    }*/

    private void resetForm() {
        workoutTypeSpinner.setSelection(0);
        durationEditText.setText("20");
        intensityRatingBar.setRating(3);
        notesEditText.setText("");
        saveWorkoutButton.setVisibility(View.GONE);
        workoutChronometer.setVisibility(View.GONE);
    }

    private void setInputsEnabled(boolean enabled) {
        workoutTypeSpinner.setEnabled(enabled);
        durationEditText.setEnabled(enabled);
        intensityRatingBar.setEnabled(enabled);
        notesEditText.setEnabled(enabled);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}