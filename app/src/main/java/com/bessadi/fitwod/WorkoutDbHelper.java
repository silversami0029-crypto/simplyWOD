package com.bessadi.fitwod;




import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.util.Calendar;
import android.util.Log;

import com.bessadi.fitwod.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class WorkoutDbHelper extends SQLiteOpenHelper {

    // Declare the nutritionService

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

    // TABLE_MEALS


    public static final String TABLE_MEAL_PLAN = "meal_plan";
    public static final String COLUMN_MEAL_ID = "meal_id";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_MEAL_TYPE = "meal_type";
    public static final String COLUMN_MEAL_DESCRIPTION = "meal_description";
    public static final String COLUMN_CALORIES = "calories";
    // Increment database version
    //nutrition profile table

    public static final String TABLE_PERSONAL_PROFILE = "nutrition_table";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_HEIGHT ="height";
    public static final String COLUMN_WEIGHT = "wright";
    public static final String COLUMN_GENDER ="gendeer";
    public static final String COLUMN_ACTIVITY_LEVEL = "level";
    public static final String COLUMN_GOAL = "goal";

    public static final String TABLE_NUTRITION_PROFILE = "nutrition_profile";
    public static final String COLUMN_CALORIES_GOAL = "calories_goal";
    public static final String COLUMN_PROTEIN_GOAL = "protein_goal";
    public static final String COLUMN_CARBS_GOAL = "carbs_goal";
    public static final String COLUMN_FAT_GOAL = "fat_goal";
    public static final String COLUMN_WATER_GOAL = "water_goal";




    private Context context;

    public WorkoutDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;


    }


    @Override
    public void onCreate(SQLiteDatabase db) {



        // Create nutrition profile table
        String createNutritionTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PERSONAL_PROFILE + "("
                + COLUMN_AGE + " INTEGER, "
                + COLUMN_WEIGHT + " REAL, "
                + COLUMN_HEIGHT + " REAL, "
                + COLUMN_GENDER + " TEXT, "
                + COLUMN_ACTIVITY_LEVEL + " TEXT, "
                + COLUMN_GOAL + " TEXT);";


        //WORKOUTS TABLE
        String createTable = "CREATE TABLE " + TABLE_WORKOUTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_WORK_TYPE + " TEXT, " +
                COLUMN_CREATED_AT + " INTEGER, " +
               COLUMN_DURATION + " INTEGER, " +
                COLUMN_WORK_TIME + " INTEGER, " +
                COLUMN_REST_TIME + " INTEGER, " +
                COLUMN_ROUNDS + " INTEGER, " +
                COLUMN_VOICE_NOTE + " TEXT," +
                COLUMN_VIDEO_NOTE + " TEXT"+ ")";


        db.execSQL(createTable);

    }



    // ... inside WorkoutDbHelper class

    public Cursor getAllWorkouts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_WORKOUTS,
                null, // Columns; null selects all
                null, // Selection
                null, // Selection args
                null, // Group by
                null, // Having
                COLUMN_CREATED_AT + " DESC" // Order by
        );
    }

    public Cursor getWorkoutsByType(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
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
        SQLiteDatabase db = this.getReadableDatabase();

        if (query == null || query.trim().isEmpty()) {
            return getAllWorkouts(); // Return all if query is empty
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
    public void debugDates() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_CREATED_AT + " FROM " + TABLE_WORKOUTS + " LIMIT 5", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                Log.d("DATE_DEBUG", "Stored date: " + date);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
    }
    public void debugAllDates() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_CREATED_AT + " FROM " + TABLE_WORKOUTS, null);

        Log.d("DATE_DEBUG", "All dates in database:");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                Log.d("DATE_DEBUG", "Stored date: '" + date + "'");
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Log.d("DATE_DEBUG", "No dates found in database");
        }
    }
    public String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Or if you want just the date part
    public String formatTimestampToDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    public Cursor searchWorkoutsByDate(String query) {
        SQLiteDatabase db = this.getReadableDatabase();

        if (query == null || query.trim().isEmpty()) {
            return getAllWorkouts();
        }

        // Check if the query is in date format (yyyy-MM-dd)
        if (query.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                // Parse the query date
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date queryDate = sdf.parse(query);

                // Calculate start and end of the day in milliseconds
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

                // Search for timestamps within this day
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
                // Fall back to regular search if date parsing fails
                return searchWorkouts(query);
            }
        } else {
            // For non-date queries, use the regular search
            return searchWorkouts(query);
        }
    }

    public boolean updateWorkoutVoiceNote(long workoutId, String voiceNotePath) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_VOICE_NOTE, voiceNotePath);

        int rowsAffected = db.update(TABLE_WORKOUTS, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(workoutId)});

        return rowsAffected > 0;
    }


    public Cursor getWorkoutsWithVoiceNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WORKOUTS,
                null, // all columns
                COLUMN_VOICE_NOTE + " IS NOT NULL AND " + COLUMN_VOICE_NOTE + " != ''",
                null, null, null,
                COLUMN_CREATED_AT + " DESC");
    }
    public String getWorkoutVoiceNote(long workoutId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String voiceNotePath = null;

        Cursor cursor = db.query(TABLE_WORKOUTS,
                new String[]{COLUMN_VOICE_NOTE},
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(workoutId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            voiceNotePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VOICE_NOTE));
            cursor.close();
        }

        return voiceNotePath;
    }
