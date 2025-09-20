package com.example.fitwod;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bessadi.fitwod.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class pbfortime extends AppCompatActivity {

    //AdView mAdView;
    ImageButton addBtn, resetBtn, playBtn, goback,recordVoice;
    TextView timeLeftTv, roundCounter;
    CountDownTimer timeCountDown = null, pre_timeCountDown = null, timeCountDown_ft = null;
    private VoiceRecorder voiceRecorder;
    private WorkoutAdapter adapter;
    private MediaRecorder mediaRecorder;

    int timeSelected = 0, roundSelected = 0, timeProgress = 0, minutes = 0, seconds = 0 , maxtime = 0;
    private int cycleCount = 0;
    int counter = 0;
    private String voiceNote = null;
    private boolean isRecording = false;
    long pauseOffSet = 0, timeLeftInMillis;
    boolean isStart = true, timerRunning, preTimeRunning, isTimerRunning = true, isVisible = true;
    MediaPlayer mplayer, wellDonePlayer, restplayer, singlebeepplayer, lastround;
    private String voiceNotePath = null;
    private ConstraintLayout mainLayout_fortime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pbfortime);
        timeLeftInMillis = roundSelected;
        /*this handles back button event*/
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true){
            @Override
            public void handleOnBackPressed(){
                if (timeCountDown != null || pre_timeCountDown != null) { // ensure timers are running
                    showExitDialog(); // display dialog
                }
                else {goback_to_mainMenu();} // goes back to main menu

            }
        });
        /*adv request detail*/
        //MobileAds.initialize(this);
       // mAdView = (AdView) findViewById(R.id.adView);
        //AdRequest adRequest = new AdRequest.Builder().build();
       // mAdView.loadAd(adRequest);

        addBtn = findViewById(R.id.btnAdd_ft);
        resetBtn =  findViewById(R.id.ib_reset_ft);
        goback =  findViewById(R.id.ib_back_ft);
        playBtn =  findViewById(R.id.ibPlay_ft);
        roundCounter = findViewById(R.id.tvcounterButton_ft);
        //Audio variables
        mplayer = MediaPlayer.create(pbfortime.this,R.raw.let_go);
        wellDonePlayer = MediaPlayer.create(pbfortime.this,R.raw.welldone);
        singlebeepplayer = MediaPlayer.create(pbfortime.this, R.raw.singlebeep1);
        lastround = MediaPlayer.create(pbfortime.this, R.raw.lastround);
        recordVoice = findViewById(R.id.btnRecordVoice_fortime);
        //background image
        mainLayout_fortime = findViewById(R.id.main_layout_fortime);


        recordVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isRecording) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });

        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeCountDown != null || pre_timeCountDown != null) { // ensure timers are running
                    showExitDialog(); // display dialog
                }
                else {goback_to_mainMenu();} // goes back to main menu
            }
        });


        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInput();
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTime();
            }
        });
        roundCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeCountDown_ft != null ){
                counter ++;
                roundCounter.setText(String.valueOf(counter));}
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimeFunction();
            }
        });
        loadSavedBackground();
    }

    private void setBackgroundImage(Uri imageUri) {
        try {
            Glide.with(this)
                    .load(imageUri)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mainLayout_fortime.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } catch (Exception e) {
            Log.e("MenuBackground", "Error loading background: " + e.getMessage());
            setDefaultBackground(R.drawable.default_background);
        }
    }

    protected void onResume() {
        super.onResume();
        loadSavedBackground();
    }

    private void loadSavedBackground() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);

        // Check if default background is set
        boolean isDefaultBackground = prefs.getBoolean("is_default_background", false);

        if (isDefaultBackground) {
            // Load default background from drawable
            int defaultBackgroundId = prefs.getInt("default_background_id", R.drawable.default_background);
            setDefaultBackground(defaultBackgroundId);
        } else {
            // Load custom background from gallery
            String savedUri = prefs.getString("background_uri", null);
            if (savedUri != null && !savedUri.isEmpty()) {
                setBackgroundImage(Uri.parse(savedUri));
            } else {
                // Fallback to original default background
                setDefaultBackground(R.drawable.default_background);
            }
        }
    }

    private void setDefaultBackground(int drawableResourceId) {
        mainLayout_fortime.setBackgroundResource(drawableResourceId);
    }

    private void setTimeFunction() {
        Dialog timeDialog = new Dialog(this);
        timeDialog.setContentView(R.layout.add_fortime_dialog);
        EditText roundSet = timeDialog.findViewById(R.id.etGetRounds_ft);
        TextView timeLeftTv = findViewById(R.id.tvTimeLeft_ft);
        TextView roundDisplay = findViewById(R.id.tvrounds_ft);
        ProgressBar progressBar_ft = findViewById(R.id.pbTimer_ft);

        timeDialog.findViewById(R.id.btnGo_ft).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( roundSet.getText().toString().isEmpty()) {
                   // Toast.makeText(pbfortime.this, "Please enter For Time duration", Toast.LENGTH_SHORT).show();
                    alertdialog("FOR TIME","Please enter For Time duration");
                } else {
                    roundDisplay.setText("Time cap: "+ roundSet.getText().toString() + " minutes");
                    roundSelected = Integer.parseInt(roundSet.getText().toString());
                    updateTimerText(); // display mm:ss format
                    maxtime = roundSelected * 60;
                    progressBar_ft.setMax(maxtime);
                    //saveToDb();

                }
                timeDialog.dismiss();
            }
        });
        timeDialog.show();
    }

    private void goback_to_mainMenu() {

        startActivity(new Intent(pbfortime.this, Menu_Activity.class));
    }
    private void resetTime() {
        if (timeCountDown_ft != null) {
            timeCountDown_ft.cancel();
            roundSelected = 0;
            timeSelected = 0;
            pauseOffSet = 0;
            timeProgress = 0;
            timeCountDown_ft = null;
            isTimerRunning = false;
            minutes = 0;
            seconds = 0;
            playBtnEnabled();
            ProgressBar progressBar_ft = findViewById(R.id.pbTimer_ft);
            progressBar_ft.setProgress(0);
            progressBar_ft.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.purple_progressbar_background));
            TextView timeLeftTv = findViewById(R.id.tvTimeLeft_ft);
            timeLeftTv.setText("00:00");
            TextView roundDisplay = findViewById(R.id.tvrounds_ft);
            counter = 0;
            roundCounter.setText(String.valueOf(counter));
            roundDisplay.setText("");
        }
    }
    /*play lets go sound function */
    public void playWellDone()
    {
        wellDonePlayer.start();
    }
    public void playLetsGo()
    {
        mplayer.start();
    }

    public void startTimer(long timeLeftInMillis) {
        playBtnDisabled();
      //  if (timeSelected > timeProgress) {
            playLetsGo(); // play lets go sound
            ProgressBar progressBar_ft = findViewById(R.id.pbTimer_ft);
            progressBar_ft.setProgress(timeProgress);
            progressBar_ft.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.purple_progressbar_background));
            timeSelected = roundSelected * 60; // convert to seconds
            timeCountDown_ft = new CountDownTimer((timeSelected * 1000), 1000) {
                @Override
                public void onTick(long p0) {
                    timeProgress++;
                    progressBar_ft.setProgress(timeSelected  - timeProgress); // already converted to seconds timeSelected = roundSelected * 60;
                    TextView timeLeftTv = findViewById(R.id.tvTimeLeft_ft);
                    TextView roundDisplay = findViewById(R.id.tvrounds_ft);
                    updateTimerText();// update time format mm:ss
                    if ((timeSelected - timeProgress) == 60) { // play last round sound for last round (60 sec)
                        playLastRound();
                    }
                }
                @Override
                public void onFinish() {
                    final int totalCycles = roundSelected;
                    TextView roundDisplay = findViewById(R.id.tvrounds_ft);
                    playWellDone();
                    alertdialog("FOR TIME","Time's up");// display dialog to a user
                    saveToDb();
                    resetTime();
                }
            }.start();
            timerRunning = true;
    }
    private void updateTimerText() {
        long timeLeftInMillis = roundSelected * 60 * 1000 - timeProgress * 1000;
        if (timeLeftInMillis >= 0) {
            TextView timeLeftTv = findViewById(R.id.tvTimeLeft_ft);
            int minutes = (int) (timeLeftInMillis / 1000) / 60;
            int seconds = (int) (timeLeftInMillis / 1000) % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            timeLeftTv.setText(timeFormatted);
        } else {
            timeCountDown.cancel();

            resetTime();
        }
    }
    public void preStartTimer() {
        playBtnDisabled();
        Toast.makeText(pbfortime.this, "Workout starts in 10 seconds ", Toast.LENGTH_SHORT).show();
        preTimeRunning = true;
        pre_timeCountDown = new CountDownTimer(10000, 1000) { //10 seconds countdown
        int timeProgress = 0;
            @Override
            public void onTick(long p0) {

                timeProgress++;
                long timeLeftInMillis = 10 * 1000 - timeProgress * 1000;
                TextView timeLeftTv = findViewById(R.id.tvTimeLeft_ft);
                //int minutes = (int) (timeLeftInMillis / 1000) / 60;
                int seconds = (int) (timeLeftInMillis / 1000) % 60;
                //String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                String timeFormatted = String.format( "%02d",seconds);
                timeLeftTv.setText(timeFormatted);
                if (seconds == 2 || seconds == 1 || seconds == 0) { // play single beep last 3 seconds
                    playsinglebeep();
                }
            }
            @Override
            public void onFinish () {
                timeProgress = 0; // reset to 0
                preTimeRunning = false;
                long timeinMillis = 0;
                startTimer(timeinMillis);
            }
        }.start();
    }

    private void stopRecording() {
        if (isRecording && mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;

            recordVoice.setImageResource(R.drawable.ic_mic); // Change back to mic icon
            Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show();
        }
    }
    private void startRecording() {
        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
            return;
        }

        try {
            // Create a unique file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "EMOM_VOICE_" + timeStamp + ".3gp";

            // Get directory for recordings
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }

            voiceNotePath = new File(storageDir, fileName).getAbsolutePath();

            // Set up media recorder
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(voiceNotePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            // recordVoice.setImageResource(R.drawable.ic_stop); // Change to stop icon
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("VOICE_RECORD", "Failed to start recording: " + e.getMessage());
            Toast.makeText(this, "Recording failed to start", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkInput() {
        if (roundSelected > timeProgress) {
            preStartTimer();
        } else {
           // Toast.makeText(this, "Please add time duration", Toast.LENGTH_SHORT).show();
            alertdialog("FOR TIME","Please add FOR TIME duration");
        }

    }

    private void alertdialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(getResources().getDrawable(R.drawable.logo));
        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Apply theme fixes
        applyDialogThemeFix(dialog);
    }


    private boolean isDarkModeEnabled() {
        int nightMode = AppCompatDelegate.getDefaultNightMode();

        if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            return true;
        } else if (nightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            return false;
        } else {
            // MODE_NIGHT_FOLLOW_SYSTEM or MODE_NIGHT_AUTO_BATTERY
            int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        }
    }
    private void applyDialogThemeFix(AlertDialog dialog) {
        if (!isDarkModeEnabled()) {
            // Light theme - force black text
            int blackColor = Color.BLACK;

            TextView messageText = dialog.findViewById(android.R.id.message);
            if (messageText != null) messageText.setTextColor(blackColor);

            TextView titleText = dialog.findViewById(android.R.id.title);
            if (titleText != null) titleText.setTextColor(blackColor);

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveButton != null) positiveButton.setTextColor(blackColor);

            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (negativeButton != null) negativeButton.setTextColor(blackColor);

            Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            if (neutralButton != null) neutralButton.setTextColor(blackColor);
        }
    }
    private void saveToDb() {
        WorkoutDbHelper dbHelper = new WorkoutDbHelper(pbfortime.this);

        try {
            long id = dbHelper.insertWorkout(
                    "FOR TIME",
                    timeSelected,
                    counter,
                    roundSelected,
                    voiceNotePath,  // Use the recorded voice path
                    ""  // Empty video note
            );

            // dbId = id;

            if (id != -1) {
                Log.d("DB_DEBUG", "Saved successfully with ID: " + id);
                Toast.makeText(pbfortime.this, "Saved successfully with ID: " + id, Toast.LENGTH_SHORT).show();

                // Verify the save by reading back
                Cursor verificationCursor = dbHelper.getLatestWorkout();
                if (verificationCursor != null && verificationCursor.moveToFirst()) {
                    String savedVoicePath = verificationCursor.getString(
                            verificationCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VOICE_NOTE)
                    );
                    Log.d("DB_DEBUG", "Verified voice path: " + savedVoicePath);
                }
                if (verificationCursor != null) verificationCursor.close();
            } else {
                Log.e("DB_DEBUG", "Save failed - returned ID: " + id);
            }
        } finally {
            dbHelper.close();
            voiceNotePath = null; // Reset after saving
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording(); // Start recording if permission granted
            } else {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        // Clean up media recorder if activity is destroyed while recording
        if (mediaRecorder != null) {
            if (isRecording) {
                mediaRecorder.stop();
            }
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }
    private void showExitDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("Do you really want to exit?")
                .setIcon(R.drawable.logo)
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    // Action when OK is clicked
                    finishActivity(0);
                    System.exit(0);
                })
                .setNegativeButton("No", (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create(); // Use create() instead of show() to get the dialog object

        dialog.show();

        // Apply theme fixes after showing the dialog
        if (!isDarkModeEnabled()) {
            applyDialogThemeFix(dialog);
        }
    }
    private void playBtnDisabled() {
        playBtn.setEnabled(false);
        playBtn.setVisibility(View.VISIBLE);
    }
    private void playBtnEnabled() {
        playBtn.setEnabled(true);
        playBtn.setVisibility(View.VISIBLE);
    }
        public void playsinglebeep() {
        singlebeepplayer.start();
    }
    public void playLastRound()
    {
        lastround.start();
    }
    private void timePause() {
        if (timeCountDown != null) {
            timeCountDown.cancel();

        }
    }
}


