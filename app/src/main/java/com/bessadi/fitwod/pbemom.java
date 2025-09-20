package com.bessadi.fitwod;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class pbemom extends AppCompatActivity {

    AdView mAdView;
    private WorkoutAdapter adapter;
    private VoiceRecorder voiceRecorder;
    private MediaRecorder mediaRecorder;

    private String voiceNote = null;
    ImageButton addBtn, resetBtn, playBtn, pauseBtn, goback, recordVoice;
    private CountDownTimer timeCountDown = null, pre_timeCountDown = null;
    private boolean isRecording = false;
    int timeSelected = 0;
    int roundSelected = 0;
    int timeProgress = 0, minutes = 0, seconds = 0;
    private int cycleCount = 0, totalCycles = 0;
    long pauseOffSet = 0;
    boolean timerRunning, preTimeRunning, isPaused = false;
    long preTimeLeft =11000, millisUntilFinished = 0;
    long timeLeftInMillis;
    MediaPlayer mplayer, wellDonePlayer, restplayer, singlebeepplayer, tensec;
    private String voiceNotePath = null;
    private ConstraintLayout mainLayout_emom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pbemom);
        voiceRecorder = new VoiceRecorder(this);

        //  Initialise timer values
        timerRunning = false;
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
        /*adv request detail -- BEGIN*/
        //MobileAds.initialize(this);
      //  mAdView = findViewById(R.id.adView);
       // AdRequest adRequest = new AdRequest.Builder().build();
      //  mAdView.loadAd(adRequest);
        /*adv request detail --END*/

        addBtn = findViewById(R.id.btnAdd);
        resetBtn = findViewById(R.id.ib_reset);
        goback = findViewById(R.id.ib_back_emom);
        playBtn = findViewById(R.id.ibPlay);
        pauseBtn = findViewById(R.id.ibPause);
        TextView addTimeTv = findViewById(R.id.tvAddTime);
        TextView timeLeftTv = findViewById(R.id.tvTimeLeft);
        TextView resumeTv = findViewById(R.id.tvResume);
        mplayer = MediaPlayer.create(pbemom.this, R.raw.let_go);
        wellDonePlayer = MediaPlayer.create(pbemom.this, R.raw.welldone);
        restplayer = MediaPlayer.create(pbemom.this, R.raw.rest);
        singlebeepplayer = MediaPlayer.create(pbemom.this, R.raw.singlebeep1);
        tensec = MediaPlayer.create(pbemom.this, R.raw.tensec);
        recordVoice = findViewById(R.id.btnRecordVoice_emom);
        //background image
        mainLayout_emom = findViewById(R.id.main_layout_emom);


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
        resumeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //timeLeftInMillis = timeSelected * 1000 - timeProgress * 1000;
               // totalCycles = roundSelected - cycleCount;
                startTimer(timeLeftInMillis); //call startTimer function
                resumeTv.setEnabled(false);
                resumeTv.setVisibility(INVISIBLE);
                timeLeftTv.setVisibility(VISIBLE);
                timeLeftTv.setEnabled(true);
                pauseBtn.setVisibility(INVISIBLE);
                pauseBtn.setEnabled(false);
                playBtn.setVisibility(INVISIBLE);
                playBtn.setEnabled(false);
            }
        });

        timeLeftTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    timeCountDown.cancel(); // pause the countdown
                    timerRunning = false;
                    resumeTv.setEnabled(true);
                    resumeTv.setVisibility(VISIBLE);
                    timeLeftTv.setVisibility(INVISIBLE);
                    timeLeftTv.setEnabled(false);
                    resumeTv.setText("RESUME");
                    timeCountDown.cancel();

            }
        });


        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   playBtnEnabled(); // hide pause button an show play button
                timePause();

                /*To make play button visible*/
            }
        });
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            long timeLeftInMillis = timeSelected * 1000 - timeProgress * 1000;
            checkInput();
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTime();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTimeFunction();
            }
        });

        //load background image
        loadSavedBackground();
    }
    private void setBackgroundImage(Uri imageUri) {
        try {
            Glide.with(this)
                    .load(imageUri)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mainLayout_emom.setBackground(resource);
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
        mainLayout_emom.setBackgroundResource(drawableResourceId);
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
    private void setTimeFunction() {
        Dialog timeDialog = new Dialog(this);
        timeDialog.setContentView(R.layout.add_dialog);
        EditText timeSet = timeDialog.findViewById(R.id.etGetTime);
        EditText roundSet = timeDialog.findViewById(R.id.etGetRounds);
        TextView timeLeftTv = findViewById(R.id.tvTimeLeft);
        ProgressBar progressBar = findViewById(R.id.pbTimer);
        TextView roundDisplay = findViewById(R.id.tvrounds);

        timeDialog.findViewById(R.id.btnOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (timeSet.getText().toString().isEmpty() || roundSet.getText().toString().isEmpty()) {
                    alertdialog("EMOM","Please add EMOM time duration");
                    /*To make play button visible*/
                    playBtnEnabled();
                } else {
                    roundDisplay.setText("Every "+ timeSet.getText().toString() + " seconds for " + roundSet.getText().toString() + " minutes");
                    //roundDisplay.setText("Every "+ timeSelected + " seconds for " + (roundSelected - cycleCount) + "minutes");
                    timeSelected = Integer.parseInt(timeSet.getText().toString());
                    //timeSelectedl = Integer.parseInt(timeSet.getText().toString());
                    roundSelected = Integer.parseInt(roundSet.getText().toString());
                    updateTimerText(); // display mm:ss format
                    progressBar.setMax(timeSelected);
                    timeLeftInMillis = timeSelected;
                    minutes = (int) (timeLeftInMillis / 1000) / 60;
                    seconds = (int) (timeLeftInMillis / 1000) % 60;
                    timerRunning = false;
                    preTimeRunning = false;
                    pre_timeCountDown = null;
                    timeCountDown = null;
                    //saveToDb();
                }
                timeDialog.dismiss();
            }
        });
        timeDialog.show();
    }
    private void goback_to_mainMenu() {
        startActivity(new Intent(pbemom.this, Menu_Activity.class));
    }
    private void resetTime() {
        if (timeCountDown != null ) {
            timeCountDown.cancel();
            timeCountDown = null;
            timerRunning = false;
        }
        timeProgress = 0;
        roundSelected = 0;
        timeSelected = 0;
        pauseOffSet = 0;
        cycleCount = 0;
        minutes = 0;
        seconds = 0;
        preTimeRunning = false;

        ProgressBar progressBar = findViewById(R.id.pbTimer);
        progressBar.setProgress(0);
        TextView resumeTv = findViewById(R.id.tvResume);
        resumeTv.setVisibility(INVISIBLE);
        resumeTv.setEnabled(false);
        TextView roundDisplay = findViewById(R.id.tvrounds);
        roundDisplay.setText("");
        playBtnEnabled();

       if (pre_timeCountDown != null) {
           pre_timeCountDown.cancel();
           pre_timeCountDown = null;
           preTimeRunning = false;
       }
          TextView timeLeftTv = findViewById(R.id.tvTimeLeft);
          timeLeftTv.setEnabled(true);
          timeLeftTv.setVisibility(VISIBLE);
          timeLeftTv.setText("00:00");
          minutes = 0;
          seconds = 0;
          timeProgress = 0;
          timerRunning = false;
          playBtnEnabled();

    }

    public void startTimer(long timeLeftInMillisParam) {
         timeLeftInMillis =  timeLeftInMillisParam ;
        TextView timeLeftTv = findViewById(R.id.tvTimeLeft);

        playBtnDisabled();
        timeLeftTv.setEnabled(true);
        pauseBtn.setVisibility(INVISIBLE);
        pauseBtn.setEnabled(false);
        playBtn.setVisibility(INVISIBLE);
        playBtn.setEnabled(false);
        isPaused = false;
        if (timeLeftInMillis == timeSelected)
        { playLetsGo();} // Play sound
        ProgressBar progressBar = findViewById(R.id.pbTimer);
        progressBar.setProgress(timeSelected - timeProgress);

        timeCountDown = new CountDownTimer(timeLeftInMillis, 1000) { // Use the passed-in timeLeftInMillis
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis  = millisUntilFinished; // Track remaining time
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                int minutes = (int) (millisUntilFinished / 1000) / 60;

                // Update UI
                progressBar.setProgress((int) (millisUntilFinished / 1000));
                updateTimerText();

                // Sound effects
                if (seconds == 10 && minutes == 0) {
                    // playTenSec();
                }
                if (seconds == 3 || seconds == 2 || seconds == 1) {
                    // playsinglebeep();
                }
            }

            @Override
            public void onFinish() {
                // Update round display
                TextView roundDisplay = findViewById(R.id.tvrounds);
                roundDisplay.setText("Every "+ timeSelected + " seconds for " + (roundSelected - cycleCount) + " minutes");
                cycleCount++; // Increment FIRST to avoid extra cycle
                if (cycleCount <= roundSelected) {
                    timeProgress = 0;
                    progressBar.setProgress(timeSelected);
                    startTimer(timeSelected * 1000); // Start next round

                } else {
                    Toast.makeText(pbemom.this, "Times up!!!", Toast.LENGTH_SHORT).show();
                    playWellDone();
                    timerRunning = false;
                    showButtons();
                    saveToDb();
                    resetTime();
                }
            }
        }.start();

        timerRunning = true;
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

    private void playBtnDisabled() {
        playBtn.setEnabled(false);
        playBtn.setVisibility(INVISIBLE);
        pauseBtn.setEnabled(true);
        pauseBtn.setVisibility(VISIBLE);
    }
    private void playBtnEnabled() {
        playBtn.setEnabled(true);
        playBtn.setVisibility(VISIBLE);
        pauseBtn.setEnabled(false);
        pauseBtn.setVisibility(INVISIBLE);
    }
    /*play lets go sound function */
    private void updateTimerText() {
        //long timeLeftInMillis = timeSelected * 1000 - timeProgress * 1000;
        if (timeLeftInMillis >= 0) {
            TextView timeLeftTv = findViewById(R.id.tvTimeLeft);
            int minutes = (int) (timeLeftInMillis / 1000) / 60;
            int seconds = (int) (timeLeftInMillis / 1000) % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            timeLeftTv.setText(timeFormatted);
        } else {
            timeCountDown.cancel();
            timePause();
            resetTime();
        }
    }
    public void playWellDone() {
        wellDonePlayer.start();
    }
    public void playsinglebeep() {
        singlebeepplayer.start();
    }
    public void playLetsGo() {
        mplayer.start();
    }
    public void playTenSec() {
        tensec.start();
    }
    public void checkInput() {
       // Toast.makeText(this, "Time selected is "+ timeSelected + "timeProgress is " +timeProgress, Toast.LENGTH_SHORT).show();
        if (timeSelected > timeProgress) {
            preStartTimer();
        } else {
           // Toast.makeText(this, "Please add time duration", Toast.LENGTH_SHORT).show();
            alertdialog("EMOM","Please add EMOM time duration");
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
    public void showButtons() {
        addBtn.setVisibility(VISIBLE);
        addBtn.setEnabled(true);
        TextView addTimeTv = findViewById(R.id.tvAddTime);
        addTimeTv.setVisibility(VISIBLE);
    }
    public void hideButtons() {
        addBtn.setVisibility(INVISIBLE);
        addBtn.setEnabled(false);
        TextView addTimeTv = findViewById(R.id.tvAddTime);
        addTimeTv.setVisibility(INVISIBLE);
    }

    public void preStartTimer() {
            hideButtons();

            timeProgress = 0; // reset to 0
            preTimeLeft = 10000; // top up the timer
            Toast.makeText(pbemom.this, "Your workout starts in 10!", Toast.LENGTH_SHORT).show();
            playBtnDisabled();
            isPaused = false;
            preTimeRunning = true;
            pre_timeCountDown = new CountDownTimer(preTimeLeft, 1000) { //10 seconds countdown
                @Override
                public void onTick(long millisUntilFinished) {

                    TextView timeLeftTv = findViewById(R.id.tvTimeLeft);
                    int seconds = (int) (preTimeLeft / 1000) % 60;
                    String timeFormatted = String.format("%02d", seconds);
                    timeLeftTv.setText(timeFormatted);
                    if (seconds == 3 || seconds == 2 || seconds == 1) { // play single beep last 3 seconds
                        playsinglebeep();
                    }
                    preTimeLeft = preTimeLeft - 1000;
                }
                @Override
                public void onFinish() {
                    timeProgress = 0; // reset to 0
                    preTimeRunning = false;
                    startTimer(timeLeftInMillis); //call startTimer function
                    preTimeLeft = 10000; // top up the timer
                }
            }.start();
    }

    private void saveToDb() {
        WorkoutDbHelper dbHelper = new WorkoutDbHelper(pbemom.this);

        try {
            long id = dbHelper.insertWorkout(
                    "EMOM",
                    timeSelected,
                    0,
                    roundSelected,
                    voiceNotePath,  // Use the recorded voice path
                    ""  // Empty video note
            );

           // dbId = id;

            if (id != -1) {
                Log.d("DB_DEBUG", "Saved successfully with ID: " + id);
                Toast.makeText(pbemom.this, "Saved successfully with ID: " + id, Toast.LENGTH_SHORT).show();

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
    private void timePause () {
            /*prestart timer*/
            if (pre_timeCountDown != null) {
                pre_timeCountDown.cancel();
                preTimeRunning = false;
                isPaused = true;
            }
            playBtnEnabled();
        }
    }