// Add method to update photo path
    public boolean updateWorkoutPhoto(long workoutId, String photoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_VIDEO_NOTE, photoPath);

        int rowsAffected = db.update(TABLE_WORKOUTS, values,
                COLUMN_ID + " = ?", new String[]{String.valueOf(workoutId)});

        return rowsAffected > 0;
    }
    // Add method for export
    public Cursor getAllWorkoutsForExport() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WORKOUTS, null, null, null, null, null,
                COLUMN_CREATED_AT + " DESC");
    }

public Cursor getWorkoutById(long id) {
    SQLiteDatabase db = this.getReadableDatabase();
    return db.query(TABLE_WORKOUTS, null, COLUMN_ID + " = ?",
            new String[]{String.valueOf(id)}, null, null, null);
}

    public boolean deleteWorkout(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_WORKOUTS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    public Cursor getLatestWorkout() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_WORKOUTS +
                " ORDER BY " + COLUMN_CREATED_AT + " DESC LIMIT 1";
        return db.rawQuery(query, null);
    }
    public long insertWorkout(String workType, int workTime, int restTime, int rounds, String voiceNote, String videoNote) {
        // Compute duration: (workTime * rounds) + (restTime * (rounds - 1))
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

        // Insert into database
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_WORKOUTS, null, values);
    }

    private void createPersonalProfileTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_PERSONAL_PROFILE + "("
                + COLUMN_AGE + " INTEGER, "
                + COLUMN_WEIGHT + " REAL, "
                + COLUMN_HEIGHT + " REAL, "
                + COLUMN_GENDER + " TEXT, "
                + COLUMN_ACTIVITY_LEVEL + " TEXT, "
                + COLUMN_GOAL + " TEXT);";
        db.execSQL(sql);
    }
    // Helper methods
    private void createNutritionTable(SQLiteDatabase db){
        String createNutritionTable = "CREATE TABLE " + TABLE_NUTRITION_PROFILE + "("
                + COLUMN_CALORIES_GOAL + " INTEGER, "
                + COLUMN_PROTEIN_GOAL + " REAL, "
                + COLUMN_CARBS_GOAL + " REAL, "
                + COLUMN_FAT_GOAL + " REAL, "
                + COLUMN_WATER_GOAL + " REAL);";
        db.execSQL(createNutritionTable);

    }
    private void createMealPlanTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_MEAL_PLAN + "("
                + COLUMN_MEAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DAY + " TEXT, "
                + COLUMN_MEAL_TYPE + " TEXT, "
                + COLUMN_MEAL_DESCRIPTION + " TEXT, "
                + COLUMN_CALORIES + " TEXT);";
        db.execSQL(sql);
    }
    private void addColumnToTable(SQLiteDatabase db, String table, String column, String type) {
        db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        // Handle new meal plan table for upgrades
        if (oldVersion < 5) {
            createMealPlanTable(db);
            createPersonalProfileTable(db);
            createNutritionTable(db);}
    }

    public void checkForHiddenCharacters() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_CREATED_AT + ", length(" + COLUMN_CREATED_AT + ") FROM " + TABLE_WORKOUTS, null);

        Log.d("HIDDEN_CHARS", "Checking for hidden characters:");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                int length = cursor.getInt(1);
                Log.d("HIDDEN_CHARS", "Date: '" + date + "' Length: " + length);

                // Check for non-printable characters
                for (int i = 0; i < date.length(); i++) {
                    char c = date.charAt(i);
                    if (c < 32 || c > 126) {
                        Log.d("HIDDEN_CHARS", "Non-printable char at position " + i + ": " + (int) c);
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
    public void testManualQuery() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Test with a known date from your screenshot
        String testDate = "2025-08-22";
        String sql = "SELECT * FROM " + TABLE_WORKOUTS +
                " WHERE " + COLUMN_CREATED_AT + " LIKE '" + testDate + "%'";

        Log.d("MANUAL_TEST", "Testing manual query: " + sql);

        Cursor cursor = db.rawQuery(sql, null);
        Log.d("MANUAL_TEST", "Manual query found " + cursor.getCount() + " results");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));
                Log.d("MANUAL_TEST", "Manual result: " + date);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }
    public AnalyticsData getAnalyticsData(String period) {

        AnalyticsData data = new AnalyticsData();
        SQLiteDatabase db = this.getReadableDatabase();
// Get start timestamp for filtering
        long startTimestamp = getStartTimestampForPeriod(period);

        // 1. Get total workouts and duration (with date filter)
        String totalQuery = "SELECT COUNT(*), SUM(" + COLUMN_DURATION + ") FROM " + TABLE_WORKOUTS;
        if (startTimestamp > 0) {
            totalQuery += " WHERE " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }

        try (Cursor cursor = db.rawQuery(totalQuery, null)) {
            if (cursor.moveToFirst()) {
                data.totalWorkouts = cursor.getInt(0);
                data.totalDuration = cursor.getLong(1);
            }
        }


        // 2. Get workout type distribution (with date filter)
        String typeQuery = "SELECT " + COLUMN_WORK_TYPE + ", COUNT(*) FROM " + TABLE_WORKOUTS;
        if (startTimestamp > 0) {
            typeQuery += " WHERE " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }
        typeQuery += " GROUP BY " + COLUMN_WORK_TYPE;

        try (Cursor cursor = db.rawQuery(typeQuery, null)) {
            while (cursor.moveToNext()) {
                String type = cursor.getString(0);
                int count = cursor.getInt(1);
                data.workoutTypeDistribution.put(type, count);
            }
        }


        // 3. Get progress data (with date filter)
        String progressQuery = "SELECT " + COLUMN_CREATED_AT + ", SUM(" + COLUMN_DURATION + ") FROM " + TABLE_WORKOUTS;
        if (startTimestamp > 0) {
            progressQuery += " WHERE " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }
        progressQuery += " GROUP BY " + COLUMN_CREATED_AT + " ORDER BY " + COLUMN_CREATED_AT + " ASC";

        try (Cursor progressCursor = db.rawQuery(progressQuery, null)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            while (progressCursor.moveToNext()) {
                long timestamp = progressCursor.getLong(0);
                float totalDuration = progressCursor.getFloat(1);

                if (timestamp > 0) {
                    String dateString = sdf.format(new Date(timestamp));
                    data.workoutProgress.add(new ProgressDataPoint(dateString, totalDuration));
                }
            }
        }
// 4. Get personal records with period filter
        data.personalRecords = getPersonalRecords(db, period);

        return data;
    }
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
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_created_at ON " + TABLE_WORKOUTS + "(" + COLUMN_CREATED_AT + ")");
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_work_type ON " + TABLE_WORKOUTS + "(" + COLUMN_WORK_TYPE + ")");

        String longest = context.getString(R.string.longest_session); // values/strings.xml
        String tabata = context.getString(R.string.tabata); // values/strings.xml
        String emom = context.getString(R.string.emom); // values/strings.xml
        String amrap = context.getString(R.string.amrap); // values/strings.xml
        String fortime = context.getString(R.string.for_time); // values/strings.xml

       List<PersonalRecord> records = new ArrayList<>();
       long startTimestamp = getStartTimestampForPeriod(period);

        // Helper method to build WHERE clause
        // TABATA longest session
        String tabataWhere = " WHERE " + COLUMN_WORK_TYPE + " = 'TABATA'";
        if (startTimestamp > 0) {
            tabataWhere += " AND " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }
        String tabataQuery = "SELECT MAX((" + COLUMN_WORK_TIME + " + " + COLUMN_REST_TIME + ") * " +
                COLUMN_ROUNDS + "), " + COLUMN_CREATED_AT + " FROM " +
                TABLE_WORKOUTS + tabataWhere;


        try (Cursor cursor = db.rawQuery(tabataQuery, null)) {
            if (cursor.moveToFirst()) {
                PersonalRecord record = new PersonalRecord();
                //record.workoutType = "TABATA";
                record.workoutType = tabata;
                record.metric = longest;
                record.value = formatDuration(cursor.getInt(0));
                record.date = formatDate(cursor.getLong(1));
                records.add(record);
            }
        }


        // EMOM longest session
        String emomWhere = " WHERE " + COLUMN_WORK_TYPE + " = 'EMOM'";
        if (startTimestamp > 0) {
            emomWhere += " AND " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }
        String emomQuery = "SELECT MAX((" + COLUMN_WORK_TIME + " + " + COLUMN_REST_TIME + ") * " +
                COLUMN_ROUNDS + "), " + COLUMN_CREATED_AT + " FROM " +
                TABLE_WORKOUTS + emomWhere;


        try (Cursor cursor = db.rawQuery(emomQuery, null)) {
            if (cursor.moveToFirst()) {
                PersonalRecord record = new PersonalRecord();
                //record.workoutType = "EMOM";
                record.workoutType = emom;
                record.metric = longest;
                record.value = formatDuration(cursor.getInt(0));
                record.date = formatDate(cursor.getLong(1));
                records.add(record);
            }
        }

        // AMRAP longest session
        String amrapWhere = " WHERE " + COLUMN_WORK_TYPE + " = 'AMRAP'";
        if (startTimestamp > 0) {
            amrapWhere += " AND " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }
        String amrapQuery = "SELECT MAX((" + COLUMN_WORK_TIME + " + " + COLUMN_REST_TIME + ") * " +
                COLUMN_ROUNDS + "), " + COLUMN_CREATED_AT + " FROM " +
                TABLE_WORKOUTS + amrapWhere;



        try (Cursor cursor = db.rawQuery(amrapQuery, null)) {
            if (cursor.moveToFirst()) {
                PersonalRecord record = new PersonalRecord();
                //record.workoutType = "AMRAP";
                record.workoutType = amrap;
                record.metric = longest;
                record.value = formatDuration(cursor.getInt(0));
                record.date = formatDate(cursor.getLong(1));
                records.add(record);
            }
        }

        // FOR TIME longest session
        String fortimeWhere = " WHERE " + COLUMN_WORK_TYPE + " = 'FOR TIME'";
        if (startTimestamp > 0) {
            fortimeWhere += " AND " + COLUMN_CREATED_AT + " >= " + startTimestamp;
        }
        String fortimeQuery = "SELECT MAX((" + COLUMN_WORK_TIME + " + " + COLUMN_REST_TIME + ") * " +
                COLUMN_ROUNDS + "), " + COLUMN_CREATED_AT + " FROM " +
                TABLE_WORKOUTS + fortimeWhere;


        try (Cursor cursor = db.rawQuery(fortimeQuery, null)) {
            if (cursor.moveToFirst()) {
                PersonalRecord record = new PersonalRecord();
                //record.workoutType = "FOR TIME";
                record.workoutType = fortime;
                record.metric = longest;
                record.value = formatDuration(cursor.getInt(0));
                record.date = formatDate(cursor.getLong(1));
                records.add(record);
            }
        }


        return records;
    }
    // Keep this for backward compatibility
    public AnalyticsData getAnalyticsData() {
        return getAnalyticsData("all");
    }


    // Utility method to format duration (seconds to MM:SS)
    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    // Utility method to format timestamp
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Nutrition Profile
    // Save personal nutrition profile (age, weight, height, etc.)
    public long savePersonalProfile(int age, double weight, double height,
                                    String gender, String activityLevel, String goal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Delete any existing profile first
        db.delete(TABLE_PERSONAL_PROFILE, null, null);

        values.put(COLUMN_AGE, age);
        values.put(COLUMN_WEIGHT, weight);
        values.put(COLUMN_HEIGHT, height);
        values.put(COLUMN_GENDER, gender);
        values.put(COLUMN_ACTIVITY_LEVEL, activityLevel);
        values.put(COLUMN_GOAL, goal);

        return db.insert(TABLE_PERSONAL_PROFILE, null, values);
    }

    // Get personal nutrition profile


    public void saveNutritionProfileGoals(int calories, float protein, float carbs, float fat, float water) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Delete existing profile first (only one profile)
        db.delete(TABLE_NUTRITION_PROFILE, null, null);

        values.put(COLUMN_CALORIES_GOAL, calories);
        values.put(COLUMN_PROTEIN_GOAL, protein);
        values.put(COLUMN_CARBS_GOAL, carbs);
        values.put(COLUMN_FAT_GOAL, fat);
        values.put(COLUMN_WATER_GOAL, water);

        db.insert(TABLE_NUTRITION_PROFILE, null, values);
    }


    // Add this method to WorkoutDbHelper
    public void insertSampleMealData() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Clear existing data
        db.delete(TABLE_MEAL_PLAN, null, null);

        // Insert sample meals
        insertMeal(db, "Breakfast", "Oatmeal with fruits", "300 cal");
        insertMeal(db, "Lunch", "Chicken salad", "450 cal");
        insertMeal(db, "Dinner", "Grilled fish with vegetables", "500 cal");
    }

    private void insertMeal(SQLiteDatabase db, String type, String description, String calories) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEAL_TYPE, type);
        values.put(COLUMN_MEAL_DESCRIPTION, description);
        values.put(COLUMN_CALORIES, calories);
        db.insert(TABLE_MEAL_PLAN, null, values);
    }




    //achievements methods


    private int getWorkoutTypeCount(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM workouts WHERE type = ?",
                new String[]{type});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public boolean hasConsecutiveWorkouts(int days) {
        return getMaxConsecutiveDays() >= days;
    }

    private int getMaxConsecutiveDays() {
        // Implementation to calculate consecutive workout days
        // This is a simplified version - you might need a more robust implementation
        SQLiteDatabase db = this.getReadableDatabase();
        // ... your implementation here ...
        return 0; // placeholder
    }

    private boolean hasRecentPersonalBest() {
        // Check if any personal best was set recently
        // Implementation depends on your data structure
        return false; // placeholder
    }

    public int getWorkoutCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORKOUTS, null);
        int count = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getWorkoutsWithPhotoCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORKOUTS +
                " WHERE " + COLUMN_VIDEO_NOTE + " IS NOT NULL AND " +
                COLUMN_VIDEO_NOTE + " != ''", null);
        int count = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public int getWorkoutsWithVoiceCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORKOUTS +
                " WHERE " + COLUMN_VOICE_NOTE + " IS NOT NULL AND " +
                COLUMN_VOICE_NOTE + " != ''", null);
        int count = 0;
        if (cursor != null) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public Cursor getUniqueWorkoutTypes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(true, TABLE_WORKOUTS,
                new String[]{COLUMN_WORK_TIME},
                null, null, COLUMN_WORK_TYPE, null, null, null);
    }

    public Cursor getWorkoutDates() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_WORKOUTS,
                new String[]{COLUMN_CREATED_AT},
                null, null, null, null,
                COLUMN_CREATED_AT + " DESC");
    }
    public int getUniqueWorkoutTypesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(DISTINCT " + COLUMN_WORK_TYPE + ") FROM " + TABLE_WORKOUTS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }



}

