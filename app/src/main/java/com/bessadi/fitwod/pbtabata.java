package com.bessadi.fitwod;

import static android.app.ProgressDialog.show;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
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
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class pbtabata extends AppCompatActivity {

    AdView mAdView;
    private WorkoutAdapter adapter;
   // String tabataVoiceNote = adapter.voiceNote;
    private VoiceRecorder voiceRecorder;
    private String voiceNote = null;

    public  long dbId;
    ImageButton addBtn, resetBtn, playBtn, goback, recordVoice;
    CountDownTimer timeCountDown = null, pre_timeCountDown = null, timeCountDownRest =null;
    int timeSelected = 0, restSelected = 0;
    int roundSelected = 0, maxround = 0, roundcounter = 0;
    int timeProgress = 0, minutes = 0, seconds = 0, resttimeProgress = 0 ;
    private int cycleCount = 1;
    long pauseOffSet = 0, timeLeftInMillis;

    boolean isStart = true, preTimeRunning = true, isVisible = true, timerRunning;
    MediaPlayer mplayer, restplayer, wellDonePlayer, beepPlayer, singlebeepplayer, lastround;
    // Voice recording variables
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String voiceNotePath = null;
    private ConstraintLayout mainLayout_tabata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        voiceRecorder = new VoiceRecorder(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pbtabata);
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
       // MobileAds.initialize(this);
       // mAdView = findViewById(R.id.adView);
       // AdRequest adRequest = new AdRequest.Builder().build();
       // mAdView.loadAd(adRequest);
        /*adv request detail end*/

        addBtn =  findViewById(R.id.btnAdd_tb);
        resetBtn = findViewById(R.id.ib_reset_tb);
        goback =  findViewById(R.id.ib_back_tb);
        playBtn =  findViewById(R.id.ibPlay_tb);
        recordVoice = findViewById(R.id.btnRecordVoice);


        //sound variables
        mplayer = MediaPlayer.create(pbtabata.this,R.raw.let_go);
        restplayer = MediaPlayer.create(pbtabata.this,R.raw.rest);
        wellDonePlayer = MediaPlayer.create(pbtabata.this,R.raw.welldone);
        beepPlayer = MediaPlayer.create(pbtabata.this,R.raw.single_beep);
        singlebeepplayer = MediaPlayer.create(pbtabata.this, R.raw.singlebeep1);
        lastround = MediaPlayer.create(pbtabata.this, R.raw.lastround);
        //main background image
        mainLayout_tabata = findViewById(R.id.main_layout_tabata);



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

                //long timeLeftInMillis = timeSelected * 1000 - timeProgress * 1000;
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
        //load save background drawable/image
        loadSavedBackground();


    }

    protected void onResume() {
        super.onResume();
        loadSavedBackground();
    }


    private void setDefaultBackground(int drawableResourceId) {
        mainLayout_tabata.setBackgroundResource(drawableResourceId);
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


    private void setBackgroundImage(Uri imageUri) {
        try {
            Glide.with(this)
                    .load(imageUri)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mainLayout_tabata.setBackground(resource);
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
            String fileName = "TABATA_VOICE_" + timeStamp + ".3gp";

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

    private void toggleRecording() {
        if (voiceRecorder.isRecording()) {
            voiceRecorder.stopVoiceRecording();
            // Update UI to show recording stopped
        } else {
            try {
                voiceRecorder.startVoiceRecording();
                // Update UI to show recording started
            } catch (IOException e) {
                Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void setTimeFunction() {
        /*START declaring a new dialog for time input*/
        Dialog timeDialog = new Dialog(this);
        timeDialog.setContentView(R.layout.add_tabata_dialog);
        EditText timeWork = timeDialog.findViewById(R.id.etGetTimeWork_tb);
        EditText roundSet = timeDialog.findViewById(R.id.etGetRounds_tb);
        EditText timeRest = timeDialog.findViewById(R.id.etGetTimeRest_tb);
        /*END declaring a new dialog for time input*/

        ProgressBar progressBar = findViewById(R.id.pbTimer_tb);
        TextView roundDisplay = findViewById(R.id.tvrounds_tb);

        timeDialog.findViewById(R.id.btnGo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*input check and display an appropriate message*/

               if( timeRest.getText().toString().isEmpty() || timeWork.getText().toString().isEmpty() || roundSet.getText().toString().isEmpty()) {
                   // Toast.makeText(pbtabata.this, "Please enter all of Tabata times", Toast.LENGTH_SHORT).show();
                   alertdialog("TABATA","Please enter all of Tabata times");

                } else {

                    roundDisplay.setText(roundcounter + "/" + roundSet.getText().toString() + " rounds");
                    timeSelected = Integer.parseInt(timeWork.getText().toString());
                    roundSelected = Integer.parseInt(roundSet.getText().toString());
                    restSelected = Integer.parseInt(timeRest.getText().toString());
                    updateTimerText(); // display mm:ss format
                    progressBar.setMax(timeSelected);
                    timeLeftInMillis = timeSelected;
                    minutes = (int) (timeLeftInMillis / 1000) / 60;
                    seconds = (int) (timeLeftInMillis / 1000) % 60;
                    progressBar.setMax(timeSelected);
                    //saveToDb(); // save to a database

                }
                timeDialog.dismiss();
            }
        });
        timeDialog.show();
    }
    private void goback_to_mainMenu() {
        startActivity(new Intent(pbtabata.this, Menu_Activity.class));
    }
    private void TabataInputCheck() {
        /*START declaring a new dialog for time input*/
        Dialog timeDialog = new Dialog(this);
        timeDialog.setContentView(R.layout.add_tabata_dialog);
        EditText timeWork = timeDialog.findViewById(R.id.etGetTimeWork_tb);
        EditText roundSet = timeDialog.findViewById(R.id.etGetRounds_tb);
        EditText timeRest = timeDialog.findViewById(R.id.etGetTimeRest_tb);

        if (timeRest.getText().toString().isEmpty() || timeWork.getText().toString().isEmpty() || roundSet.getText().toString().isEmpty()) {
            alertdialog("TABATA","Please enter all of Tabata times");

        }
        else{

            //timeSelected = Integer.parseInt(timeWork.getText().toString());
           // roundSelected = Integer.parseInt(roundSet.getText().toString());
           // restSelected = Integer.parseInt(timeRest.getText().toString());
            alertdialog("TABATA","Good to start Voice recording");
           // toggleRecording();
        }

    }

    private void resetTime() {
        if (timeCountDown != null ) {
            timeCountDown.cancel();
            timeProgress = 0;
            roundcounter =0;
            roundSelected = 0;
            timeSelected = 0;
            pauseOffSet = 0;
            resttimeProgress = 0;
            cycleCount = 1;
            timeCountDown = null;
            minutes = 0;
            seconds = 0;
            isStart = true;
            ProgressBar progressBar = findViewById(R.id.pbTimer_tb);
            progressBar.setProgress(0);
            ProgressBar progressBarRest = findViewById(R.id.pbTimer_tb);
            progressBarRest.setProgress(0);
            TextView timeLeftTv = findViewById(R.id.tvTimeLeft_tb);
            timeLeftTv.setText("00:00");
            TextView roundDisplay = findViewById(R.id.tvrounds_tb);
            roundDisplay.setText("");
            playBtnEnabled();

        }
        if (pre_timeCountDown != null)
        {pre_timeCountDown.cancel();
            pre_timeCountDown = null;
            TextView timeLeftTv = findViewById(R.id.tvTimeLeft_tb);
            timeLeftTv.setText("00:00");
            //playBtnEnabled();
            minutes = 0;
            seconds = 0;
            roundcounter =0;
            cycleCount = 1;
            timeProgress = 0;
            resttimeProgress = 0;
            ProgressBar progressBar = findViewById(R.id.pbTimer_tb);
            progressBar.setProgress(0);
            ProgressBar progressBarRest = findViewById(R.id.pbTimer_tb);
            progressBarRest.setProgress(0);
            TextView roundDisplay = findViewById(R.id.tvrounds_tb);
            roundDisplay.setText("");
            playBtnEnabled();
        }
    }
    private void startTimer(long timeLeftInMillis) {
        playBtnDisabled(); // 10 seconds heads up prior tot eh start of the main timer.
        if (timeSelected > timeProgress) {

            ProgressBar progressBar = findViewById(R.id.pbTimer_tb);
            progressBar.setProgress(timeProgress);
            //set progressbar colour to blue_progressbar_background when in Work mode
            progressBar.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.blue_progressbar_background));
            maxround = roundSelected;
            roundcounter++;
            if (roundcounter == maxround) // play last round
            {playLastRound();}
            else{playLetsGo();} // play lets go sound}
            timeCountDown = new CountDownTimer((timeSelected * 1000) , 1000) {
                @Override
            public void onTick(long p0) {

                long timeLeftInMillis = timeSelected * 1000 - timeProgress * 1000;
                int seconds = (int) (timeLeftInMillis / 1000) % 60;
                if (seconds == 2 || seconds == 1 || seconds == 0) { // play single beep last 3 seconds
                        playsinglebeep();
                    }
                progressBar.setProgress(timeSelected - timeProgress);
                TextView roundDisplay = findViewById(R.id.tvrounds_tb);
                updateTimerText();// update time format mm:ss
                roundDisplay.setText(roundcounter + "/" + maxround + " - Work");
                timeProgress++;
            }
            @Override
            public void onFinish() {
                long p1 = 0;
                startTimer_REST(p1);// call timer during rest period
        }
        }.start();
            timerRunning = true;
        } else {
           // Toast.makeText(this, "Please add TABATA time duration", Toast.LENGTH_SHORT).show();
            alertdialog("TABATA","Please enter all of Tabata times");
        }
    }
    private void btnVisibility(boolean isVisible) {
        if (isVisible) {
            playBtn.setVisibility(View.VISIBLE);

        }else {
            playBtn.setVisibility(View.INVISIBLE);

        }
    }
    public void checkInput() {
        if (timeSelected > timeProgress) {
            preStartTimer();
        } else {
           // Toast.makeText(this, "Please add TABATA time duration", Toast.LENGTH_SHORT).show();
            alertdialog("TABATA","Please enter all of TABATA times");
        }
    }
    private void startTimer_REST(long timeLeftInMillis) {
        ProgressBar progressBarRest = findViewById(R.id.pbTimer_tb);
        timeProgress = 0;
        progressBarRest.setProgress(timeProgress);
        //Change progressbar colour when in rest time
        progressBarRest.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.white_progressbar_background));
        playRest(); //PLAY REST SOUND//
        timeCountDownRest = new CountDownTimer((restSelected * 1000) , 1000) {
            @Override
            public void onTick(long p0) {

                long timeLeftInMillis = restSelected * 1000 - timeProgress * 1000;
                int seconds = (int) (timeLeftInMillis / 1000) % 60;
                if (seconds == 2 || seconds == 1 || seconds == 0) { // play single beep last 3 seconds
                    playsinglebeep();
                }
                progressBarRest.setProgress(restSelected - timeProgress);
                TextView roundDisplay = findViewById(R.id.tvrounds_tb);
                updateTimerRestText();// update time format mm:ss
                roundDisplay.setText(roundcounter + "/" + maxround + " - Rest");
                timeProgress++;
            }
            @Override
            public void onFinish() {
                final int totalCycles = roundSelected;
                TextView roundDisplay = findViewById(R.id.tvrounds_tb);
                /*increment cycle count*/
                cycleCount++;
                if (cycleCount <= totalCycles) {
                    timeProgress = 0;
                    progressBarRest.setProgress(timeSelected); // Reset ProgressBar
                    startTimer(timeLeftInMillis); // Start the next cycle
                    //Toast.makeText(pbtabata.this, "round!!! "+ cycleCount, Toast.LENGTH_SHORT).show();
                } else {
                       // Toast.makeText(pbtabata.this, "times up!!!", Toast.LENGTH_SHORT).show();
                        alertdialog("TABATA","times up!!!");
                        playBtnEnabled();  //re-enable play button
                        playWellDone();  //WELL DONE SOUND
                        saveToDb();
                        resetTime();
                }
            }
        }.start();
    }
    //LETS GO SOUND
    public void playLetsGo()
    {
        mplayer.start();
    }
    //REST SOUND
    public void playRest()
    {
        restplayer.start();
    }
    //BEEP SOUND
    public void playbeep()
    {
        beepPlayer.start();
    }
    //WELL DONE SOUND
    public void playLastRound()
    {
        lastround.start();
    }
    public void playWellDone()
    {
        wellDonePlayer.start();
    }

    public void preStartTimer() {
        playBtnDisabled();
        Toast.makeText(pbtabata.this, "Your workout starts in 10 seconds ", Toast.LENGTH_SHORT).show();
       // alertdialog("Your workout starts in 10 seconds ");
        preTimeRunning = true;
        pre_timeCountDown = new CountDownTimer(10000, 1000) { //10 seconds countdown
       int timeProgress = 0;
            @Override
            public void onTick(long p0) {

                long timeLeftInMillis = 10 * 1000 - timeProgress * 1000;
                TextView timeLeftTv = findViewById(R.id.tvTimeLeft_tb);
               // int minutes = (int) (timeLeftInMillis / 1000) / 60;
                int seconds = (int) (timeLeftInMillis / 1000) % 60;
                String timeFormatted = String.format( "%02d",seconds);
                //String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                timeLeftTv.setText(timeFormatted);
                if (seconds == 2 || seconds == 1 || seconds == 0) { // play single beep last 3 seconds
                    playsinglebeep();
                }
                timeProgress++;
            }
            @Override
            public void onFinish () {
                timeProgress = 0; // reset to 0
                preTimeRunning = false;
                startTimer(timeLeftInMillis); //call startTimer function
            }
        }.start();
    }
    private void playBtnEnabled() {
        playBtn.setEnabled(true);
        playBtn.setVisibility(View.VISIBLE);
        //  pauseBtn.setEnabled(true);
        // pauseBtn.setVisibility(View.VISIBLE);
    }
    private void playBtnDisabled() {
        playBtn.setEnabled(false); // no event is generated when clicking the button
        playBtn.setVisibility(View.VISIBLE); // still see the button
    }
    private void updateTimerText() {
        long timeLeftInMillis = timeSelected * 1000 - timeProgress * 1000;
        if (timeLeftInMillis >= 0) {
            TextView timeLeftTv = findViewById(R.id.tvTimeLeft_tb);
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
    private void updateTimerRestText() {
        long timeLeftInMillis = restSelected * 1000 - timeProgress * 1000;
        if (timeLeftInMillis >= 0) {
            TextView timeLeftTv = findViewById(R.id.tvTimeLeft_tb);
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
    public void playsinglebeep() {
        singlebeepplayer.start();
    }
    /*Display dialog*/
   /* private void showExitDialog(){
        new AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("Do you really want to exit?")
                .setIcon(getResources().getDrawable(R.mipmap.ic_launcher)) // display the logo
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Action when OK is clicked
                    finishActivity(0);
                    System.exit(0);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();

    }*/
    private void showExitDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("Do you really want to exit?")
                .setIcon(R.mipmap.ic_launcher)
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

    private void alertdialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIcon(R.mipmap.ic_launcher); // This is the best way!
        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Apply theme fixes
        applyDialogThemeFix(dialog);
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
        WorkoutDbHelper dbHelper = new WorkoutDbHelper(pbtabata.this);

        try {
            long id = dbHelper.insertWorkout(
                    "TABATA",
                    timeSelected,
                    restSelected,
                    roundSelected,
                    voiceNotePath,  // Use the recorded voice path
                    ""  // Empty video note
            );

            dbId = id;

            if (id != -1) {
                Log.d("DB_DEBUG", "Saved successfully with ID: " + id);
                Toast.makeText(pbtabata.this, "Saved successfully with ID: " + id, Toast.LENGTH_SHORT).show();

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

    private void timePause() {
        if (timeCountDown != null) {
            timeCountDown.cancel();
        }
    }
}


