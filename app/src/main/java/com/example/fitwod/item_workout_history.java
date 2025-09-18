package com.example.fitwod;

import static androidx.fragment.app.FragmentManager.TAG;

import static com.example.fitwod.DatabaseBackupUtil.exportToCSV;
import static com.example.fitwod.WorkoutDbHelper.COLUMN_VIDEO_NOTE;
import static com.example.fitwod.WorkoutDbHelper.COLUMN_VOICE_NOTE;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.util.Calendar;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SearchView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bessadi.fitwod.R;


import com.google.android.material.chip.ChipGroup;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class item_workout_history extends AppCompatActivity {
    private WorkoutDbHelper dbHelper;
    private WorkoutAdapter adapter;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private VoiceRecorder voiceRecorder;
    private Cursor cursor , filteredCursor;
    private boolean isRecording = false;
    // Add these class variables
    private ImageButton btnDatePicker, recordVoice;
    private Calendar selectedDate;
    ImageButton ib_back_db;
    private ChipGroup filterChipGroup;
    private Set<String> selectedFilters = new HashSet<>();
    private int timeSelected = 20; // Default values
    private int restSelected = 10;
    private int roundSelected = 8;
    private Context context;
    private ImageButton btnBackup, btnExport, btnAddPhoto, btnRestore;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_RESTORE_BACKUP = 3;

    private static final int REQUEST_CAMERAX_CAPTURE = 102;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int PERMISSION_REQUEST_GALLERY = 103;
    private static final int PERMISSION_REQUEST_CAMERA = 104;

    private long currentWorkoutIdForVoice = -1;
    private String voiceNotePath;

    private long currentWorkoutIdForPhoto = -1;
    private String currentPhotoPath;
    private Uri currentImageUri; //
    private MediaPlayer currentMediaPlayer; // Add this class variable

    private MediaRecorder mediaRecorder;

   // public item_workout_history(Context context)
   // {this.context = context;}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history_list);
        ib_back_db = findViewById(R.id.ib_back_db);
        btnDatePicker = findViewById(R.id.btn_date_picker);
        selectedDate = Calendar.getInstance();
        //buttons for backup -image - and external export
        btnBackup = findViewById(R.id.btn_backup);
        btnExport = findViewById(R.id.btn_export);
        btnAddPhoto = findViewById(R.id.btn_add_photo);
        btnRestore = findViewById(R.id.btn_restore);
        recordVoice = findViewById(R.id.btnRecordVoice);
        //methods for the above three buttons
        btnBackup.setOnClickListener(v -> backupData());
        btnExport.setOnClickListener(v -> exportLogs());
        btnAddPhoto.setOnClickListener(v -> showPhotoOptionsDialog());
        btnRestore.setOnClickListener(v -> showRestoreOptionsDialog());
      //  recordVoice.setOnClickListener(v -> showVoiceRecordingOptions());
        recordVoice.setOnClickListener(v -> toggleRecording());



        btnDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // showDatePickerDialog();
                showCustomDatePickerDialog();
            }
        });


        // Check permissions
        checkPermissions();

        // Initialize components
        dbHelper = new WorkoutDbHelper(this);
        voiceRecorder = new VoiceRecorder(this);

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Pass null initially
        adapter = new WorkoutAdapter(this, null);
        recyclerView.setAdapter(adapter);

        // Use the correct listener type - our custom interface from WorkoutAdapter
        adapter.setOnItemLongClickListener(new WorkoutAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClicked(int position, long workoutId) {
              //  showDeleteDialog(position, workoutId);
                showWorkoutOptionsDialog(position, workoutId);
            }
        });

        recyclerView.setAdapter(adapter);

        ib_back_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goback_to_mainMenu();
            }
        });
        // Load workouts
        loadAllWorkouts();

        // Setup media buttons
        //setupMediaButtons();
        Intent intent = getIntent();
        if (intent != null) {
            timeSelected = intent.getIntExtra("work_time", 20);
            restSelected = intent.getIntExtra("rest_time", 10);
            roundSelected = intent.getIntExtra("rounds", 8);
        }

        // Setup search functionality
        setupSearchView();

        dbHelper.debugAllDates();
        dbHelper.testManualQuery();
        dbHelper.checkForHiddenCharacters();

        //isolate buttons fron theme interference
        isolateAllFilterButtons();
        //setup button click listeners
        setupFilterButtons();
        //debugTabataButton();

    }
    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
        } else {
            // First, ask which workout to add the voice note to
            showWorkoutSelectionForRecording();
        }
    }


    private void showRestoreOptionsDialog() {
        String[] options = {"Restore from File", "Restore from Last Backup"};

        new AlertDialog.Builder(this)
                .setTitle("Restore Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // "Restore from File"
                            restoreFromBackup();
                            break;
                        case 1: // "Restore from Last Backup"
                            restoreFromLastBackup();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showWorkoutSelectionForRecording() {
        // Get all workouts for selection
        Cursor workoutsCursor = dbHelper.getAllWorkouts();

        if (workoutsCursor == null || workoutsCursor.getCount() == 0) {
            Toast.makeText(this, "No workouts found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create arrays for dialog
        String[] workoutNames = new String[workoutsCursor.getCount()];
        final long[] workoutIds = new long[workoutsCursor.getCount()];

        int index = 0;
        while (workoutsCursor.moveToNext()) {
            String type = workoutsCursor.getString(workoutsCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE));
            String date = workoutsCursor.getString(workoutsCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT));

            // Format date
            String formattedDate;
            try {
                long timestamp = Long.parseLong(date);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                formattedDate = sdf.format(new Date(timestamp));
            } catch (NumberFormatException e) {
                formattedDate = date;
            }

            workoutNames[index] = type + " - " + formattedDate;
            workoutIds[index] = workoutsCursor.getLong(workoutsCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID));
            index++;
        }
        workoutsCursor.close();

        new AlertDialog.Builder(this)
                .setTitle("Select Workout for Voice Note")
                .setItems(workoutNames, (dialog, which) -> {
                    currentWorkoutIdForVoice = workoutIds[which];
                    startRecording();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void restoreFromLastBackup() {
        // Automatically find and restore from the most recent backup
        File backupDir = new File(getExternalFilesDir(null), "Backups");
        if (backupDir.exists() && backupDir.isDirectory()) {
            File[] backupFiles = backupDir.listFiles((dir, name) -> name.endsWith(".db"));

            if (backupFiles != null && backupFiles.length > 0) {
                // Find the most recent backup
                File latestBackup = backupFiles[0];
                for (File file : backupFiles) {
                    if (file.lastModified() > latestBackup.lastModified()) {
                        latestBackup = file;
                    }
                }

                restoreDatabaseFromBackup(Uri.fromFile(latestBackup));
            } else {
                Toast.makeText(this, "No backup files found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Backup directory not found", Toast.LENGTH_SHORT).show();
        }
    }
    private void restoreFromBackup() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/x-sqlite3");
        startActivityForResult(intent, REQUEST_RESTORE_BACKUP);
    }
    private void backupData() {

            showBackupOptionsDialog();
       //backupToAppSpecificStorage();

    }
    private void backupToAppSpecificStorage() {
        try {
            File backupDir = new File(getExternalFilesDir(null), "Backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File backupFile = new File(backupDir, "fitwod_backup_" + timeStamp + ".db");

            // Get the database file
            File dbFile = getDatabasePath(WorkoutDbHelper.DATABASE_NAME);

            if (dbFile.exists()) {
                // Copy database file
                FileInputStream inStream = new FileInputStream(dbFile);
                FileOutputStream outStream = new FileOutputStream(backupFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }

                inStream.close();
                outStream.close();

                // Share the backup file
                shareFile(backupFile, "application/x-sqlite3", "Share Database Backup");
                Toast.makeText(this, "Backup created successfully!", Toast.LENGTH_LONG).show();

                Log.d("BACKUP_DEBUG", "Backup saved to: " + backupFile.getAbsolutePath());
                Log.d("BACKUP_DEBUG", "Backup file size: " + backupFile.length() + " bytes");

            } else {
                Toast.makeText(this, "Database file not found", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Log.e("BACKUP_DEBUG", "Backup error: " + e.getMessage());
            Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private File getAppSpecificBackupDir() {
        File dir = new File(getExternalFilesDir(null), "Backups");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    private File getAppSpecificExportDir() {
        File dir = new File(getExternalFilesDir(null), "Exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private void copyFile(File source, File dest) throws IOException {
        FileInputStream inStream = new FileInputStream(source);
        FileOutputStream outStream = new FileOutputStream(dest);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, length);
        }

        inStream.close();
        outStream.close();
    }
    private String createCsvContent() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Type,Work Time,Rest Time,Rounds,Date,Notes\n");

        Cursor cursor = dbHelper.getAllWorkouts();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                csv.append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID))).append(",");
                csv.append(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE))).append(",");
                csv.append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TIME))).append(",");
                csv.append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_REST_TIME))).append(",");
                csv.append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ROUNDS))).append(",");
                csv.append(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT))).append(",");
               // csv.append("\"").append(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_NOTES))).append("\"\n");
            } while (cursor.moveToNext());
            cursor.close();
        }
        return csv.toString();
    }

    private void shareFile(File file, String mimeType, String title) {
        Uri contentUri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(shareIntent, title));
    }




        private void exportLogs() {
       // if (checkStoragePermission()) {
            showExportOptionsDialog();
       // }
    }
    private void showBackupOptionsDialog() {
        String[] options = {"Backup Database Only", "Backup Database + Photos"};

        new AlertDialog.Builder(this)
                .setTitle("Backup Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            backupToAppSpecificStorage();
                            break;
                        case 1:
                            backupDatabaseWithPhotos();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showExportOptionsDialog() {
        String[] options = {"Export to CSV", "Export to Excel", "Export to JSON"};

        new AlertDialog.Builder(this)
                .setTitle("Export Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            exportToCSVFormat();
                            break;
                        case 1:
                            exportToExcelFormat();
                            break;
                        case 2:
                            exportToJSONFormat();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void exportToCSVFormat() {
        try {
            File exportDir = new File(getExternalFilesDir(null), "Exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File exportFile = new File(exportDir, "workout_export_" + timeStamp + ".csv");

            StringBuilder csvContent = new StringBuilder();
            csvContent.append("ID,Type,Work Time,Rest Time,Rounds,Date,Notes\n");

            Cursor cursor = dbHelper.getAllWorkouts();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    csvContent.append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID))).append(",");
                    csvContent.append(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE))).append(",");
                    csvContent.append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TIME))).append(",");
                    csvContent.append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_REST_TIME))).append(",");
                    csvContent.append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ROUNDS))).append(",");
                    csvContent.append(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT))).append(",");
                    csvContent.append(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOICE_NOTE))).append(",");
                    csvContent.append(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE))).append(",");// COLUMN_VIDEO_NOTE to store photo
                } while (cursor.moveToNext());
                cursor.close();
            }

            FileOutputStream fos = new FileOutputStream(exportFile);
            fos.write(csvContent.toString().getBytes());
            fos.close();

            shareFile(exportFile, "text/csv", "Share Workout Export");
            Toast.makeText(this, "CSV exported successfully!", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void backupDatabaseOnly() {
        boolean success = DatabaseBackupUtil.backupDatabase(this);
        if (success) {
            Toast.makeText(this, "Database backup created successfully!", Toast.LENGTH_LONG).show();
        }
    }



    private void backupDatabaseWithPhotos() {
        try {
            // First backup the database
            File backupDir = new File(getExternalFilesDir(null), "Backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File backupFile = new File(backupDir, "fitwod_complete_backup_" + timeStamp + ".db");
            File photosDir = new File(backupDir, "photos_" + timeStamp);

            // Backup database
            File dbFile = getDatabasePath(WorkoutDbHelper.DATABASE_NAME);
            if (dbFile.exists()) {
                copyFile(dbFile, backupFile);
            }

            // Backup photos
            int photoCount = 0;
            Cursor cursor = dbHelper.getAllWorkouts();
            if (cursor != null && cursor.moveToFirst()) {
                if (!photosDir.exists()) {
                    photosDir.mkdirs();
                }

                do {
                    String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE));
                    if (photoPath != null && !photoPath.isEmpty()) {
                        File sourcePhoto = new File(photoPath);
                        if (sourcePhoto.exists()) {
                            File destPhoto = new File(photosDir, sourcePhoto.getName());
                            copyFile(sourcePhoto, destPhoto);
                            photoCount++;
                        }
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            Toast.makeText(this, "Backup complete! " + photoCount + " photos backed up", Toast.LENGTH_LONG).show();
            shareFile(backupFile, "application/x-sqlite3", "Share Complete Backup");

        } catch (IOException e) {
            Toast.makeText(this, "Backup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void exportToExcelFormat() {
        exportToCSVFormat(); // For now, just use CSV
        Toast.makeText(this, "Excel export (as CSV) completed!", Toast.LENGTH_LONG).show();
    }
    private void exportToJSONFormat() {
        // Simple JSON export implementation
        try {
            File exportDir = new File(getExternalFilesDir(null), "Exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File exportFile = new File(exportDir, "workout_export_" + timeStamp + ".json");

            StringBuilder jsonContent = new StringBuilder();
            jsonContent.append("[\n");

            Cursor cursor = dbHelper.getAllWorkouts();
            if (cursor != null && cursor.moveToFirst()) {
                boolean first = true;
                do {
                    if (!first) {
                        jsonContent.append(",\n");
                    }
                    first = false;

                    jsonContent.append("  {\n");
                    jsonContent.append("    \"id\": ").append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID))).append(",\n");
                    jsonContent.append("    \"type\": \"").append(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE))).append("\",\n");
                    jsonContent.append("    \"work_time\": ").append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TIME))).append(",\n");
                    jsonContent.append("    \"rest_time\": ").append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_REST_TIME))).append(",\n");
                    jsonContent.append("    \"rounds\": ").append(cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ROUNDS))).append(",\n");
                    jsonContent.append("    \"date\": \"").append(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT))).append("\",\n");
                    jsonContent.append("    \"date\": \"").append(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOICE_NOTE))).append("\",\n");
                    jsonContent.append("    \"notes\": \"").append(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE)) != null ?
                            cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE)) : "").append("\"\n");
                    jsonContent.append("  }");
                } while (cursor.moveToNext());
                cursor.close();
            }

            jsonContent.append("\n]");

            FileOutputStream fos = new FileOutputStream(exportFile);
            fos.write(jsonContent.toString().getBytes());
            fos.close();

            shareFile(exportFile, "application/json", "Share Workout Export (JSON)");
            Toast.makeText(this, "JSON exported successfully!", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(this, "JSON export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("CHECK_PERMISSION", "Storage permission granted!");
                // Retry the operation that required permission
                retryBackupOrExport();
            } else {
                Log.d("CHECK_PERMISSION", "Storage permission denied");
                Toast.makeText(this, "Storage permission is required for backup/export", Toast.LENGTH_LONG).show();
            }
        }
    }*/

    private void retryBackupOrExport() {
        // You can implement logic to remember what operation was requested
        Toast.makeText(this, "Please try backup/export again", Toast.LENGTH_SHORT).show();
    }
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.d("CHECK_PERMISSION", "Requesting storage permission");

                // Request the permission
                requestPermissions(new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 100);

                return false;
            } else {
                Log.d("CHECK_PERMISSION", "Storage permission already granted");
                return true;
            }
        }
        Log.d("CHECK_PERMISSION", "No permission needed (pre-Marshmallow)");
        return true; // Pre-Marshmallow doesn't need runtime permissions
    }

    private void showWorkoutSelectionForPhoto() {
        // This is only called when user clicks the photo button directly
        // (not from long-press menu)
        Cursor workoutsCursor = dbHelper.getAllWorkouts();

        if (workoutsCursor == null || workoutsCursor.getCount() == 0) {
            Toast.makeText(this, "No workouts found", Toast.LENGTH_SHORT).show();
            return;
        }

        // ... your existing workout selection code
        // When user selects a workout, set currentWorkoutIdForPhoto and show photo options
    }
    private void showPhotoOptionsDialog() {
        //Check if a workout is already selected.
        if(currentWorkoutIdForPhoto == -1){
            showWorkoutSelectionForPhoto();
            return;
        }
        //If workout is already selected, directly show camer/gallert options
        String[] options ={"Take Photo","Choose from Gallery"};

    new AlertDialog.Builder(this)
            .setTitle("Add Photo to Workout")
            .setItems(options, (dialog, which) -> {
        if (which == 0) {
            takePhoto();
        } else {
            chooseFromGallery();
        }
    })
            .setNegativeButton("Cancel", null)
            .setOnCancelListener(dialog -> {
        // Reset selection if user cancels
        currentWorkoutIdForPhoto = -1;
    })
            .show();

}
    private void takePhoto() {
        if (currentWorkoutIdForPhoto == -1) {
            Toast.makeText(this, "Please select a workout first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkCameraPermissions()) {
            launchCameraX();
        } else {
            requestCameraPermissions();
        }
    }
    private void launchCameraX() {
        try {
            Intent cameraIntent = new Intent(this, CameraXActivity.class);
            cameraIntent.putExtra("workoutId", currentWorkoutIdForPhoto);
            startActivityForResult(cameraIntent, REQUEST_CAMERAX_CAPTURE);
        } catch (Exception e) {
            Log.e("CameraX", "Error launching CameraX", e);
            Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
        }
    }
    private void requestCameraPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Always need camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
        }

        // Storage permission depends on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                launchCameraX();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

   /* private void takePhoto() {
        Log.d("PHOTO_DEBUG", "=== takePhoto() called ===");
        Log.d("PHOTO_DEBUG", "currentWorkoutIdForPhoto: " + currentWorkoutIdForPhoto);

        if (currentWorkoutIdForPhoto == -1) {
            Toast.makeText(this, "Please select a workout first", Toast.LENGTH_SHORT).show();
            return;
        }

        //Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent cameraIntent = new Intent(this, CameraXActivity.class);
        cameraIntent.putExtra("workoutId", currentWorkoutIdForPhoto);
        startActivityForResult(cameraIntent, REQUEST_CAMERAX_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            Log.d("PHOTO_DEBUG", "Starting camera activity without EXTRA_OUTPUT");

            // REMOVE the EXTRA_OUTPUT logic - let camera app handle storage
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);

        } else {
            Log.d("PHOTO_DEBUG", "No camera app found");
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }*/

   /* private void takePhoto() {
        Log.d("PHOTO_DEBUG", "=== takePhoto() called ===");
        Log.d("PHOTO_DEBUG", "currentWorkoutIdForPhoto: " + currentWorkoutIdForPhoto);
        Log.d("PHOTO_DEBUG", "currentImagePath: " + currentPhotoPath);

        // No need to select workout - use the already selected one
        if (currentWorkoutIdForPhoto == -1) {
            Log.d("PHOTO_DEBUG", "ERROR: No workout selected");
            Toast.makeText(this, "Please select a workout first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Log.d("PHOTO_DEBUG", "Camera app available");

            File photoFile = createImageFile();
            Log.d("PHOTO_DEBUG", "createImageFile() returned: " + (photoFile != null ? photoFile.getAbsolutePath() : "NULL"));

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        getPackageName() + ".provider", photoFile);
                Log.d("PHOTO_DEBUG", "Photo URI: " + photoURI);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Add permission flags
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Log.d("PHOTO_DEBUG", "Starting camera activity...");
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Log.d("PHOTO_DEBUG", "ERROR: Failed to create image file");
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("PHOTO_DEBUG", "ERROR: No camera app found");
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }*/

    private void showWorkoutSelectionDialog() {
        // Get all workouts for selection
        Cursor workoutsCursor = dbHelper.getAllWorkouts();

        if (workoutsCursor == null || workoutsCursor.getCount() == 0) {
            Toast.makeText(this, "No workouts found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create arrays for dialog
        String[] workoutNames = new String[workoutsCursor.getCount()];
        final long[] workoutIds = new long[workoutsCursor.getCount()];

        int index = 0;
        while (workoutsCursor.moveToNext()) {
            String type = workoutsCursor.getString(workoutsCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE));
            String date = workoutsCursor.getString(workoutsCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT));

            // Format date if it's a timestamp
            String formattedDate;
            try {
                long timestamp = Long.parseLong(date);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                formattedDate = sdf.format(new Date(timestamp));
            } catch (NumberFormatException e) {
                formattedDate = date;
            }

            workoutNames[index] = type + " - " + formattedDate;
            workoutIds[index] = workoutsCursor.getLong(workoutsCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID));
            index++;
        }
        workoutsCursor.close();

        new AlertDialog.Builder(this)
                .setTitle("Select Workout for Photo")
                .setItems(workoutNames, (dialog, which) -> {
                    currentWorkoutIdForPhoto = workoutIds[which];
                    launchCamera();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create a content values object for the new image
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "FitWod_Workout_Photo");
            values.put(MediaStore.Images.Media.DESCRIPTION, "Workout photo for FitWod app");
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

            Uri imageUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                currentImageUri = imageUri; // Store this as a class variable
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void chooseFromGallery() {
        if (currentWorkoutIdForPhoto == -1) {
            Toast.makeText(this, "Please select a workout first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for permission based on Android version
        if (checkGalleryPermission()) {
            openGallery();
        } else {
            requestGalleryPermission();
        }
    }
    private boolean checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - Use READ_MEDIA_IMAGES
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 and below - Use READ_EXTERNAL_STORAGE
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                    PERMISSION_REQUEST_GALLERY);
        } else {
            // Android 12 and below
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_GALLERY);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }
  /*  private void chooseFromGallery() {
        // No need to select workout - use the already selected one
        if (currentWorkoutIdForPhoto == -1) {
            Toast.makeText(this, "Please select a workout first", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }*/
    private File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            Log.d("PHOTO_DEBUG", "Storage directory: " + (storageDir != null ? storageDir.getAbsolutePath() : "NULL"));
            Log.d("PHOTO_DEBUG", "Directory exists: " + (storageDir != null ? storageDir.exists() : "NULL"));
            Log.d("PHOTO_DEBUG", "Directory writable: " + (storageDir != null ? storageDir.canWrite() : "NULL"));

            if (storageDir != null && !storageDir.exists()) {
                boolean dirCreated = storageDir.mkdirs();
                Log.d("PHOTO_DEBUG", "Directory created: " + dirCreated);
            }

            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save the file path for later use
            currentPhotoPath = image.getAbsolutePath();
            Log.d("PHOTO_DEBUG", "Image file created: " + currentPhotoPath);
            Log.d("PHOTO_DEBUG", "Image file can write: " + image.canWrite());

            return image;

        } catch (IOException e) {
            Log.e("PHOTO_DEBUG", "Error creating image file", e);
            Toast.makeText(this, "Error creating image file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        } catch (Exception e) {
            Log.e("PHOTO_DEBUG", "Unexpected error creating image file", e);
            return null;
        }
    }
   /* private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            Log.e("DatabaseBackup", "Error creating image file", e);
            return null;
        }
    }*/

    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            File imageFile = new File(storageDir, imageFileName);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
            outputStream.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e("PHOTO_DEBUG", "Error saving bitmap", e);
            return null;
        }
    }
    private String getRealPathFromUri(Uri uri) {
        String path = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("GALLERY_DEBUG", "Error getting path from URI", e);
        }

        // Fallback: if above fails, try to get the path from the URI directly
        if (path == null) {
            path = uri.getPath();
        }

        Log.d("GALLERY_DEBUG", "URI: " + uri + " -> Path: " + path);
        return path;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("PHOTO_DEBUG", "=== onActivityResult() called ===");
        Log.d("PHOTO_DEBUG", "requestCode: " + requestCode + ", resultCode: " + resultCode);

        //gallery request code REQUEST_PICK_IMAGE
        if (requestCode == REQUEST_PICK_IMAGE) {
            Log.d("GALLERY_DEBUG", "Gallery request detected");

            if (resultCode == RESULT_OK) {
                Log.d("GALLERY_DEBUG", "Gallery selection successful");

                if (data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    Log.d("GALLERY_DEBUG", "Selected URI: " + selectedImageUri);
                    Log.d("GALLERY_DEBUG", "currentWorkoutIdForPhoto: " + currentWorkoutIdForPhoto);

                    handleGallerySelection(selectedImageUri);
                } else {
                    Log.d("GALLERY_DEBUG", "No data or URI in gallery result");
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.d("GALLERY_DEBUG", "User cancelled gallery selection");
            } else {
                Log.d("GALLERY_DEBUG", "Unexpected result code: " + resultCode);
            }
        }


        //camera handling code

        if (requestCode == REQUEST_CAMERAX_CAPTURE && resultCode == RESULT_OK) {
            Log.d("PHOTO_DEBUG", "CameraX photo captured successfully");

            if (data != null) {
                String photoPath = data.getStringExtra("photoPath");
                long workoutId = data.getLongExtra("workoutId", -1);

                Log.d("PHOTO_DEBUG", "Photo path from CameraX: " + photoPath);
                Log.d("PHOTO_DEBUG", "Workout ID from CameraX: " + workoutId);

                if (photoPath != null && workoutId != -1) {
                    // Verify the file was actually created and has content
                    File photoFile = new File(photoPath);
                    Log.d("PHOTO_DEBUG", "Photo file exists: " + photoFile.exists());
                    Log.d("PHOTO_DEBUG", "Photo file size: " + photoFile.length() + " bytes");

                    if (photoFile.exists() && photoFile.length() > 0) {
                        // Save to database
                        boolean success = dbHelper.updateWorkoutPhoto(workoutId, photoPath);
                        Log.d("PHOTO_DEBUG", "Database update success: " + success);

                        if (success) {
                            refreshData();
                            Toast.makeText(this, "High-quality photo saved!", Toast.LENGTH_SHORT).show();

                            // Debug: check photo dimensions
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(photoPath, options);
                            Log.d("PHOTO_DEBUG", "Photo dimensions: " + options.outWidth + "x" + options.outHeight);
                        } else {
                            Toast.makeText(this, "Failed to save photo to database", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("PHOTO_DEBUG", "Photo file doesn't exist or is empty");
                        Toast.makeText(this, "Photo file not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("PHOTO_DEBUG", "Missing photo path or workout ID");
                    Toast.makeText(this, "Error getting photo data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.d("PHOTO_DEBUG", "No data returned from CameraX");
                Toast.makeText(this, "No photo data received", Toast.LENGTH_SHORT).show();
            }
        }

        // Reset selection
        currentWorkoutIdForPhoto = -1;
    }
    private void restoreDatabaseFromBackup(Uri backupUri) {
        try {
            // Get the backup file
            String backupPath = getPathFromUri(backupUri);
            if (backupPath == null) {
                Toast.makeText(this, "Invalid backup file", Toast.LENGTH_SHORT).show();
                return;
            }

            File backupFile = new File(backupPath);
            if (!backupFile.exists()) {
                Toast.makeText(this, "Backup file not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current database file
            File currentDbFile = getDatabasePath(WorkoutDbHelper.DATABASE_NAME);
            File backupDir = currentDbFile.getParentFile();

            // Create a backup of current database first (just in case)
            File tempBackup = new File(backupDir, "temp_backup_" + System.currentTimeMillis() + ".db");
            if (currentDbFile.exists()) {
                copyFile(currentDbFile, tempBackup);
            }

            // Replace current database with the backup
            copyFile(backupFile, currentDbFile);

            Toast.makeText(this, "Database restored successfully!", Toast.LENGTH_LONG).show();

            // Refresh the data
            refreshData();

        } catch (IOException e) {
            Log.e("RESTORE_DEBUG", "Restore failed", e);
            Toast.makeText(this, "Restore failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void checkIfPhotoWasSavedAnyway() {
        if (currentPhotoPath != null) {
            File photoFile = new File(currentPhotoPath);
            if (photoFile.exists() && photoFile.length() > 1000) { // Check if file has reasonable size
                Log.d("PHOTO_DEBUG", "Photo was saved successfully! File size: " + photoFile.length());
                handleCapturedPhoto();
            } else {
                Log.d("PHOTO_DEBUG", "Photo file is empty or too small: " + photoFile.length());
                if (photoFile.exists()) {
                    photoFile.delete(); // Clean up empty file
                }
                Toast.makeText(this, "Photo was not saved properly", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private String getPathFromUri(Uri uri) {
        String path = null;
        try {
            // Try to get the path from MediaStore
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                path = cursor.getString(columnIndex);
                cursor.close();
            }

            // If MediaStore didn't work, try getting the path directly
            if (path == null) {
                path = uri.getPath();
            }

        } catch (Exception e) {
            Log.e("URI_DEBUG", "Error getting path from URI", e);
            path = uri.getPath(); // Fallback
        }

        Log.d("URI_DEBUG", "URI: " + uri + " -> Path: " + path);
        return path;
    }
    private void handleGallerySelection(Uri selectedImageUri) {
        Log.d("PHOTO_DEBUG", "handleGallerySelection called with URI: " + selectedImageUri);

        try {
            String filePath = getPathFromUri(selectedImageUri);
            Log.d("PHOTO_DEBUG", "Extracted file path: " + filePath);

            if (filePath != null && currentWorkoutIdForPhoto != -1) {
                boolean success = dbHelper.updateWorkoutPhoto(currentWorkoutIdForPhoto, filePath);
                Log.d("PHOTO_DEBUG", "Gallery photo database update success: " + success);

                if (success) {
                    refreshData();

                    // Notify gallery about the new file reference
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(filePath);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    sendBroadcast(mediaScanIntent);

                    Toast.makeText(this, "✓ Photo added to workout from gallery", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add photo from gallery", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid file path or no workout selected", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PHOTO_DEBUG", "Error handling gallery selection", e);
            Toast.makeText(this, "Error selecting photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void handleCapturedPhoto() {
        Log.d("FITWOD_DEBUG", "=== handleCapturedPhoto called ===");
        Log.d("FITWOD_DEBUG", "currentPhotoPath: " + currentPhotoPath); // ← FIXED
        Log.d("FITWOD_DEBUG", "currentWorkoutIdForPhoto: " + currentWorkoutIdForPhoto);

        if (currentPhotoPath != null && currentWorkoutIdForPhoto != -1) { // ← FIXED
            // Check if file actually exists before saving to database
            File photoFile = new File(currentPhotoPath); // ← FIXED
            Log.d("FITWOD_DEBUG", "Photo file exists: " + photoFile.exists());
            Log.d("FITWOD_DEBUG", "Photo file size: " + photoFile.length());

            boolean success = dbHelper.updateWorkoutPhoto(currentWorkoutIdForPhoto, currentPhotoPath); // ← FIXED
            Log.d("FITWOD_DEBUG", "Database update success: " + success);

            if (success) {
                // Verify the photo was actually saved to database
                verifyPhotoInDatabase(currentWorkoutIdForPhoto);
                refreshData();
                Toast.makeText(this, "Photo added to workout!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save photo to database", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("FITWOD_DEBUG", "Invalid parameters - cannot save photo");
        }
    }




    private void verifyPhotoInDatabase(long workoutId) {
        Log.d("FITWOD_DEBUG", "=== Verifying photo in database ===");
        Cursor cursor = dbHelper.getWorkoutById(workoutId);
        if (cursor != null && cursor.moveToFirst()) {
            int photoIndex = cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE);
            String savedPhotoPath = cursor.getString(photoIndex);
            Log.d("FITWOD_DEBUG", "Workout ID: " + workoutId);
            Log.d("FITWOD_DEBUG", "Photo path in database: '" + savedPhotoPath + "'");

            if (savedPhotoPath != null && !savedPhotoPath.isEmpty()) {
                File savedFile = new File(savedPhotoPath);
                Log.d("FITWOD_DEBUG", "Saved file exists: " + savedFile.exists());
                Log.d("FITWOD_DEBUG", "Saved file size: " + savedFile.length());
            } else {
                Log.d("FITWOD_DEBUG", "No photo path found in database");
            }
            cursor.close();
        } else {
            Log.d("FITWOD_DEBUG", "Could not find workout in database");
        }
    }

    private void showVoiceNotesSelectionDialog() {
        // Get all workouts that have voice notes
        Cursor cursor = dbHelper.getWorkoutsWithVoiceNotes();

        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No voice notes found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create arrays for dialog
        String[] workoutNames = new String[cursor.getCount()];
        final long[] workoutIds = new long[cursor.getCount()];

        int index = 0;
        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT));

            // Format date
            String formattedDate;
            try {
                long timestamp = Long.parseLong(date);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                formattedDate = sdf.format(new Date(timestamp));
            } catch (NumberFormatException e) {
                formattedDate = date;
            }

            workoutNames[index] = type + " - " + formattedDate;
            workoutIds[index] = cursor.getLong(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID));
            index++;
        }
        cursor.close();

        new AlertDialog.Builder(this)
                .setTitle("Select Voice Note to Play")
                .setItems(workoutNames, (dialog, which) -> {
                    playVoiceNote(workoutIds[which]);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showWorkoutOptionsDialog(int position, long workoutId) {
        WorkoutData workoutData = getWorkoutData(workoutId);
        if (workoutData == null) {
            Toast.makeText(this, "Error loading workout data", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> optionsList = new ArrayList<>();
        optionsList.add("Delete Workout");

        // VoiceNote logic
        if (workoutData.hasVoiceNote) {
            optionsList.add("Play Voice Note");
            optionsList.add("Delete Voice Note");
        } else {
            optionsList.add("Add Voice Note");
        }

        // Photo logic
        if (workoutData.hasPhoto) {
            optionsList.add("Show Photo");
            optionsList.add("Delete Photo");
        } else {
            optionsList.add("Add Photo");
        }

        optionsList.add("View Details");

        showOptionsDialog(position, workoutId, workoutData, optionsList);
    }

   // private void refreshAdapter() {
       // adapter.swapCursor(dbHelper.getAllWorkouts());
   // }


    private WorkoutData getWorkoutData(long workoutId) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getWorkoutById(workoutId);
            if (cursor != null && cursor.moveToFirst()) {
                int photoIndex = cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE);
                int voiceNoteIndex = cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VOICE_NOTE);

                String photoPath = cursor.getString(photoIndex);
                String voiceNotePath = cursor.getString(voiceNoteIndex);

                return new WorkoutData(
                        photoPath != null && !photoPath.isEmpty(),
                        voiceNotePath != null && !voiceNotePath.isEmpty(),
                        photoPath
                );
            }
        } catch (Exception e) {
            Log.e("WORKOUT_DATA", "Error getting workout data", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private void showOptionsDialog(int position, long workoutId, WorkoutData workoutData, List<String> optionsList) {
        String[] options = optionsList.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Workout Options")
                .setIcon(R.drawable.logo)
                .setItems(options, (dialog, which) -> handleOptionSelection(
                        position, workoutId, workoutData, options[which]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleOptionSelection(int position, long workoutId, WorkoutData workoutData, String selectedOption) {
        switch (selectedOption) {
            case "Delete Workout":
                showDeleteDialog(position, workoutId);
                break;
            case "Add Photo":
                currentWorkoutIdForPhoto = workoutId;
                showPhotoOptionsDialog();
                break;
            case "Delete Photo":
                currentWorkoutIdForPhoto = workoutId;
                if (workoutData.photoPath != null && !workoutData.photoPath.isEmpty()) {
                    deletePhoto(workoutId);
                } else {
                    Toast.makeText(this, "No photo available", Toast.LENGTH_SHORT).show();
                }
                break;
            case "Show Photo":
                currentWorkoutIdForPhoto = workoutId;
                if (workoutData.photoPath != null && !workoutData.photoPath.isEmpty()) {
                    openPhoto(workoutData.photoPath);
                } else {
                    Toast.makeText(this, "No photo available", Toast.LENGTH_SHORT).show();
                }
                break;
            case "Add Voice Note":
                currentWorkoutIdForVoice = workoutId;
                startRecording();
                break;
            case "Play Voice Note":
                playVoiceNote(workoutId);
                break;
            case "Delete Voice Note":
                deleteVoiceNote(workoutId);
                break;
            case "View Details":
                viewWorkoutDetails(workoutId);
                break;
        }
    }

    // Helper data class
    private static class WorkoutData {
        boolean hasPhoto;
        boolean hasVoiceNote;
        String photoPath;

        WorkoutData(boolean hasPhoto, boolean hasVoiceNote, String photoPath) {
            this.hasPhoto = hasPhoto;
            this.hasVoiceNote = hasVoiceNote;
            this.photoPath = photoPath;
        }
    }

    private boolean checkCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - need READ_MEDIA_IMAGES instead of storage permissions
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12 and below
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }



     void openPhoto(String photoPath) {
        // Show photo in your own dialog
        if (context == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_photo_view, null);
        ImageView photoView = dialogView.findViewById(R.id.photo_view);

        // Load the photo efficiently
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4; // Reduce memory usage

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        if (bitmap != null) {
            photoView.setImageBitmap(bitmap);
            builder.setView(dialogView)
                    .setTitle("Workout Photo")
                    .setIcon(R.drawable.logo)
                    .setPositiveButton("Close", null)
                    .show();
        } else {
            Toast.makeText(context, "Cannot load photo", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkIfWorkoutHasVoiceNote(long workoutId) {
        Cursor cursor = dbHelper.getWorkoutById(workoutId);
        if (cursor != null && cursor.moveToFirst()) {
            String voicePath = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VOICE_NOTE));
            cursor.close();
            return voicePath != null && !voicePath.isEmpty();
        }
        return false;
    }

    private boolean checkIfWorkoutHasPhoto(long workoutId) {
        Cursor cursor = null;
        try {
            cursor = dbHelper.getWorkoutById(workoutId);
            if (cursor != null && cursor.moveToFirst()) {
                int photoIndex = cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE);
                String photoPath = cursor.getString(photoIndex);
                return photoPath != null && !photoPath.isEmpty();
            }
        } catch (Exception e) {
            Log.e("PHOTO_CHECK", "Error checking photo", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }
    private void showVoiceRecordingOptions() {
        String[] options = {"Record New Voice Note", "Play Existing Voice Note","Stop Recording" ,"Delete Voice Note"};

        new AlertDialog.Builder(this)
                .setTitle("Voice Note Options")
                .setIcon(R.drawable.logo)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Record New
                            startRecording();
                            break;
                        case 1: // Play Existing
                            playVoiceNote(currentWorkoutIdForVoice);
                            break;
                        case 2:
                            stopRecording();
                            break;
                        case 3: // Delete
                            deleteVoiceNote(currentWorkoutIdForVoice);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void startRecording() {
        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 101);
            return;
        }

        try {
            // Create a unique file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "FITWOD_VOICE_" + timeStamp + ".3gp";

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

            // Change button to stop icon
            ImageButton btnRecordVoice = findViewById(R.id.btnRecordVoice);
            btnRecordVoice.setImageResource(R.drawable.ic_stop); // Create this icon

            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e("VOICE_RECORD", "Failed to start recording: " + e.getMessage());
            Toast.makeText(this, "Recording failed to start", Toast.LENGTH_SHORT).show();
        }
    }


    private void stopRecording() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;

                // Change button back to mic icon
                ImageButton btnRecordVoice = findViewById(R.id.btnRecordVoice);
                btnRecordVoice.setImageResource(R.drawable.ic_voice_note);

                // Save voice note to database
                if (currentWorkoutIdForVoice != -1 && voiceNotePath != null) {
                    boolean success = dbHelper.updateWorkoutVoiceNote(currentWorkoutIdForVoice, voiceNotePath);
                    if (success) {
                        Toast.makeText(this, "Voice note saved!", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(this, "Failed to save voice note", Toast.LENGTH_SHORT).show();
                    }
                }

            } catch (Exception e) {
                Log.e("VOICE_RECORD", "Stop recording error: " + e.getMessage());
                Toast.makeText(this, "Recording save failed", Toast.LENGTH_SHORT).show();

                // Reset button anyway
                ImageButton btnRecordVoice = findViewById(R.id.btnRecordVoice);
                btnRecordVoice.setImageResource(R.drawable.ic_mic);
            }
        }
    }



    private void playVoiceNote(long workoutId) {
        // Stop any currently playing audio
        stopCurrentPlayback();

        String voicePath = dbHelper.getWorkoutVoiceNote(workoutId);

        if (voicePath != null && !voicePath.isEmpty()) {
            File voiceFile = new File(voicePath);
            if (voiceFile.exists()) {
                try {
                    currentMediaPlayer = new MediaPlayer();
                    currentMediaPlayer.setDataSource(voicePath);
                    currentMediaPlayer.prepare();
                    currentMediaPlayer.start();

                    Toast.makeText(this, "Playing voice note...", Toast.LENGTH_SHORT).show();

                    currentMediaPlayer.setOnCompletionListener(mp -> {
                        mp.release();
                        currentMediaPlayer = null;
                        Toast.makeText(this, "Playback completed", Toast.LENGTH_SHORT).show();
                    });

                    currentMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        mp.release();
                        currentMediaPlayer = null;
                        Toast.makeText(this, "Playback error", Toast.LENGTH_SHORT).show();
                        return true;
                    });

                } catch (IOException e) {
                    Toast.makeText(this, "Cannot play voice note", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Voice file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No voice note available", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopCurrentPlayback() {
        if (currentMediaPlayer != null) {
            if (currentMediaPlayer.isPlaying()) {
                currentMediaPlayer.stop();
            }
            currentMediaPlayer.release();
            currentMediaPlayer = null;
        }
    }


    private void deleteVoiceNote(long workoutId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Voice Note")
                .setIcon(R.drawable.logo)
                .setMessage("Are you sure you want to delete this voice note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Cursor cursor = dbHelper.getWorkoutById(workoutId);
                    if (cursor != null && cursor.moveToFirst()) {
                        String voicePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOICE_NOTE));
                        cursor.close();

                        // Delete from database
                        boolean success = dbHelper.updateWorkoutVoiceNote(workoutId, null);

                        if (success) {
                            // Delete physical file
                            if (voicePath != null && !voicePath.isEmpty()) {
                                File voiceFile = new File(voicePath);
                                if (voiceFile.exists()) {
                                    voiceFile.delete();
                                }
                            }
                            Toast.makeText(this, "Voice note deleted", Toast.LENGTH_SHORT).show();
                            refreshData();
                        } else {
                            Toast.makeText(this, "Failed to delete voice note", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePhoto(long workoutId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Voice Note")
                .setIcon(R.drawable.logo)
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Cursor cursor = dbHelper.getWorkoutById(workoutId);
                    if (cursor != null && cursor.moveToFirst()) {
                        String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_NOTE));
                        cursor.close();

                        // Delete from database
                        boolean success = dbHelper.updateWorkoutPhoto(workoutId, null);

                        if (success) {
                            // Delete physical file
                            if (photoPath != null && !photoPath.isEmpty()) {
                                File photoFile = new File(photoPath);
                                if (photoFile.exists()) {
                                    photoFile.delete();
                                }
                            }
                            Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
                            refreshData();
                        } else {
                            Toast.makeText(this, "Failed to delete photo", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        //refreshAdapter();

    }



    private void viewWorkoutDetails(long workoutId) {

        // Get workout details from database
        Cursor cursor = dbHelper.getWorkoutById(workoutId);

        if (cursor != null && cursor.moveToFirst()) {
            // Extract workout data
            String type = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE));
            int workTime = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TIME));
            int restTime = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_REST_TIME));
            int rounds = cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ROUNDS));
            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT));
            long timestamp = Long.parseLong(createdAt);
            String createdAtFormated = dbHelper.formatTimestamp(timestamp);// format time using method conversion
            String voiceNote = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOICE_NOTE));
            String photoPath = cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE));

            cursor.close();

            // Create detailed message
            StringBuilder details = new StringBuilder();
            details.append("Workout Type: ").append(type).append("\n\n");
            details.append("Work Time: ").append(workTime).append(" seconds\n");
            details.append("Rest Time: ").append(restTime).append(" seconds\n");
            details.append("Rounds: ").append(rounds).append("\n\n");
            details.append("Date: ").append(createdAtFormated).append("\n\n");

          //  if (notes != null && !notes.isEmpty()) {
            //    details.append("Notes: ").append(notes).append("\n\n");
          //  }

            if (voiceNote != null && !voiceNote.isEmpty()) {
                details.append("Voice Note: Available\n");
            }

            if (photoPath != null && !photoPath.isEmpty()) {
                details.append("Photo: Available");
            }

            // Show details in dialog
            new AlertDialog.Builder(this)
                    .setTitle("Workout Details")
                    .setMessage(details.toString())
                    .setIcon(R.drawable.logo)
                    .setPositiveButton("OK", null)
                    .setNeutralButton("View Photo", (dialog, which) -> {
                        Log.d("PHOTO_DEBUG", "View Photo button clicked");
                        Log.d("PHOTO_DEBUG", "Photo path: " + photoPath);

                        // handleCapturedPhoto();//debug
                        if (photoPath != null && !photoPath.isEmpty()) {
                            // Verify the file actually exists
                            File photoFile = new File(photoPath);
                            Log.d("PHOTO_DEBUG", "Photo file exists: " + photoFile.exists());
                            Log.d("PHOTO_DEBUG", "Photo file size: " + photoFile.length());
                            Log.d("PHOTO_DEBUG", "Photo file can read: " + photoFile.canRead());

                            if (photoFile.exists() && photoFile.length() > 0) {
                                //openPhoto(photoPath);
                                showPhotoDialog(photoPath);
                            } else {
                                Toast.makeText(this, "Photo file not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "No photo available", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        } else {
            Toast.makeText(this, "Workout details not found", Toast.LENGTH_SHORT).show();
        }
    }



    private void viewPhoto(String photoPath) {
        File photoFile = new File(photoPath);
        if (photoFile.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri photoUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", photoFile);
            intent.setDataAndType(photoUri, "image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Photo file not found", Toast.LENGTH_SHORT).show();
        }
    }
    // Add this method to your Activity class
    private void showPhotoDialog(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            Toast.makeText(this, "No photo available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verify file exists
        File photoFile = new File(photoPath);
        if (!photoFile.exists() || photoFile.length() == 0) {
            Toast.makeText(this, "Photo file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_photo_view, null);
            ImageView photoView = dialogView.findViewById(R.id.photo_view);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
            if (bitmap != null) {
                photoView.setImageBitmap(bitmap);
                builder.setView(dialogView)
                        .setTitle("Workout Photo")
                        .setIcon(R.drawable.logo)
                        .setPositiveButton("Close", null)
                        .show();
            } else {
                Toast.makeText(this, "Cannot load photo", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading photo", Toast.LENGTH_SHORT).show();
        }
    }
    private void isolateAllFilterButtons() {
        // Map of button IDs to their colors
        Map<Integer, Integer> buttonColors = new HashMap<>();
        buttonColors.put(R.id.btnFilterAll, R.color.teal_700);
        buttonColors.put(R.id.btnFilterTabata, R.color.orange);
        buttonColors.put(R.id.btnFilterForTime, R.color.blue);
        buttonColors.put(R.id.btnFilterEmom, R.color.max_green);
        buttonColors.put(R.id.btnFilterAmrap, R.color.blue1);

        for (Map.Entry<Integer, Integer> entry : buttonColors.entrySet()) {
            Button button = findViewById(entry.getKey());
            if (button != null) {
                // Remove all theme influences
                button.setBackgroundTintList(null);
                button.setBackgroundTintMode(null);

                // Get the button's color
                int color = ContextCompat.getColor(this, entry.getValue());

                // Apply the appropriate shape based on selection state
                if (button.isSelected()) {
                    applyOvalShape(button, color);
                } else {
                    applyRectangleShape(button, color);
                }
            }
        }
    }

    private void applyOvalShape(Button button, int color) {
        GradientDrawable oval = new GradientDrawable();
        oval.setShape(GradientDrawable.OVAL);
        oval.setColor(color);
        // Set size for oval to look nice
        int sizeInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 36, getResources().getDisplayMetrics()
        );
        oval.setSize(sizeInPx, sizeInPx);
        button.setBackground(oval);
    }

    private void applyRectangleShape(Button button, int color) {
        GradientDrawable rect = new GradientDrawable();
        rect.setShape(GradientDrawable.RECTANGLE);
        rect.setCornerRadius(16f); // Rounded corners
        rect.setColor(color);
        button.setBackground(rect);
    }


    private void setDatePickerTextColor(DatePicker datePicker, int color) {
        // For spinner mode DatePicker
        try {
            // Get the day, month, and year NumberPickers
            Field[] fields = datePicker.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("mYearSpinner") ||
                        field.getName().equals("mMonthSpinner") ||
                        field.getName().equals("mDaySpinner")) {
                    field.setAccessible(true);
                    NumberPicker spinner = (NumberPicker) field.get(datePicker);
                    if (spinner != null) {
                        for (int i = 0; i < spinner.getChildCount(); i++) {
                            View child = spinner.getChildAt(i);
                            if (child instanceof EditText) {
                                ((EditText) child).setTextColor(color);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Get workout parameters from intent
// Add this method to show the date picker
    private void showCustomDatePickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_date_picker);

        DatePicker datePicker = dialog.findViewById(R.id.custom_date_picker);
        Button okButton = dialog.findViewById(R.id.btn_ok);
        Button cancelButton = dialog.findViewById(R.id.btn_cancel);

        // Set current date
        datePicker.init(selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH),
                null);

        // Programmatically change font colors
        setDatePickerTextColor(datePicker, Color.BLACK);
        okButton.setTextColor(Color.BLUE);
        cancelButton.setTextColor(Color.BLUE);

        okButton.setOnClickListener(v -> {
            String formattedDate = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d",
                    datePicker.getYear(),
                    datePicker.getMonth() + 1,
                    datePicker.getDayOfMonth());

            Log.d("DATE_PICKER", "Selected date: " + formattedDate);
            searchView.setQuery(formattedDate, true);

            // Simple check - if no results after 1 second, show message
            new Handler().postDelayed(() -> {
                // Replace 'yourAdapter' with your actual adapter variable name
                if (adapter.getItemCount() == 0) { // Use your actual adapter instance name
                    Toast.makeText(this, "No workouts found for " + formattedDate, Toast.LENGTH_LONG).show();
                }
            }, 1000);

            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Set dialog size
        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.8);
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
    }
    /*private void showCustomDatePickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_date_picker);

        DatePicker datePicker = dialog.findViewById(R.id.custom_date_picker);
        Button okButton = dialog.findViewById(R.id.btn_ok);
        Button cancelButton = dialog.findViewById(R.id.btn_cancel);

        // Set current date
        datePicker.init(selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH),
                null);

        // Programmatically change font colors
        setDatePickerTextColor(datePicker, Color.BLACK);
        okButton.setTextColor(Color.BLUE);
        cancelButton.setTextColor(Color.BLUE);


        okButton.setOnClickListener(v -> {
            String formattedDate = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d",
                    datePicker.getYear(),
                    datePicker.getMonth() + 1,
                    datePicker.getDayOfMonth());

            Log.d("DATE_PICKER", "Selected date: " + formattedDate);
            searchView.setQuery(formattedDate, true);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Set dialog size to half of screen
        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.8);
            window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
    }*/


    private void setupSearchView() {
        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterWorkouts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterWorkouts(newText);
                return false;
            }
        });
    }
    // Add this method to your DB helper


    private void debugTabataButton() {
        Button tabataButton = findViewById(R.id.btnFilterTabata);

        Log.d("ButtonDebug", "Tabata Button properties:");
        Log.d("ButtonDebug", " - Selected: " + tabataButton.isSelected());
        Log.d("ButtonDebug", " - Background: " + tabataButton.getBackground());
        Log.d("ButtonDebug", " - Background tint: " + tabataButton.getBackgroundTintList());

        // Test the shape change programmatically
        tabataButton.postDelayed(() -> {
            Log.d("ButtonDebug", "Testing selection...");
            tabataButton.setSelected(true);
            Log.d("ButtonDebug", "Now selected: " + tabataButton.isSelected());
        }, 1000);
    }

private void filterWorkouts(String query) {
    Log.d("FILTER_DEBUG", "Filtering with query: '" + query + "'");

    Cursor filteredCursor = dbHelper.searchWorkoutsByDate(query);

    if (filteredCursor != null) {
        Log.d("FILTER_DEBUG", "Total results: " + filteredCursor.getCount());

        // Show toast only for date searches that return no results
        if (filteredCursor.getCount() == 0 && isDateQuery(query)) {
            Toast.makeText(this, "No workouts on " + formatDateForDisplay(query), Toast.LENGTH_SHORT).show();
        }

        // Update your adapter
        adapter.swapCursor(filteredCursor);

        // Show/hide empty view
        if (filteredCursor.getCount() == 0) {
            findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            findViewById(R.id.emptyView).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

    } else {
        Toast.makeText(this, "Search error", Toast.LENGTH_SHORT).show();
    }
}

    private boolean isDateQuery(String query) {
        // Check if query matches date format (YYYY-MM-DD)
        return query.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    private String formatDateForDisplay(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString; // Return original if parsing fails
        }
    }


    private void goback_to_mainMenu() {
        startActivity(new Intent(item_workout_history.this, Menu_Activity.class));
    }
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1);
            }
        }
    }


    private void showDeleteDialog(int position, long workoutId) {
        //if (context == null) return;
        String delete_Msg = getString(R.string.delete_msg); // values/strings.xml
        String dlt_msg_title = getString(R.string.delete_title); // values/strings.xml
        String dlt = getString(R.string.delete); // values/strings.xml
        String cancel = getString(R.string.cancel);
        new AlertDialog.Builder(this)
                .setTitle(dlt_msg_title)
                .setMessage(delete_Msg)
                .setPositiveButton(dlt, (dialog, which) -> {
                    deleteWorkout(position, workoutId);
                })
                .setNegativeButton(cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

   private View currentlySelectedButton = null;

    private void setupFilterButtons() {
        // Map of button IDs to their colors and types
        Map<Integer, Object[]> buttonInfo = new HashMap<>();
        buttonInfo.put(R.id.btnFilterAll, new Object[]{R.color.teal_700, ""});
        buttonInfo.put(R.id.btnFilterTabata, new Object[]{R.color.orange, "TABATA"});
        buttonInfo.put(R.id.btnFilterForTime, new Object[]{R.color.blue, "FOR TIME"});
        buttonInfo.put(R.id.btnFilterEmom, new Object[]{R.color.max_green, "EMOM"});
        buttonInfo.put(R.id.btnFilterAmrap, new Object[]{R.color.blue1, "AMRAP"});

        for (Map.Entry<Integer, Object[]> entry : buttonInfo.entrySet()) {
            Button button = findViewById(entry.getKey());
            int colorRes = (Integer) entry.getValue()[0];
            String filterType = (String) entry.getValue()[1];

            int color = ContextCompat.getColor(this, colorRes);

            // Apply initial shape
            applyRectangleShape(button, color);

            button.setOnClickListener(v -> {
                // Reset all buttons first
                // First clear any existing search (like date search)
                searchView.setQuery("", false); // false = don't trigger search
                resetAllButtons(buttonInfo);

                // Set current button as selected
                v.setSelected(true);
                currentlySelectedButton = (Button) v;

                // Apply oval shape to selected button
                applyOvalShape((Button) v, color);

                // Perform filtering
                filterByType(filterType);
            });
        }
    }

    private void resetAllButtons(Map<Integer, Object[]> buttonInfo) {
        for (Map.Entry<Integer, Object[]> entry : buttonInfo.entrySet()) {
            Button button = findViewById(entry.getKey());
            if (button != null) {
                button.setSelected(false);
                int colorRes = (Integer) entry.getValue()[0];
                int color = ContextCompat.getColor(this, colorRes);
                applyRectangleShape(button, color);
            }
        }
        currentlySelectedButton = null;
    }

    private void filterByType(String type) {
        Log.d("FilterDebug","Filtering by type" + type + "");
        Cursor filteredCursor;
        if (type.isEmpty()) {
            filteredCursor = dbHelper.getAllWorkouts();
        } else {
            filteredCursor = dbHelper.getWorkoutsByType(type);
        }
        adapter.swapCursor(filteredCursor);
    }

    private void deleteWorkout(int position, long workoutId) {
    WorkoutDbHelper dbHelper = new WorkoutDbHelper(this);
    String workout_deleted = getString(R.string.workout_deleted);
    String failed_to_delete_workout = getString(R.string.failed_to_delete_workout);

    // First get the workout details to check for associated files
    Cursor cursor = dbHelper.getWorkoutById(workoutId);
    String voicePath = null;

    if (cursor != null && cursor.moveToFirst()) {
        int voiceIndex = cursor.getColumnIndex(COLUMN_VOICE_NOTE);

        if (voiceIndex != -1) voicePath = cursor.getString(voiceIndex);

        cursor.close();
    }

    // Delete the database record
    boolean deleted = dbHelper.deleteWorkout(workoutId);

    if (deleted) {
        // Delete associated files
        if (voicePath != null && !voicePath.isEmpty()) {
            File voiceFile = new File(voicePath);
            if (voiceFile.exists()) voiceFile.delete();
        }

        // Refresh the data
        refreshData();
        Toast.makeText(this, workout_deleted, Toast.LENGTH_SHORT).show();
    } else {
        Toast.makeText(this, failed_to_delete_workout, Toast.LENGTH_SHORT).show();
    }
}
    private void refreshData() {
        Log.d("FITWOD_DEBUG", "=== refreshData called ===");
        Cursor newCursor = dbHelper.getAllWorkouts();

        if (newCursor != null) {
            Log.d("FITWOD_DEBUG", "New cursor count: " + newCursor.getCount());

            // Check if any workouts have photos
            boolean hasPhotos = false;
            if (newCursor.moveToFirst()) {
                do {
                    int photoIndex = newCursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE);
                    String photoPath = newCursor.getString(photoIndex);
                    if (photoPath != null && !photoPath.isEmpty()) {
                        hasPhotos = true;
                        Log.d("FITWOD_DEBUG", "Found photo for workout: " + photoPath);
                    }
                } while (newCursor.moveToNext());
            }

            Log.d("FITWOD_DEBUG", "Workouts with photos: " + hasPhotos);
            adapter.swapCursor(newCursor);
        } else {
            Log.d("FITWOD_DEBUG", "New cursor is null!");
        }
    }



    private void loadAllWorkouts() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void... voids) {

                try {
                    return dbHelper.getAllWorkouts();
                } catch (Exception e) {
                    Log.e("DB_ERROR", "Failed to load workouts", e);
                    return null;
                }

            }
            @Override
            protected void onPostExecute(Cursor cursor) {
                adapter.swapCursor(cursor);
                if (cursor == null || cursor.getCount() == 0) {
                    findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    findViewById(R.id.emptyView).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    @Override
    protected void onDestroy() {
        if (voiceRecorder != null && isRecording) {
      //      voiceRecorder.stopRecording();
        }
        dbHelper.close();
        super.onDestroy();
    }
}