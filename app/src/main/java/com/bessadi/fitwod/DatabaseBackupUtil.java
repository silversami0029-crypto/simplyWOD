package com.bessadi.fitwod;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseBackupUtil {
    private static final String TAG = "DatabaseBackupUtil";

    public static boolean backupDatabaseWithPhotos(Context context, WorkoutDbHelper dbHelper) {
        try {
            // Backup database first
            boolean dbBackupSuccess = backupDatabase(context);

            if (!dbBackupSuccess) {
                return false;
            }

            // Backup photos
            File backupDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "FitWodBackups");

            File photosBackupDir = new File(backupDir, "photos_backup");
            if (!photosBackupDir.exists()) {
                photosBackupDir.mkdirs();
            }

            // Get all workouts with photos
            Cursor cursor = dbHelper.getAllWorkouts();
            int photoCount = 0;

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String photoPath = cursor.getString(
                            cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE));// USED IT TO STORE PHOTOS cos the column was already there.

                    if (photoPath != null && !photoPath.isEmpty()) {
                        File sourceFile = new File(photoPath);
                        if (sourceFile.exists()) {
                            File destFile = new File(photosBackupDir, sourceFile.getName());
                            copyFile(sourceFile, destFile);
                            photoCount++;
                        }
                    }
                } while (cursor.moveToNext());

                cursor.close();
            }

            Toast.makeText(context, "Backup complete! " + photoCount + " photos backed up",
                    Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Backup with photos failed", e);
            Toast.makeText(context, "Backup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    private static void copyFile(File source, File dest) throws IOException {
        FileInputStream inStream = new FileInputStream(source);
        FileOutputStream outStream = new FileOutputStream(dest);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();

        inChannel.transferTo(0, inChannel.size(), outChannel);

        inStream.close();
        outStream.close();
    }
    public static boolean exportToExcel(Context context, WorkoutDbHelper dbHelper) {
        // For simplicity, we'll create a CSV that can be opened in Excel
        return exportToCSV(context, dbHelper);
    }
    public static boolean exportToJSON(Context context, WorkoutDbHelper dbHelper) {
        try {
            File exportDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "FitWodExports");

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File exportFile = new File(exportDir, "workout_logs_" + timeStamp + ".json");

            Cursor cursor = dbHelper.getAllWorkoutsForExport();
            FileOutputStream fos = new FileOutputStream(exportFile);

            fos.write("[\n".getBytes());

            if (cursor != null && cursor.moveToFirst()) {
                boolean first = true;
                do {
                    if (!first) {
                        fos.write(",\n".getBytes());
                    }
                    first = false;

                    String json = String.format(Locale.getDefault(),
                            "  {\n" +
                                    "    \"id\": %d,\n" +
                                    "    \"type\": \"%s\",\n" +
                                    "    \"work_time\": %d,\n" +
                                    "    \"rest_time\": %d,\n" +
                                    "    \"rounds\": %d,\n" +
                                    "    \"created_at\": \"%s\",\n" +
                                    "    \"notes\": \"%s\",\n" +
                                    "    \"voice_note\": \"%s\",\n" +
                                    "    \"photo_path\": \"%s\"\n" +
                                    "  }",
                            cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID)),
                            escapeJsonString(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE))),
                            cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TIME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_REST_TIME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ROUNDS)),
                            escapeJsonString(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT))),
                           // escapeJsonString(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_NOTES))),
                            escapeJsonString(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VOICE_NOTE))),
                            escapeJsonString(cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE)))
                    );
                    fos.write(json.getBytes());

                } while (cursor.moveToNext());

                cursor.close();
            }

            fos.write("\n]".getBytes());
            fos.close();

            shareFile(context, exportFile, "application/json", "Export Workout Logs (JSON)");
            return true;

        } catch (IOException e) {
            Log.e(TAG, "JSON export failed", e);
            Toast.makeText(context, "JSON export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private static String escapeJsonString(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static boolean backupDatabase(Context context) {
        try {
            File dbFile = context.getDatabasePath(WorkoutDbHelper.DATABASE_NAME);
            File backupDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "FitWodBackups");

            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File backupFile = new File(backupDir, "fitwod_backup_" + timeStamp + ".db");

            FileInputStream inStream = new FileInputStream(dbFile);
            FileOutputStream outStream = new FileOutputStream(backupFile);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();

            inChannel.transferTo(0, inChannel.size(), outChannel);

            inStream.close();
            outStream.close();

            Toast.makeText(context, "Backup created: " + backupFile.getName(), Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Backup failed", e);
            Toast.makeText(context, "Backup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean exportToCSV(Context context, WorkoutDbHelper dbHelper) {
        try {
            File exportDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "FitWodExports");

            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            File exportFile = new File(exportDir, "workout_logs_" + timeStamp + ".csv");

            Cursor cursor = dbHelper.getAllWorkoutsForExport();
            FileOutputStream fos = new FileOutputStream(exportFile);

            // Write CSV header
            String header = "ID,Type,Work Time,Rest Time,Rounds,Created At,Notes,Voice Note,Photo Path\n";
            fos.write(header.getBytes());

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String row = String.format(Locale.getDefault(),
                            "%d,%s,%d,%d,%d,%s,%s,%s,%s\n",
                            cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TYPE)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_WORK_TIME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_REST_TIME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_ROUNDS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_CREATED_AT)),
                          //  cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_NOTES)),
                            cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VOICE_NOTE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(WorkoutDbHelper.COLUMN_VIDEO_NOTE))
                    );
                    fos.write(row.getBytes());
                } while (cursor.moveToNext());

                cursor.close();
            }

            fos.close();

            // Share the file
            shareFile(context, exportFile, "text/csv", "Export Workout Logs");
            return true;

        } catch (IOException e) {
            Log.e(TAG, "Export failed", e);
            Toast.makeText(context, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private static void shareFile(Context context, File file, String mimeType, String title) {
        Uri contentUri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".provider", file);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(shareIntent, title));
    }
}