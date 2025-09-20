package com.bessadi.fitwod;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaRecorder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VoiceRecorder {
    private MediaRecorder mediaRecorder;
    private WorkoutDbHelper dbHelper;
    private pbtabata pbtabata;
    private Context context;
    private String currentRecordingPath;
    private long startTime;
    private boolean isRecording = false;

    public VoiceRecorder(Context context) {
        this.context = context;
        this.dbHelper = new WorkoutDbHelper(context); // Initialize the dbHelper
    }

    public void stopVoiceRecording() {
        if (mediaRecorder != null && isRecording) {
            long duration = 0;
            try {
                mediaRecorder.stop();
                isRecording = false;

                // Calculate duration
                duration = System.currentTimeMillis() - startTime;

                // Save to database only if we have a valid recording path
                if (currentRecordingPath != null) {
                    File recordingFile = new File(currentRecordingPath);
                    if (recordingFile.exists() && recordingFile.length() > 0) {
                        saveRecordingToDatabase(currentRecordingPath, duration);
                    } else {
                        // File doesn't exist or is empty
                        Toast.makeText(context, "Recording failed: no audio data", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Toast.makeText(context, "Recording error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (RuntimeException e) {
                e.printStackTrace();
                Toast.makeText(context, "Recording error (runtime): " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                currentRecordingPath = null; // Reset after saving
            }
        }
    }

    public void startVoiceRecording() throws IOException {
        // Stop any existing recording first
        if (mediaRecorder != null) {
            stopVoiceRecording();
        }

        mediaRecorder = new MediaRecorder();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File outputDir = context.getExternalFilesDir(null);
        currentRecordingPath = outputDir.getAbsolutePath() + "/audio_record_" + timeStamp + ".3gp";

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(currentRecordingPath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            startTime = System.currentTimeMillis();
            isRecording = true;
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            currentRecordingPath = null;
            isRecording = false;
            if (mediaRecorder != null) {
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            throw new IOException("Failed to start recording: " + e.getMessage());
        }
    }

    private void saveRecordingToDatabase(String filePath, long duration) {
        long timestamp = System.currentTimeMillis();

        ContentValues values = new ContentValues();

       // values.put(dbHelper.COLUMN_ID, pbtabata.dbId);
        values.put(dbHelper.COLUMN_VOICE_NOTE, filePath);
        values.put(dbHelper.COLUMN_CREATED_AT, timestamp);
        values.put(dbHelper.COLUMN_DURATION, duration);

        // Insert into your database
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long rowId = db.insert(dbHelper.TABLE_WORKOUTS, null, values);

            if (rowId != -1) {
                Toast.makeText(context, "Recording saved successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to save recording to database", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}