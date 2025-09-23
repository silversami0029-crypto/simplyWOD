package com.bessadi.fitwod;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bessadi.fitwod.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

public class Menu_Activity extends BaseActivity implements AchievementsListener {

    private boolean isTestingMode = true; // Set to false for production
    ImageButton  analytics;
    ImageView menu;
    ImageButton logReport, btnSettings, btnAchievement;
    AdView mAdView;
    private SharedPreferences prefs;

    private PremiumManager premiumManager;
    private static final String appURL = "https://play.google.com/store/apps/details?id=com.bessadi.fitwod";

    private ViewGroup mainLayout;
    private LinearLayout amrap,tabata,emom,fortime;
    private ProgressDialog loadingDialog;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "FitWOD_Preferences";
    private static final String THEME_MODE = "theme_mode";
    private static final String TAG = "Menu_Activity";

    private static final int MODE_NIGHT_NO = 1;        // Light mode
    private static final int MODE_NIGHT_YES = 2;       // Dark mode
    private static final int MODE_NIGHT_FOLLOW_SYSTEM = -1; // System default

    private AchievementsManager achievementsManager;
    private SafeDatabaseHelper databaseHelper; // Add this line

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.rate) {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW);
            rateIntent.setData(Uri.parse(appURL));
            startActivity(rateIntent);
        }
        if (item.getItemId() == R.id.share){
            shareAppViaWhatsApp();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareAppViaWhatsApp() {
        String appPackageName = getPackageName();
        String appLink = "https://play.google.com/store/apps/details?id=" + appPackageName;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing app: " + appLink);
        shareIntent.setPackage("com.whatsapp");

        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "WhatsApp is not installed on your device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Menu_Activity", "onCreate called");

        // Initialize SafeDatabaseHelper
        databaseHelper = SafeDatabaseHelper.getInstance(this);

        // Initialize UI first
        prefs = getSharedPreferences("premium_prefs", Context.MODE_PRIVATE);

        // Initialize PremiumManager LAST and in background
        new Handler().postDelayed(() -> {
            premiumManager = new PremiumManager(this);
        }, 1000);

        // Set theme before setting content view
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int savedTheme = sharedPreferences.getInt(THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);

        setContentView(R.layout.menu_main_cards);

        // Edge-to-edge layout setup
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        View rootView = findViewById(android.R.id.content);
        View mainContentView = ((ViewGroup) rootView).getChildAt(0);

        if (mainContentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContentView, (v, insets) -> {
                int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
                v.setPadding(v.getPaddingLeft(), systemBarsTop, v.getPaddingRight(), systemBarsBottom);
                return insets;
            });
        }

        prefs = getSharedPreferences("premium_prefs", Context.MODE_PRIVATE);
        premiumManager = new PremiumManager(this);

        // Initialize views
        amrap = findViewById(R.id.amrap);
        tabata = findViewById(R.id.tabata);
        emom = findViewById(R.id.emom);
        fortime = findViewById(R.id.fortime);
        menu = findViewById(R.id.menu_button);
        logReport = findViewById(R.id.btnLog);
        analytics = findViewById(R.id.btnAnalytics);
        mainLayout = findViewById(R.id.main_layout);
        btnSettings = findViewById(R.id.btnSettings);
        btnAchievement = findViewById(R.id.btnAchieve);

        View mainCard = findViewById(R.id.mainCard);
        mAdView = findViewById(R.id.adView_main);

        // Initialize achievements manager with database helper
        achievementsManager = new AchievementsManager(this, databaseHelper);

        btnAchievement.setOnClickListener(v -> {showAchievementsDialog();});

        btnSettings.setOnClickListener(v -> {
            Log.d(TAG, "Settings button clicked");
            ScaleAnimation scale = new ScaleAnimation(1f, 0.9f, 1f, 0.9f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(100);
            scale.setRepeatCount(1);
            scale.setRepeatMode(Animation.REVERSE);
            v.startAnimation(scale);
            showThemePopupMenu();
        });

        // Load animations
        Animation scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Set up button hover animations
        setupButtonHoverAnimation(amrap, scaleUp, scaleDown);
        setupButtonHoverAnimation(emom, scaleUp, scaleDown);
        setupButtonHoverAnimation(fortime, scaleUp, scaleDown);
        setupButtonHoverAnimation(tabata, scaleUp, scaleDown);

        // Set click listeners with animations
        setClickListenersWithAnimation();

        // Animate card entrance
        mainCard.startAnimation(fadeIn);

        // Load the saved background when activity starts
        loadSavedBackground();

        // Load ad view if not a premium user
        forPremiumUser();

        findViewById(R.id.menu_button).setOnClickListener(v -> {showTopPopup(v);});

        analytics.setOnClickListener(v -> {
            startActivity(new Intent(Menu_Activity.this, PerformanceAnalyticsActivity.class));
        });

        logReport.setOnClickListener(v -> {
            startActivity(new Intent(Menu_Activity.this, item_workout_history.class));
        });

        fortime.setOnClickListener(v -> {
            startActivity(new Intent(Menu_Activity.this, pbfortime.class));
        });

        amrap.setOnClickListener(v -> {
            startActivity(new Intent(Menu_Activity.this, pbamrap.class));
        });

        tabata.setOnClickListener(v -> {
            startActivity(new Intent(Menu_Activity.this, pbtabata.class));
        });

        emom.setOnClickListener(v -> {
            startActivity(new Intent(Menu_Activity.this, pbemom.class));
        });

        // Test database connection safely
        testDatabaseConnection();
    }

    // Test database connection safely
    private void testDatabaseConnection() {
        new Thread(() -> {
            try {
                // Get workout count safely
                int workoutCount = databaseHelper.getWorkoutCount();
                Log.d("DatabaseTest", "Workout count: " + workoutCount);

                // Test getting workouts safely
                Cursor cursor = databaseHelper.getAllWorkouts();
                trackCursor(cursor); // Auto-closed in onDestroy

                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        int count = 0;
                        do {
                            count++;
                            String workoutType = cursor.getString(cursor.getColumnIndexOrThrow(
                                    SafeDatabaseHelper.COLUMN_WORK_TYPE));
                            Log.d("DatabaseTest", "Workout " + count + ": " + workoutType);
                        } while (cursor.moveToNext());
                        Log.d("DatabaseTest", "Total workouts in cursor: " + count);
                    }
                } finally {
                    safeCloseCursor(cursor); // Close cursor when done
                }

            } catch (Exception e) {
                Log.e("DatabaseTest", "Error testing database: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void onAchievementUnlocked(Achievement achievement) {
        showAchievementUnlockedDialog(achievement);
    }

    public void onWorkoutCompleted() {
        if (achievementsManager != null) {
            // Update achievements using safe database operations
            achievementsManager.checkAndUnlockAchievements();
        }
    }

    private void navigateBasedOnPremiumStatus(boolean isPremium, String feature) {
        if (isPremium) {
            if ("background_change".equals(feature)) {
                startActivity(new Intent(this, ChangeBackgroundActivity.class));
            } else {
                startActivity(new Intent(this, PerformanceAnalyticsActivity.class));
            }
        } else {
            navigateToSubscription(feature);
        }
    }

    private void navigateToSubscription(String feature) {
        Intent intent = new Intent(this, SubscriptionActivity.class);
        intent.putExtra("feature_redirect", feature);
        startActivity(intent);
    }

    private void showAchievementUnlockedDialog(Achievement achievement) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievement_unlocked, null);

        ImageView icon = dialogView.findViewById(R.id.ivAchievementIcon);
        TextView title = dialogView.findViewById(R.id.tvAchievementTitle);
        TextView description = dialogView.findViewById(R.id.tvAchievementDesc);
        Button btnAwesome = dialogView.findViewById(R.id.btnAwesome);

        icon.setImageResource(achievement.getIconResId());
        title.setText(achievement.getTitle());
        description.setText(achievement.getDescription());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        btnAwesome.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        animateAchievementUnlocked(dialogView);
    }

    private void animateAchievementUnlocked(View dialogView) {
        ImageView icon = dialogView.findViewById(R.id.ivAchievementIcon);
        icon.setScaleX(0f);
        icon.setScaleY(0f);
        icon.setAlpha(0f);

        icon.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    private void showThemePopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, btnSettings);
        popupMenu.getMenuInflater().inflate(R.menu.theme_menu, popupMenu.getMenu());

        int currentTheme = sharedPreferences.getInt(THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
            popupMenu.getMenu().findItem(R.id.menu_theme_light).setChecked(true);
        } else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) {
            popupMenu.getMenu().findItem(R.id.menu_theme_dark).setChecked(true);
        } else {
            popupMenu.getMenu().findItem(R.id.menu_theme_system).setChecked(true);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            int themeMode;
            String themeName;

            if (itemId == R.id.menu_theme_light) {
                themeMode = AppCompatDelegate.MODE_NIGHT_NO;
                themeName = "Light";
            } else if (itemId == R.id.menu_theme_dark) {
                themeMode = AppCompatDelegate.MODE_NIGHT_YES;
                themeName = "Dark";
            } else {
                themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                themeName = "System Default";
            }

            sharedPreferences.edit().putInt(THEME_MODE, themeMode).apply();
            AppCompatDelegate.setDefaultNightMode(themeMode);

            Toast.makeText(this, "Applying " + themeName + " theme...", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> recreate(), 100);
            recreate();

            Toast.makeText(this, "Theme changed to " + themeName, Toast.LENGTH_SHORT).show();
            return true;
        });

        popupMenu.show();
    }

    private void applyTheme(int themeMode) {
        Log.d(TAG, "Applying theme: " + themeMode);
        try {
            AppCompatDelegate.setDefaultNightMode(themeMode);
            Log.d(TAG, "Theme applied successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error applying theme: " + e.getMessage());
        }
    }

    private void loadAdView() {
        boolean localPremium = premiumManager.isPremiumUser();
        if (!localPremium) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    private void setClickListenersWithAnimation() {
        View.OnClickListener withAnimation = v -> {
            ScaleAnimation scale = new ScaleAnimation(1f, 0.95f, 1f, 0.95f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scale.setDuration(50);
            scale.setRepeatCount(1);
            scale.setRepeatMode(Animation.REVERSE);
            v.startAnimation(scale);

            if (v == amrap) {
                startActivity(new Intent(Menu_Activity.this, pbamrap.class));
            } else if (v == emom) {
                startActivity(new Intent(Menu_Activity.this, pbemom.class));
            } else if (v == fortime) {
                startActivity(new Intent(Menu_Activity.this, pbfortime.class));
            } else if (v == tabata) {
                startActivity(new Intent(Menu_Activity.this, pbtabata.class));
            } else if (v == analytics) {
                startActivity(new Intent(Menu_Activity.this, PerformanceAnalyticsActivity.class));
            } else if (v == findViewById(R.id.btnLog)) {
                startActivity(new Intent(Menu_Activity.this, item_workout_history.class));
            }
        };

        amrap.setOnClickListener(withAnimation);
        emom.setOnClickListener(withAnimation);
        fortime.setOnClickListener(withAnimation);
        tabata.setOnClickListener(withAnimation);
        analytics.setOnClickListener(withAnimation);
        findViewById(R.id.btnLog).setOnClickListener(withAnimation);
    }

    private void showAchievementsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.achievements_dialog, null);

        RecyclerView rvAchievements = dialogView.findViewById(R.id.rvAchievements);
        Button btnClose = dialogView.findViewById(R.id.btnCloseAchievements);
        TextView tvProgress = dialogView.findViewById(R.id.tvProgress);

        // Get achievements using safe database operations
        List<Achievement> achievements = achievementsManager.getAchievements();

        if (tvProgress != null) {
            int unlockedCount = 0;
            for (Achievement achievement : achievements) {
                if (achievement.isUnlocked()) {
                    unlockedCount++;
                }
            }
            tvProgress.setText(unlockedCount + "/" + achievements.size() + " achievements unlocked");
        }

        AchievementAdapter adapter = new AchievementAdapter(achievements);
        rvAchievements.setLayoutManager(new LinearLayoutManager(this));
        rvAchievements.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setView(dialogView).create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setupButtonHoverAnimation(View button, Animation scaleUp, Animation scaleDown) {
        button.setOnHoverListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.startAnimation(scaleUp);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.startAnimation(scaleDown);
                    break;
            }
            return false;
        });
    }

    private void setBackgroundImage(Uri imageUri) {
        try {
            Glide.with(this)
                    .load(imageUri)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            mainLayout.setBackground(resource);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } catch (Exception e) {
            Log.e("MenuBackground", "Error loading background: " + e.getMessage());
            setDefaultBackground(R.drawable.default_background);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Menu_Activity", "onResume called");

        if (prefs == null) {
            prefs = getSharedPreferences("premium_prefs", Context.MODE_PRIVATE);
        }

        boolean isPremium = prefs.getBoolean("is_premium", false);
        Log.d("Menu_Activity", "onResume premium status: " + isPremium);
    }

    private void loadSavedBackground() {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        boolean isDefaultBackground = prefs.getBoolean("is_default_background", false);

        if (isDefaultBackground) {
            int defaultBackgroundId = prefs.getInt("default_background_id", R.drawable.default_background);
            setDefaultBackground(defaultBackgroundId);
        } else {
            String savedUri = prefs.getString("background_uri", null);
            if (savedUri != null && !savedUri.isEmpty()) {
                setBackgroundImage(Uri.parse(savedUri));
            } else {
                setDefaultBackground(R.drawable.default_background);
            }
        }
    }

    private void setDefaultBackground(int drawableResourceId) {
        mainLayout.setBackgroundResource(drawableResourceId);
    }

    public void showTopPopup(View anchorView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_top_menu, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        int statusBarHeight = getStatusBarHeight();

        popupWindow.showAtLocation(
                anchorView,
                Gravity.TOP | Gravity.START,
                0,
                statusBarHeight
        );

        popupView.findViewById(R.id.home).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.bWorkoutlog).setOnClickListener(v -> {
            openWorkoutItemLog();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.bAnalytics).setOnClickListener(v -> {
            openAnalytics();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.bRateUs).setOnClickListener(v -> {
            openRateApp();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.bRecommend).setOnClickListener(v -> {
            openRecommend();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.bRequestfeatures).setOnClickListener(v -> {
            openRequestFeature();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.bLanguage).setOnClickListener(v -> {
            openLanguage();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.bBackground_image).setOnClickListener(v -> {
            openBackGroundChange();
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.bSubscription).setOnClickListener(v -> {
            openSubscription();
            popupWindow.dismiss();
        });

        popupView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                popupWindow.dismiss();
                return true;
            }
            return false;
        });
    }

    private void openLanguage() {
        startActivity(new Intent(Menu_Activity.this, LanguageSelectionActivity.class));
    }

    private void openWorkoutItemLog() {
        startActivity(new Intent(Menu_Activity.this, item_workout_history.class));
    }

    private void forPremiumUser() {
        boolean localPremium = premiumManager.isPremiumUser();
        Log.d("Menu_Activity", "Local premium status: " + localPremium);
        if (localPremium) {
            analytics.setVisibility(View.VISIBLE);
            analytics.setEnabled(true);
            mAdView.setVisibility(View.INVISIBLE);
            mAdView.setEnabled(false);
        }
    }

    private void openAnalytics() {
        Log.d("Menu_Activity", "openAnalytics() called");

        try {
            boolean localPremium = prefs.getBoolean("is_premium", false);
            Log.d("Menu_Activity", "Simple local premium status: " + localPremium);

            if (localPremium) {
                startActivity(new Intent(Menu_Activity.this, PerformanceAnalyticsActivity.class));
                return;
            }

            Intent intent = new Intent(Menu_Activity.this, SubscriptionActivity.class);
            intent.putExtra("feature_redirect", "analytics");
            startActivity(intent);

        } catch (Exception e) {
            Log.e("Menu_Activity", "Error in openAnalytics: " + e.getMessage(), e);
            try {
                Intent intent = new Intent(Menu_Activity.this, SubscriptionActivity.class);
                intent.putExtra("feature_redirect", "analytics");
                startActivity(intent);
            } catch (Exception ex) {
                Log.e("Menu_Activity", "Even fallback failed: " + ex.getMessage());
            }
        }
    }

    private void openRequestFeature() {
        startActivity(new Intent(Menu_Activity.this, FeatureRequestActivity.class));
    }

    private void openBackGroundChange() {
        Log.d("Menu_Activity", "openBackGroundChange() called");

        try {
            boolean localPremium = prefs.getBoolean("is_premium", false);
            Log.d("Menu_Activity", "Simple local premium status: " + localPremium);

            if (localPremium) {
                Log.d("Menu_Activity", "Going directly to Background change");
                startActivity(new Intent(Menu_Activity.this, ChangeBackgroundActivity.class));
                return;
            }

            Log.d("Menu_Activity", "Going to Subscription page");
            Intent intent = new Intent(Menu_Activity.this, SubscriptionActivity.class);
            intent.putExtra("feature_redirect", "background_change");
            startActivity(intent);

        } catch (Exception e) {
            Log.e("Menu_Activity", "Error in openBackGroundChange: " + e.getMessage(), e);
            try {
                Intent intent = new Intent(Menu_Activity.this, SubscriptionActivity.class);
                intent.putExtra("feature_redirect", "background_change");
                startActivity(intent);
            } catch (Exception ex) {
                Log.e("Menu_Activity", "Even fallback failed: " + ex.getMessage());
            }
        }
    }

    private void openSubscription() {
        startActivity(new Intent(Menu_Activity.this, SubscriptionActivity.class));
    }

    private void openRecommend() {
        shareAppViaWhatsApp();
    }

    private void openRateApp() {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW);
        rateIntent.setData(Uri.parse(appURL));
        startActivity(rateIntent);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // BaseActivity will handle resource cleanup automatically
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("Do you really want to exit?")
                .setIcon(getResources().getDrawable(R.mipmap.ic_launcher))
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishActivity(0);
                    System.exit(0);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}