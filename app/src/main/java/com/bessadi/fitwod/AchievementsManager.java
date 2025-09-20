package com.bessadi.fitwod;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import com.bessadi.fitwod.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class AchievementsManager {
    private Context context;
    private SharedPreferences prefs;
    private WorkoutDbHelper dbHelper;

    public AchievementsManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("achievements_prefs", Context.MODE_PRIVATE);
        this.dbHelper = new WorkoutDbHelper(context);
    }

    public List<Achievement> getAchievements() {
        List<Achievement> achievements = new ArrayList<>();

        // Get actual user data from database
        int totalWorkouts = dbHelper.getWorkoutCount();
        int workoutsWithPhotos = dbHelper.getWorkoutsWithPhotoCount();
        int workoutsWithVoice = dbHelper.getWorkoutsWithVoiceCount();
        int consecutiveDays = calculateConsecutiveDays();
        int uniqueWorkoutTypes = dbHelper.getUniqueWorkoutTypesCount();

        // Dynamic achievements based on actual progress
        achievements.add(createAchievement(1, "First Steps", "Complete your first workout",
                totalWorkouts >= 1, R.drawable.ic_trophy_bronze,
                totalWorkouts, 1));  // currentProgress, targetProgress

        achievements.add(createAchievement(2, "Dedicated", "Complete 5 workouts",
                totalWorkouts >= 5, R.drawable.ic_trophy_silver,
                totalWorkouts, 5));

        achievements.add(createAchievement(3, "Fitness Guru", "Complete 20 workouts",
                totalWorkouts >= 20, R.drawable.ic_trophy_gold,
                totalWorkouts, 20));

        achievements.add(createAchievement(4, "Photogenic", "Add photos to 3 workouts",
                workoutsWithPhotos >= 3, R.drawable.ic_photo,
                workoutsWithPhotos, 3));

        achievements.add(createAchievement(5, "Storyteller", "Add voice notes to 2 workouts",
                workoutsWithVoice >= 2, R.drawable.ic_mic,
                workoutsWithVoice, 2));

        achievements.add(createAchievement(6, "Consistency", "Workout 3 days in a row",
                consecutiveDays >= 3, R.drawable.ic_consistency,
                consecutiveDays, 3));

        achievements.add(createAchievement(7, "Variety Seeker", "Try 3 different workout types",
                uniqueWorkoutTypes >= 3, R.drawable.ic_variety,
                uniqueWorkoutTypes, 3));


        return achievements;
    }

    private Achievement createAchievement(int id, String title, String description,
                                          boolean unlocked, int iconResId,int currentProgress, int targetProgress) {
        Achievement achievement = new Achievement(id, title, description, iconResId,currentProgress, targetProgress);
        achievement.setUnlocked(unlocked);

        // Load unlock date from shared preferences if unlocked
        if (unlocked) {
            long unlockTime = prefs.getLong("achievement_unlock_" + id, 0);
            if (unlockTime > 0) {
                achievement.setUnlockedDate(new Date(unlockTime));
            }
        }

        return achievement;
    }

    private int calculateConsecutiveDays() {
        Cursor cursor = dbHelper.getWorkoutDates();
        int consecutiveDays = 0;

        if (cursor != null && cursor.moveToFirst()) {
            // This would require more complex logic to calculate consecutive days
            // For simplicity, we'll return a placeholder
            consecutiveDays = 1; // Implement proper consecutive day calculation
            cursor.close();
        }

        return consecutiveDays;
    }

    private boolean hasTriedMultipleWorkoutTypes(int requiredTypes) {
        Cursor cursor = dbHelper.getUniqueWorkoutTypes();
        if (cursor != null) {
            int typeCount = cursor.getCount();
            cursor.close();
            return typeCount >= requiredTypes;
        }
        return false;
    }

    public void checkAndUnlockAchievements() {
        List<Achievement> achievements = getAchievements();
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked() &&
                    !prefs.getBoolean("achievement_" + achievement.getId(), false)) {

                // New achievement unlocked!
                prefs.edit()
                        .putBoolean("achievement_" + achievement.getId(), true)
                        .putLong("achievement_unlock_" + achievement.getId(), System.currentTimeMillis())
                        .apply();

                // Notify activity to show celebration
                if (context instanceof AchievementsListener) {
                    ((AchievementsListener) context).onAchievementUnlocked(achievement);
                }
            }
        }
    }
}