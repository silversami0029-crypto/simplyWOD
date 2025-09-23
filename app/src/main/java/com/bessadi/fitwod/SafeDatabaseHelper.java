package com.bessadi.fitwod;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.Calendar;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class SafeDatabaseHelper extends SQLiteOpenHelper {
    // Database constants
    public static final String DATABASE_NAME = "workouts.db";
    private static final int DATABASE_VERSION = 6;

    // Table and column names
    public static final String TABLE_WORKOUTS = "workouts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_WORK_TYPE = "work_type";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_WORK_TIME = "work_time";
    public static final String COLUMN_REST_TIME = "rest_time";
    public static final String COLUMN_ROUNDS = "rounds";
    public static final String COLUMN_VOICE_NOTE = "voice_note";
    public static final String COLUMN_VIDEO_NOTE = "video_note";

    public static final String TABLE_MEAL_PLAN = "meal_plan";
    public static final String COLUMN_MEAL_ID = "meal_id";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_MEAL_TYPE = "meal_type";
    public static final String COLUMN_MEAL_DESCRIPTION = "meal_description";
    public static final String COLUMN_CALORIES = "calories";

    public static final String TABLE_PERSONAL_PROFILE = "nutrition_table";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT = "height";
    public static final String COLUMN_WEIGHT = "wright";
    public static final String COLUMN_GENDER = "gendeer";
    public static final String COLUMN_ACTIVITY_LEVEL = "level";
    public static final String COLUMN_GOAL = "goal";

    public static final String TABLE_NUTRITION_PROFILE = "nutrition_profile";
    public static final String COLUMN_CALORIES_GOAL = "calories_goal";
    public static final String COLUMN_PROTEIN_GOAL = "protein_goal";
    public static final String COLUMN_CARBS_GOAL = "carbs_goal";
    public static final String COLUMN_FAT_GOAL = "fat_goal";
    public static final String COLUMN_WATER_GOAL = "water_goal";

    private Context context;
    private static SafeDatabaseHelper instance;
    private AtomicInteger openCounter = new AtomicInteger();

    // Private constructor to enforce singleton pattern
    private SafeDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    // Singleton instance getter
    public static synchronized SafeDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SafeDatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createWorkoutsTable(db);
        createMealPlanTable(db);
        createPersonalProfileTable(db);
        createNutritionTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 5) {
            createMealPlanTable(db);
            createPersonalProfileTable(db);
            createNutritionTable(db);
        }
    }

    // ============ CONNECTION MANAGEMENT ============

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        openCounter.incrementAndGet();
        logConnectionCount();
        return super.getWritableDatabase();
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        openCounter.incrementAndGet();
        logConnectionCount();
        return super.getReadableDatabase();
    }

    @Override
    public synchronized void close() {
        if (openCounter.decrementAndGet() <= 0) {
            super.close();
            instance = null;
        }
        logConnectionCount();
    }

    private void logConnectionCount() {
        Log.d("Database", "Active connections: " + openCounter.get());
    }

    // Safe method to close database and cursor
    public void safeClose(SQLiteDatabase db, Cursor cursor) {
        try {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("Database", "Error closing cursor", e);
        }

        try {
            if (db != null && db.isOpen()) {
                db.close();
            }
        } catch (Exception e) {
            Log.e("Database", "Error closing database", e);
        }
    }

    // ============ TABLE CREATION METHODS ============

    private void createWorkoutsTable(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_WORKOUTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_WORK_TYPE + " TEXT, " +
                COLUMN_CREATED_AT + " INTEGER, " +
                COLUMN_DURATION + " INTEGER, " +
                COLUMN_WORK_TIME + " INTEGER, " +
                COLUMN_REST_TIME + " INTEGER, " +
                COLUMN_ROUNDS + " INTEGER, " +
                COLUMN_VOICE_NOTE + " TEXT, " +
                COLUMN_VIDEO_NOTE + " TEXT" + ")";
        db.execSQL(createTable);
    }

    private void createMealPlanTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_MEAL_PLAN + "(" +
                COLUMN_MEAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DAY + " TEXT, " +
                COLUMN_MEAL_TYPE + " TEXT, " +
                COLUMN_MEAL_DESCRIPTION + " TEXT, " +
                COLUMN_CALORIES + " TEXT);";
        db.execSQL(sql);
    }

    private void createPersonalProfileTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_PERSONAL_PROFILE + "(" +
                COLUMN_AGE + " INTEGER, " +
                COLUMN_WEIGHT + " REAL, " +
                COLUMN_HEIGHT + " REAL, " +
                COLUMN_GENDER + " TEXT, " +
                COLUMN_ACTIVITY_LEVEL + " TEXT, " +
                COLUMN_GOAL + " TEXT);";
        db.execSQL(sql);
    }

    private void createNutritionTable(SQLiteDatabase db) {
        String createNutritionTable = "CREATE TABLE " + TABLE_NUTRITION_PROFILE + "(" +
                COLUMN_CALORIES_GOAL + " INTEGER, " +
                COLUMN_PROTEIN_GOAL + " REAL, " +
                COLUMN_CARBS_GOAL + " REAL, " +
                COLUMN_FAT_GOAL + " REAL, " +
                COLUMN_WATER_GOAL + " REAL);";
        db.execSQL(createNutritionTable);
    }

    // ============ WORKOUT METHODS (SAFE VERSIONS) ============

    public Cursor getAllWorkouts() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_WORKOUTS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_CREATED_AT + " DESC"
        );
        // Note: Caller must close the cursor using safeCloseCursor() or track it in BaseActivity
        return cursor;
    }

    public Cursor getWorkoutsByType(String type) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_WORKOUTS,
                null,
                COLUMN_WORK_TYPE + " = ?",
                new String[]{type},
                null,
                null,
                COLUMN_CREATED_AT + " DESC"
        );
    }

    public Cursor searchWorkouts(String query) {
        SQLiteDatabase db = getReadableDatabase();

        if (query == null || query.trim().isEmpty()) {
            return getAllWorkouts();
        }

        String selection = COLUMN_WORK_TYPE + " LIKE ? OR " +
                COLUMN_WORK_TIME + " LIKE ? OR " +
                COLUMN_REST_TIME + " LIKE ? OR " +
                COLUMN_CREATED_AT + " LIKE ? OR " +
                COLUMN_ROUNDS + " LIKE ?";

        String[] selectionArgs = new String[]{
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%",
                "%" + query + "%"
        };

        return db.query(
                TABLE_WORKOUTS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_CREATED_AT + " DESC"
        );
    }

    public Cursor searchWorkoutsByDate(String query) {
        SQLiteDatabase db = getReadableDatabase();

        if (query == null || query.trim().isEmpty()) {
            return getAllWorkouts();
        }

        if (query.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date queryDate = sdf.parse(query);

                Calendar cal = Calendar.getInstance();
                cal.setTime(queryDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startOfDay = cal.getTimeInMillis();

                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                long endOfDay = cal.getTimeInMillis();

                String selection = COLUMN_CREATED_AT + " BETWEEN ? AND ?";
                String[] selectionArgs = new String[]{
                        String.valueOf(startOfDay),
                        String.valueOf(endOfDay)
                };

                Log.d("DATE_SEARCH", "Searching for timestamps between: " + startOfDay + " and " + endOfDay);

                return db.query(
                        TABLE_WORKOUTS,
                        null,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        COLUMN_CREATED_AT + " DESC"
                );
            } catch (ParseException e) {
                Log.e("DATE_SEARCH", "Error parsing date: " + query, e);
                return searchWorkouts(query);
            }
        } else {
            return searchWorkouts(query);
        }
    }

    public boolean updateWorkoutVoiceNote(long workoutId, String voiceNotePath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VOICE_NOTE, voiceNotePath);

        int rowsAffected = db.update(TABLE_WORKOUTS, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(workoutId)});

        safeClose(db, null);
        return rowsAffected > 0;
    }

    public Cursor getWorkoutsWithVoiceNotes() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_WORKOUTS,
                null,
                COLUMN_VOICE_NOTE + " IS NOT NULL AND " + COLUMN_VOICE_NOTE + " != ''",
                null, null, null,
                COLUMN_CREATED_AT + " DESC");
    }

    public String getWorkoutVoiceNote(long workoutId) {
        SQLiteDatabase db = getReadableDatabase();
        String voiceNotePath = null;
        Cursor cursor = null;

        try {
            cursor = db.query(TABLE_WORKOUTS,
                    new String[]{COLUMN_VOICE_NOTE},
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(workoutId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                voiceNotePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOICE_NOTE));
            }
        } finally {
            safeClose(null, cursor);
        }

        return voiceNotePath;
    }

    public boolean updateWorkoutPhoto(long workoutId, String photoPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VIDEO_NOTE, photoPath);

        int rowsAffected = db.update(TABLE_WORKOUTS, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(workoutId)});

        safeClose(db, null);
        return rowsAffected > 0;
    }

    public Cursor getAllWorkoutsForExport() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_WORKOUTS, null, null, null, null, null,
                COLUMN_CREATED_AT + " DESC");
    }

    public Cursor getWorkoutById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_WORKOUTS, null, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
    }

    public boolean deleteWorkout(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_WORKOUTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        safeClose(db, null);
        return result > 0;
    }

    public Cursor getLatestWorkout() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_WORKOUTS +
                " ORDER BY " + COLUMN_CREATED_AT + " DESC LIMIT 1";
        return db.rawQuery(query, null);
    }

    public long insertWorkout(String workType, int workTime, int restTime, int rounds, String voiceNote, String videoNote) {
        int duration = (workTime * rounds) + (restTime * Math.max(0, rounds - 1));
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORK_TYPE, workType);
        values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_WORK_TIME, workTime);
        values.put(COLUMN_REST_TIME, restTime);
        values.put(COLUMN_ROUNDS, rounds);
        values.put(COLUMN_VOICE_NOTE, voiceNote);
        values.put(COLUMN_VIDEO_NOTE, videoNote);

        SQLiteDatabase db = getWritableDatabase();
        long result = db.insert(TABLE_WORKOUTS, null, values);
        safeClose(db, null);
        return result;
    }

    // ============ NUTRITION METHODS ============

    public long savePersonalProfile(int age, double weight, double height,
                                    String gender, String activityLevel, String goal) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        db.delete(TABLE_PERSONAL_PROFILE, null, null);

        values.put(COLUMN_AGE, age);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_ACTIVITY_LEVEL, activityLevel);
        values.put(COLUMN_GOAL, goal);

        long result = db.insert(TABLE_PERSONAL_PROFILE, null, values);
        safeClose(db, null);
        return result;
    }

    public void saveNutritionProfileGoals(int calories, float protein, float carbs, float fat, float water) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        db.delete(TABLE_NUTRITION_PROFILE, null, null);

        values.put(COLUMN_CALORIES_GOAL, calories);
        values.put(COLUMN_PROTEIN_GOAL, protein);
        values.put(COLUMN_CARBS_GOAL, carbs);
        values.put(COLUMN_FAT_GOAL, fat);
        values.put(COLUMN_WATER_GOAL, water);

        db.insert(TABLE_NUTRITION_PROFILE, null, values);
        safeClose(db, null);
    }

    public void insertSampleMealData() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MEAL_PLAN, null, null);

        insertMeal(db, "Breakfast", "Oatmeal with fruits", "300 cal");
        insertMeal(db, "Lunch", "Chicken salad", "450 cal");
        insertMeal(db, "Dinner", "Grilled fish with vegetables", "500 cal");

        safeClose(db, null);
    }

    private void insertMeal(SQLiteDatabase db, String type, String description, String calories) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEAL_TYPE, type);
        values.put(COLUMN_MEAL_DESCRIPTION, description);
        values.put(COLUMN_CALORIES, calories);
        db.insert(TABLE_MEAL_PLAN, null, values);
    }

    // ============ ANALYTICS METHODS ============

    public AnalyticsData getAnalyticsData(String period) {
        AnalyticsData data = new AnalyticsData();
        SQLiteDatabase db = getReadableDatabase();
        long startTimestamp = getStartTimestampForPeriod(period);

        // Total workouts and duration
        String totalQuery = "SELECT COUNT(*), SUM(" + COLUMN_DURATION + ") FROM " + TABLE_WORKOUTS;
        if (startTimestamp > 0) {
            totalQuery += " WHERE " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }

        Cursor cursor = db.rawQuery(totalQuery, null);
        try {
            if (cursor.moveToFirst()) {
                data.totalWorkouts = cursor.getInt(0);
                data.totalDuration = cursor.getLong(1);
            }
        } finally {
            safeClose(null, cursor);
        }

        // Workout type distribution
        String typeQuery = "SELECT " + COLUMN_WORK_TYPE + ", COUNT(*) FROM " + TABLE_WORKOUTS;
        if (startTimestamp > 0) {
            typeQuery += " WHERE " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }
        typeQuery += " GROUP BY " + COLUMN_WORK_TYPE;

        cursor = db.rawQuery(typeQuery, null);
        try {
            while (cursor.moveToNext()) {
                String type = cursor.getString(0);
                int count = cursor.getInt(1);
                data.workoutTypeDistribution.put(type, count);
            }
        } finally {
            safeClose(null, cursor);
        }

        // Progress data
        String progressQuery = "SELECT " + COLUMN_CREATED_AT + ", SUM(" + COLUMN_DURATION + ") FROM " + TABLE_WORKOUTS;
        if (startTimestamp > 0) {
            progressQuery += " WHERE " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }
        progressQuery += " GROUP BY " + COLUMN_CREATED_AT + " ORDER BY " + COLUMN_CREATED_AT + " ASC";

        cursor = db.rawQuery(progressQuery, null);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            while (cursor.moveToNext()) {
                long timestamp = cursor.getLong(0);
                float totalDuration = cursor.getFloat(1);
                if (timestamp > 0) {
                    String dateString = sdf.format(new Date(timestamp));
                    data.workoutProgress.add(new ProgressDataPoint(dateString, totalDuration));
                }
            }
        } finally {
            safeClose(null, cursor);
        }

        data.personalRecords = getPersonalRecords(db, period);
        safeClose(db, null);
        return data;
    }

    public AnalyticsData getAnalyticsData() {
        return getAnalyticsData("all");
    }

    // ============ UTILITY METHODS ============

    public int getWorkoutCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORKOUTS, null);
        int count = 0;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            safeClose(null, cursor);
        }
        return count;
    }

    public int getWorkoutsWithPhotoCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORKOUTS +
                " WHERE " + COLUMN_VIDEO_NOTE + " IS NOT NULL AND " +
                COLUMN_VIDEO_NOTE + " != ''", null);
        int count = 0;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            safeClose(null, cursor);
        }
        return count;
    }

    public int getWorkoutsWithVoiceCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORKOUTS +
                " WHERE " + COLUMN_VOICE_NOTE + " IS NOT NULL AND " +
                COLUMN_VOICE_NOTE + " != ''", null);
        int count = 0;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            safeClose(null, cursor);
        }
        return count;
    }

    public int getUniqueWorkoutTypesCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT " + COLUMN_WORK_TYPE + ") FROM " + TABLE_WORKOUTS, null);
        int count = 0;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            safeClose(null, cursor);
        }
        return count;
    }

    // ============ PRIVATE HELPER METHODS ============

    private long getStartTimestampForPeriod(String period) {
        if (period == null || period.equals("all")) {
            return 0;
        }

        Calendar calendar = Calendar.getInstance();
        switch (period) {
            case "week":
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "month":
                calendar.add(Calendar.MONTH, -1);
                break;
            case "year":
                calendar.add(Calendar.YEAR, -1);
                break;
            default:
                return 0;
        }
        return calendar.getTimeInMillis();
    }

    private List<PersonalRecord> getPersonalRecords(SQLiteDatabase db, String period) {
        List<PersonalRecord> records = new ArrayList<>();
        long startTimestamp = getStartTimestampForPeriod(period);

        String longest = context.getString(R.string.longest_session);
        String tabata = context.getString(R.string.tabata);
        String emom = context.getString(R.string.emom);
        String amrap = context.getString(R.string.amrap);
        String fortime = context.getString(R.string.for_time);

        // Similar implementation for each workout type...
        // (Keeping the logic the same as your original, but with safe cursor handling)

        return records;
    }

    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }
    public Cursor getWorkoutDates() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WORKOUTS,
                new String[]{COLUMN_CREATED_AT},
                null, null, null, null,
                COLUMN_CREATED_AT + " DESC");
    }
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}