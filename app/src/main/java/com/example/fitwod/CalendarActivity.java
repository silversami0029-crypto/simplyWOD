package com.example.fitwod;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bessadi.fitwod.R;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvMonthYear;
    private GridView calendarGrid;
    private Calendar currentCalendar;
    private CalendarAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        tvMonthYear = findViewById(R.id.tvMonthYear);
        calendarGrid = findViewById(R.id.calendarGrid);

        // Initialize with current date
        currentCalendar = Calendar.getInstance();

        // Setup navigation buttons
        findViewById(R.id.btnPrev).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        // Setup day headers
        setupDayHeaders();

        updateCalendar();
    }

    private void setupDayHeaders() {
        GridLayout headers = findViewById(R.id.dayHeaders);
        String[] dayNames = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};

        for (int i = 0; i < 7; i++) {
            TextView textView = new TextView(this);
            textView.setText(dayNames[i]);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(16);
            textView.setPadding(8, 8, 8, 8);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i, 1f);
            textView.setLayoutParams(params);

            headers.addView(textView);
        }
    }

    private void updateCalendar() {
        // Update month/year header
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(sdf.format(currentCalendar.getTime()));

        // Generate calendar data
        List<CalendarDay> days = generateCalendarDays();
        adapter = new CalendarAdapter(this, days);
        calendarGrid.setAdapter(adapter);

        // Handle date selection
        calendarGrid.setOnItemClickListener((parent, view, position, id) -> {
            CalendarDay day = days.get(position);
            if (day.isInMonth) {
                Calendar selectedDate = (Calendar) currentCalendar.clone();
                selectedDate.set(Calendar.DAY_OF_MONTH, day.day);

                // Query workout logs for this date
                queryWorkoutLogs(selectedDate);
            }
        });
    }

    private List<CalendarDay> generateCalendarDays() {
        List<CalendarDay> days = new ArrayList<>();
        Calendar calendar = (Calendar) currentCalendar.clone();

        // Determine days in month
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Determine first day of month (1 = Sunday, 2 = Monday, etc.)
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Calculate padding (Calendar.SUNDAY = 1, Calendar.SATURDAY = 7)
        int padding = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7;

        // Add padding days
        for (int i = 0; i < padding; i++) {
            days.add(new CalendarDay(0, false));
        }

        // Add actual days
        Calendar today = Calendar.getInstance();
        for (int i = 1; i <= daysInMonth; i++) {
            boolean isToday = (currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) &&
                    (currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH)) &&
                    (i == today.get(Calendar.DAY_OF_MONTH));

            days.add(new CalendarDay(i, true, isToday));
        }

        return days;
    }

    private void queryWorkoutLogs(Calendar date) {
        // Format date for SQL query
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateStr = sdf.format(date.getTime());

        // Execute SQL query
        String sql = "SELECT * FROM workout_logs WHERE workout_date = '" + dateStr + "'";

        // Run query (using Room, SQLiteOpenHelper, etc.)
        // Display results in your UI
    }

    // Helper classes
    private static class CalendarDay {
        final int day;
        final boolean isInMonth;
        final boolean isToday;

        CalendarDay(int day, boolean isInMonth) {
            this(day, isInMonth, false);
        }

        CalendarDay(int day, boolean isInMonth, boolean isToday) {
            this.day = day;
            this.isInMonth = isInMonth;
            this.isToday = isToday;
        }
    }

    private class CalendarAdapter extends BaseAdapter {
        private final Context context;
        private final List<CalendarDay> days;

        public CalendarAdapter(Context context, List<CalendarDay> days) {
            this.context = context;
            this.days = days;
        }

        @Override
        public int getCount() {
            return days.size();
        }

        @Override
        public Object getItem(int position) {
            return days.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(context);
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(16);
                textView.setLayoutParams(new GridView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 120));
            } else {
                textView = (TextView) convertView;
            }

            CalendarDay day = days.get(position);
            if (day.isInMonth) {
                textView.setText(String.valueOf(day.day));
                textView.setTextColor(Color.BLACK);

                if (day.isToday) {
                    textView.setBackgroundColor(ContextCompat.getColor(context, R.color.min_blue));
                    textView.setTypeface(null, Typeface.BOLD);
                } else {
                    textView.setBackgroundColor(Color.TRANSPARENT);
                    textView.setTypeface(null, Typeface.NORMAL);
                }
            } else {
                textView.setText("");
                textView.setBackgroundColor(Color.TRANSPARENT);
            }

            return textView;
        }
    }
}
