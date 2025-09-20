package com.bessadi.fitwod;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bessadi.fitwod.R;

public class AnalyticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Example analytics content
        TextView analyticsText = findViewById(R.id.tv_analytics);
        analyticsText.setText("Welcome to Premium Analytics!\n\nYour workout statistics:\n- Total workouts: 25\n- Calories burned: 12,500\n- Average duration: 45min");
    }
}
