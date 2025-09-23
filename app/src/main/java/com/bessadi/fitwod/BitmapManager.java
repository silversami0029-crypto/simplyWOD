package com.bessadi.fitwod;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class BitmapManager {
    private static final int MAX_MEMORY_CACHE = 1024 * 1024 * 10; // 10MB
    private LruCache<String, Bitmap> memoryCache;
    private Set<Bitmap> trackedBitmaps = Collections.newSetFromMap(new WeakHashMap<Bitmap, Boolean>());

    private static BitmapManager instance;

    private BitmapManager() {
        setupMemoryCache();
    }

    public static synchronized BitmapManager getInstance() {
        if (instance == null) {
            instance = new BitmapManager();
        }
        return instance;
    }

    private void setupMemoryCache() {
        memoryCache = new LruCache<String, Bitmap>(MAX_MEMORY_CACHE) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }

            @Override
            protected void entryRemoved(boolean evicted, String key,
                                        Bitmap oldValue, Bitmap newValue) {
                if (evicted && oldValue != null && !oldValue.isRecycled()) {
                    oldValue.recycle();
                }
            }
        };
    }

    public Bitmap loadBitmap(Context context, int resId) {
        String key = String.valueOf(resId);
        Bitmap bitmap = memoryCache.get(key);

        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = decodeSampledBitmapFromResource(context.getResources(), resId, 100, 100);
            if (bitmap != null) {
                memoryCache.put(key, bitmap);
                trackedBitmaps.add(bitmap);
            }
        }

        return bitmap;
    }

    public Bitmap loadBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        String key = filePath + "_" + reqWidth + "x" + reqHeight;
        Bitmap bitmap = memoryCache.get(key);

        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = decodeSampledBitmapFromFile(filePath, reqWidth, reqHeight);
            if (bitmap != null) {
                memoryCache.put(key, bitmap);
                trackedBitmaps.add(bitmap);
            }
        }

        return bitmap;
    }

    private Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                   int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inMutable = false;

        return BitmapFactory.decodeResource(res, resId, options);
    }

    private Bitmap decodeSampledBitmapFromFile(String filePath,
                                               int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inMutable = false;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            return BitmapFactory.decodeStream(fis, null, options);
        } catch (IOException e) {
            Log.e("BitmapManager", "Error loading bitmap from file", e);
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            trackedBitmaps.remove(bitmap);
        }
    }

    public void clearCache() {
        memoryCache.evictAll();
        for (Bitmap bitmap : trackedBitmaps) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        trackedBitmaps.clear();
    }

    public int getCacheSize() {
        return memoryCache.size();
    }

    public int getTrackedBitmapCount() {
        return trackedBitmaps.size();
    }
}
