package com.bessadi.fitwod;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    // Resource tracking to prevent leaks
    private List<Closeable> resources = new ArrayList<>();
    private List<Cursor> cursors = new ArrayList<>();
    private List<Bitmap> bitmaps = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        String language = LanguageManager.getLanguage(newBase);
        Context context = LanguageManager.setLocale(newBase, language);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Reset title to ensure it gets translated
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), 0).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Start resource monitoring
        startResourceMonitoring();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update UI if language was changed while activity was in background
        String currentLanguage = LanguageManager.getLanguage(this);
        if (!currentLanguage.equals(getResources().getConfiguration().getLocales().get(0).getLanguage())) {
            recreate();
        }
    }

    // ============ RESOURCE MANAGEMENT METHODS ============

    /**
     * Track a Closeable resource for automatic cleanup
     */
    protected void trackResource(Closeable resource) {
        if (resource != null) {
            resources.add(resource);
        }
    }

    /**
     * Track a Cursor for automatic closing
     */
    protected void trackCursor(Cursor cursor) {
        if (cursor != null) {
            cursors.add(cursor);
        }
    }

    /**
     * Track a Bitmap for automatic recycling
     */
    protected void trackBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmaps.add(bitmap);
        }
    }

    /**
     * Manually cleanup a specific cursor
     */
    protected void safeCloseCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
            cursors.remove(cursor);
        }
    }

    /**
     * Manually cleanup a specific bitmap
     */
    protected void safeRecycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmaps.remove(bitmap);
        }
    }

    /**
     * Monitor resource usage to detect leaks early
     */
    private void startResourceMonitoring() {
        final Handler handler = new Handler();
        final Runnable monitorRunnable = new Runnable() {
            @Override
            public void run() {
                logResourceUsage();
                handler.postDelayed(this, 30000); // Check every 30 seconds
            }
        };
        handler.postDelayed(monitorRunnable, 30000);
    }

    /**
     * Log current resource usage for debugging
     */
    protected void logResourceUsage() {
        // Check file descriptor count
        File fdDir = new File("/proc/self/fd");
        if (fdDir.exists() && fdDir.listFiles() != null) {
            int fdCount = fdDir.listFiles().length;
            Log.d("ResourceMonitor",
                    "File descriptors: " + fdCount +
                            ", Tracked cursors: " + cursors.size() +
                            ", Tracked bitmaps: " + bitmaps.size());

            if (fdCount > 100) {
                Log.w("ResourceMonitor", "High file descriptor usage: " + fdCount);
            }
        }
    }

    /**
     * Cleanup all tracked resources
     */
    private void cleanupResources() {
        Log.d("BaseActivity", "Cleaning up resources: " +
                resources.size() + " resources, " +
                cursors.size() + " cursors, " +
                bitmaps.size() + " bitmaps");

        // Close all Closeable resources
        for (Closeable resource : resources) {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (IOException e) {
                Log.e("ResourceCleanup", "Error closing resource", e);
            }
        }
        resources.clear();

        // Close all cursors
        for (Cursor cursor : cursors) {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        cursors.clear();

        // Recycle bitmaps
        for (Bitmap bitmap : bitmaps) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        bitmaps.clear();
    }

    @Override
    protected void onDestroy() {
        // Cleanup all resources when activity is destroyed
        cleanupResources();
        super.onDestroy();
    }
}