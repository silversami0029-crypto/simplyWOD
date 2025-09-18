package com.example.fitwod;

import static com.example.fitwod.WorkoutDbHelper.COLUMN_REST_TIME;
import static com.example.fitwod.WorkoutDbHelper.COLUMN_ROUNDS;
import static com.example.fitwod.WorkoutDbHelper.COLUMN_WORK_TIME;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bessadi.fitwod.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class workout_history_list extends AppCompatActivity {
    private WorkoutDbHelper dbHelper;
    private WorkoutAdapter adapter;
    private RecyclerView recyclerView;
    ImageButton goback;
    private Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_workout_history_list);
        dbHelper = new WorkoutDbHelper(this);


        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Adapter
        adapter = new WorkoutAdapter(this,cursor);
        recyclerView.setAdapter(adapter);

        // Initialize DB Helper
        dbHelper = new WorkoutDbHelper(this);

        goback = findViewById(R.id.ib_back_db);
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goback_to_mainMenu();
            }
        });
        //loadAllWorkouts();
       loadLastWorkout();
        Toast.makeText(workout_history_list.this, "you reached workout hist list" , Toast.LENGTH_SHORT).show();

    }
    private void goback_to_mainMenu() {
        startActivity(new Intent(workout_history_list.this, Menu_Activity.class));
    }


    private void loadLastWorkout() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                return db.query(
                        dbHelper.TABLE_WORKOUTS,
                        null,
                        null, null, null, null,
                        dbHelper.COLUMN_CREATED_AT + " DESC",
                        "1"
                );
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        long date = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT));
                        Log.d("LOAD", "Raw timestamp: " + date);

                        updateUI(
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORK_TIME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REST_TIME)),
                                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ROUNDS)),
                                date
                        );
                    } else {
                        showEmptyState();
                    }
                } finally {
                    if (cursor != null) cursor.close();
                }
            }
        }.execute();
    }


    private void loadAllWorkouts() {
        dbHelper = new WorkoutDbHelper(this);
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {
                // Temporary debug call
              //  dbHelper.debugGetAllWorkouts();

                return dbHelper.getAllWorkouts();
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                CursorAdapter adapter = null;
                if (cursor != null && adapter != null) {
                    adapter.swapCursor(cursor);

                    if (cursor.getCount() == 0) {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                }
            }
        }.execute();
    }



    private void showEmptyState() {
        runOnUiThread(() -> {
            TextView params = findViewById(R.id.tvWorkoutParams);
            TextView dateView = findViewById(R.id.tvWorkoutDate);

            if (params != null) {
                params.setText("No workouts found");
                params.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            }

            if (dateView != null) {
                dateView.setText("");
            }


        });
    }

    private void updateUI( int work, int rest, int rounds, long dateMillis) {
        runOnUiThread(() -> {
            TextView params = findViewById(R.id.tvWorkoutParams);
            TextView dateView = findViewById(R.id.tvWorkoutDate);

            params.setText(String.format("Work: %ds\nRest: %ds\nRounds: %d", work, rest, rounds));

            if (dateMillis > 0) { // Only format if valid date
                dateView.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(new Date(dateMillis)));
            } else {
                dateView.setText("Date not available");
            }
        });
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }


}