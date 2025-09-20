package com.bessadi.fitwod;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaHelper {
    private static final String AUDIO_DIR = "workout_audio";
    private static final String VIDEO_DIR = "workout_video";

    public static File getOutputAudioFile(Context context) throws IOException {
        File mediaDir = new File(context.getExternalFilesDir(null), AUDIO_DIR);
        if (!mediaDir.exists()) mediaDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        return new File(mediaDir.getPath() + File.separator +
                "AUDIO_" + timeStamp + ".3gp");
    }

    public static File getOutputVideoFile(Context context) throws IOException {
        File mediaDir = new File(context.getExternalFilesDir(null), VIDEO_DIR);
        if (!mediaDir.exists()) mediaDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        return new File(mediaDir.getPath() + File.separator +
                "VIDEO_" + timeStamp + ".mp4");
    }
}
