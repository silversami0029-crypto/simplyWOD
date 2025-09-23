package com.bessadi.fitwod;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView; // Use ImageView for logos, not ImageButton
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView titleText = findViewById(R.id.titleText);
        TextView timerText = findViewById(R.id.timerText);
        ImageView logo = findViewById(R.id.ibLogo_splash); // Changed to ImageView

        // Show simplyWOD immediately
        titleText.setText("simplyWOD");
        titleText.setAlpha(0f);
        titleText.animate().alpha(1f).setDuration(500).start();

        // After 2 seconds, show Timer text with animation
        new Handler().postDelayed(() -> {
            timerText.setAlpha(0f);
            timerText.setVisibility(TextView.VISIBLE);
            timerText.animate().alpha(1f).setDuration(1000).start();

            // After the timer text animation completes (1 second), show the logo
            new Handler().postDelayed(() -> {
                logo.setAlpha(0f);
                logo.setVisibility(ImageView.VISIBLE); // Fixed this line
                logo.animate().alpha(1f).setDuration(1000).start();

                // After logo animation, launch main activity
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(SplashActivity.this, Menu_Activity.class);
                    startActivity(intent);
                    finish();
                }, 2000); // Wait 2 seconds after logo appears
            }, 1000); // Wait 1 second after timer text appears
        }, 2000); // Initial 2 second delay
    }
}