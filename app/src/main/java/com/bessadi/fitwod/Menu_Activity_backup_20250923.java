package com.bessadi.fitwod;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;


public class Menu_Activity_backup_20250923 extends BaseActivity implements AchievementsListener {




    private boolean isTestingMode = true; // Set to false for production
ImageButton  analytics;
//Button tabata,fortime,amrap, emom;
ImageView menu;
//TextView logReport;
ImageButton logReport, btnSettings, btnAchievement;
AdView mAdView;
private SharedPreferences prefs;

private PremiumManager premiumManager;
private static final String appURL = "https://play.google.com/store/apps/details?id=com.bessadi.fitwod";

//private ConstraintLayout mainLayout;
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



public boolean onOptionsItemSelected(MenuItem item) {

   if (item.getItemId() == R.id.rate) {
            Intent rateIntent = new Intent(Intent.ACTION_VIEW);
            rateIntent.setData(Uri.parse(appURL));
           // rateIntent.setPackage("com.bessadi.fitwod");
            startActivity(rateIntent);
    }
   if (item.getItemId() == R.id.share){
       shareAppViaWhatsApp();
    }

return super.onOptionsItemSelected(item);
}

private void shareAppViaWhatsApp() {
    String appPackageName = getPackageName(); // Get your app's package name
    String appLink = "https://play.google.com/store/apps/details?id=" + appPackageName;

    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing app: " + appLink);

    // Ensure WhatsApp is installed
    shareIntent.setPackage("com.whatsapp");

    try {
        startActivity(shareIntent);
    } catch (android.content.ActivityNotFoundException ex) {
        Toast.makeText(this, "WhatsApp is not installed on your device.", Toast.LENGTH_SHORT).show();
    }
}
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);



    Log.d("Menu_Activity", "onCreate called");

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
    // --- CORRECTED CODE FOR EDGE-TO-EDGE LAYOUT ---
    // 1. Request to draw behind the system bars
    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

    // 2. Handle the insets for the root view of your activity
    View rootView = findViewById(android.R.id.content);
    View mainContentView = ((ViewGroup) rootView).getChildAt(0); // Gets the first child of the root (usually your layout)

    if (mainContentView != null) {
        ViewCompat.setOnApplyWindowInsetsListener(mainContentView, (v, insets) -> {
            // Get the insets for the system bars (status bar + navigation bar)
            // 'insets' is already a WindowInsetsCompat object!
            int systemBarsTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            int systemBarsBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Apply the top inset as padding to push the content down below the status bar
            v.setPadding(v.getPaddingLeft(),
                    systemBarsTop, // This is the crucial padding for the top
                    v.getPaddingRight(),
                    systemBarsBottom); // And for the bottom nav bar if needed

            // Return the insets, consuming the parts we've used for padding
            return insets;
        });
    }
    // --- END OF CORRECTED CODE ---



    // Initialize simple preferences first
    prefs = getSharedPreferences("premium_prefs", Context.MODE_PRIVATE);



    // Simple UI setup without waiting for billing



    premiumManager = new PremiumManager(this);

    amrap =  findViewById(R.id.amrap);
    tabata =  findViewById(R.id.tabata);
    emom =   findViewById(R.id.emom);
    fortime =  findViewById(R.id.fortime);
    menu =  findViewById(R.id.menu_button);
    logReport = findViewById(R.id.btnLog);
    analytics = findViewById(R.id.btnAnalytics);
    mainLayout = findViewById(R.id.main_layout);
    btnSettings = findViewById(R.id.btnSettings);
    btnAchievement = findViewById(R.id.btnAchieve);


    View mainCard = findViewById(R.id.mainCard);
    mAdView = findViewById(R.id.adView_main);

    //achievements declarations
    //achievementsManager = new AchievementsManager(this);

    btnAchievement.setOnClickListener(v -> {showAchievementsDialog();});

    btnSettings.setOnClickListener(v -> {
        Log.d(TAG, "Settings button clicked");

        // Button animation
        ScaleAnimation scale = new ScaleAnimation(1f, 0.9f, 1f, 0.9f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(100);
        scale.setRepeatCount(1);
        scale.setRepeatMode(Animation.REVERSE);

        v.startAnimation(scale);
        // Show theme selection menu
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

    // Settings button


    // Animate card entrance
    mainCard.startAnimation(fadeIn);


    //Load the save background when activity starts
    loadSavedBackground();


     //load adViw if not a premium user
    //loadAdView();
    //Premium user privileges.
    forPremiumUser();



    findViewById(R.id.menu_button).setOnClickListener(v -> {showTopPopup(v);});


    analytics.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Menu_Activity_backup_20250923.this, PerformanceAnalyticsActivity.class));
        }
    });

    logReport.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(Menu_Activity_backup_20250923.this, item_workout_history.class));
        }
    });
    fortime.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            startActivity(new Intent(Menu_Activity_backup_20250923.this, pbfortime.class));
        }
    });
    amrap.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            /*startActivity(new Intent(Menu_Activity.this, amrap.class));*/
            startActivity(new Intent(Menu_Activity_backup_20250923.this, pbamrap.class));
        }
    });

    tabata.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            startActivity(new Intent(Menu_Activity_backup_20250923.this, pbtabata.class));
        }
    });

    emom.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            /*startActivity(new Intent(Menu_Activity.this, tabata.class));*/
            startActivity(new Intent(Menu_Activity_backup_20250923.this, pbemom.class));
        }
    });

}
    @Override
public void onAchievementUnlocked(Achievement achievement) {
    // Show celebration dialog for newly unlocked achievement
    showAchievementUnlockedDialog(achievement);
}


// Call this when user completes a workout
public void onWorkoutCompleted() {
    if (achievementsManager != null) {
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

private void testBillingSafely() {
    Log.d("Menu_Activity", "Testing billing step by step...");
    showLoadingDialog();

    new Thread(() -> {
        try {
            // Step 1: Test basic PremiumManager
            Log.d("Menu_Activity", "Step 1: Testing basic PremiumManager");
            PremiumManager testManager = new PremiumManager(Menu_Activity_backup_20250923.this);

            // Step 2: Test simple methods
            Log.d("Menu_Activity", "Step 2: Testing simple methods");
            boolean testPremium = testManager.isPremiumUser();

            // Step 3: Test billing connection (if needed)
            Log.d("Menu_Activity", "Step 3: Test completed");

            runOnUiThread(() -> {
                dismissLoadingDialog();
                Toast.makeText(Menu_Activity_backup_20250923.this, "Billing test passed!", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Log.e("Menu_Activity", "Billing test failed: " + e.getMessage(), e);
            runOnUiThread(() -> {
                dismissLoadingDialog();
                Toast.makeText(Menu_Activity_backup_20250923.this, "Billing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }).start();
}

private void showAchievementUnlockedDialog(Achievement achievement) {
    // Inflate the custom dialog layout
    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_achievement_unlocked, null);

    // Find views
    ImageView icon = dialogView.findViewById(R.id.ivAchievementIcon);
    TextView title = dialogView.findViewById(R.id.tvAchievementTitle);
    TextView description = dialogView.findViewById(R.id.tvAchievementDesc);
    Button btnAwesome = dialogView.findViewById(R.id.btnAwesome);

    // Set achievement data
    icon.setImageResource(achievement.getIconResId());
    title.setText(achievement.getTitle());
    description.setText(achievement.getDescription());

    // Create the dialog
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setView(dialogView);
    builder.setCancelable(false); // User must click the button to dismiss

    AlertDialog dialog = builder.create();

    // Set button click listener
    btnAwesome.setOnClickListener(v -> dialog.dismiss());

    // Show the dialog
    dialog.show();

    // Add celebration animation
    animateAchievementUnlocked(dialogView);
}

private void animateAchievementUnlocked(View dialogView) {
    ImageView icon = dialogView.findViewById(R.id.ivAchievementIcon);

    // Initial state - hidden and scaled down
    icon.setScaleX(0f);
    icon.setScaleY(0f);
    icon.setAlpha(0f);

    // Animate in with bounce effect
    icon.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(new OvershootInterpolator(2f))
            .start();

    // Optional: Add confetti or particle effects here
}

// end of AchievementsListener implementation

private void showThemePopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, btnSettings);
        popupMenu.getMenuInflater().inflate(R.menu.theme_menu, popupMenu.getMenu());


        // Set current theme as checked
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

            // Save preference
            sharedPreferences.edit().putInt(THEME_MODE, themeMode).apply();

            // Apply theme and recreate activity
            AppCompatDelegate.setDefaultNightMode(themeMode);

            // Recreate the activity to apply the theme immediately
            Toast.makeText(this, "Applying " + themeName + " theme...", Toast.LENGTH_SHORT).show();

             // Then recreate with a slight delay
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



private void loadAdView (){
 boolean localPremium = premiumManager.isPremiumUser();
 if (!localPremium) {
     // [START load_ad]
     AdRequest adRequest = new AdRequest.Builder().build();
     mAdView.loadAd(adRequest);
     // [END load_ad]
     }
}

private void setClickListenersWithAnimation() {
    View.OnClickListener withAnimation = v -> {
        // Scale animation on click
        ScaleAnimation scale = new ScaleAnimation(1f, 0.95f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(50);
        scale.setRepeatCount(1);
        scale.setRepeatMode(Animation.REVERSE);
        v.startAnimation(scale);

        // Handle clicks
        if (v == amrap) {
            startActivity(new Intent(Menu_Activity_backup_20250923.this, pbamrap.class));
        } else if (v == emom) {
            startActivity(new Intent(Menu_Activity_backup_20250923.this, pbemom.class));
        } else if (v == fortime) {
            startActivity(new Intent(Menu_Activity_backup_20250923.this, pbfortime.class));
        } else if (v == tabata) {
            startActivity(new Intent(Menu_Activity_backup_20250923.this, pbtabata.class));
        } else if (v == analytics) {
            startActivity(new Intent(Menu_Activity_backup_20250923.this, PerformanceAnalyticsActivity.class));
        } else if (v == findViewById(R.id.btnLog)) {
            startActivity(new Intent(Menu_Activity_backup_20250923.this, item_workout_history.class));
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
    // Inflate the dialog layout
    View dialogView = LayoutInflater.from(this).inflate(R.layout.achievements_dialog, null);

    // Find the RecyclerView IN THE DIALOG layout
    RecyclerView rvAchievements = dialogView.findViewById(R.id.rvAchievements);
    Button btnClose = dialogView.findViewById(R.id.btnCloseAchievements);
    TextView tvProgress = dialogView.findViewById(R.id.tvProgress);

    // Get achievements based on REAL user data
    List<Achievement> achievements = achievementsManager.getAchievements();

    // Show progress (e.g., "3/7 achievements unlocked")
    if (tvProgress != null) {
        int unlockedCount = 0;
        for (Achievement achievement : achievements) {
            if (achievement.isUnlocked()) {
                unlockedCount++;
            }
        }
        tvProgress.setText(unlockedCount + "/" + achievements.size() + " achievements unlocked");
    }

    // Create and set the adapter
    AchievementAdapter adapter = new AchievementAdapter(achievements);
    rvAchievements.setLayoutManager(new LinearLayoutManager(this));
    rvAchievements.setAdapter(adapter);

    // Create and show the dialog
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

private void addViewWithDelay(View view, String viewName, long delay) {
    new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
            mainLayout.addView(view);
            Log.d("G_DEBUG", "Added: " + viewName + " - Check if 'G' appeared");
        }
    }, delay);
}

    private void simpleViewCheck(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            Log.d("VIEW_CHECK", "View: " + child.getClass().getSimpleName() +
                    " - ID: " + (child.getId() != View.NO_ID ? getResources().getResourceName(child.getId()) : "none") +
                    " - Visible: " + (child.getVisibility() == View.VISIBLE));
        }
    }
// Add these methods to your class:
private void debugAllViews(ViewGroup parent) {
    for (int i = 0; i < parent.getChildCount(); i++) {
        View child = parent.getChildAt(i);
        String viewInfo = getViewInfo(child);
        Log.d("VIEW_DEBUG", viewInfo);

        // Check if this view might be the "G"
        if (couldBeTheG(child)) {
            Log.d("G_DEBUG", "POSSIBLE 'G' SOURCE: " + viewInfo);
            child.setVisibility(View.GONE); // Temporarily hide to test
        }

        if (child instanceof ViewGroup) {
            debugAllViews((ViewGroup) child);
        }
    }
}

private String getViewInfo(View view) {
    return view.getClass().getSimpleName() +
            " ID: " + (view.getId() != View.NO_ID ? getResources().getResourceName(view.getId()) : "no_id") +
            " Visible: " + (view.getVisibility() == View.VISIBLE) +
            " Text: " + (view instanceof TextView ? ((TextView) view).getText() : "N/A") +
            " ContentDesc: " + (view.getContentDescription() != null ? view.getContentDescription() : "N/A");
}

private boolean couldBeTheG(View view) {
    // Check if this view might contain the "G"
    if (view instanceof TextView) {
        TextView tv = (TextView) view;
        return "G".equals(tv.getText().toString());
    }
    if (view.getContentDescription() != null) {
        return view.getContentDescription().toString().contains("G");
    }
    return false;
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

    // Initialize prefs if not done
    if (prefs == null) {
        prefs = getSharedPreferences("premium_prefs", Context.MODE_PRIVATE);
    }

    // Simple premium status check
    boolean isPremium = prefs.getBoolean("is_premium", false);
    Log.d("Menu_Activity", "onResume premium status: " + isPremium);
}


private void loadSavedBackground() {
    SharedPreferences prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);

    // Check if default background is set
    boolean isDefaultBackground = prefs.getBoolean("is_default_background", false);

    if (isDefaultBackground) {
        // Load default background from drawable
        int defaultBackgroundId = prefs.getInt("default_background_id", R.drawable.default_background);
        setDefaultBackground(defaultBackgroundId);
    } else {
        // Load custom background from gallery
        String savedUri = prefs.getString("background_uri", null);
        if (savedUri != null && !savedUri.isEmpty()) {
            setBackgroundImage(Uri.parse(savedUri));
        } else {
            // Fallback to original default background
            setDefaultBackground(R.drawable.default_background);
        }
    }
}


private void setDefaultBackground(int drawableResourceId) {
    mainLayout.setBackgroundResource(drawableResourceId);
}


public void showTopPopup(View anchorView) {
    // Inflate popup layout
    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    View popupView = inflater.inflate(R.layout.popup_top_menu, null);

    // Create PopupWindow
    PopupWindow popupWindow = new PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
    );

    // Set animation
  //  popupWindow.setAnimationStyle(R.style.PopupAnimation);


    // Calculate status bar height
    int statusBarHeight = getStatusBarHeight();

    // Show at top of screen
    popupWindow.showAtLocation(
            anchorView,
            Gravity.TOP | Gravity.START,
            0,
            statusBarHeight
    );

    // Handle menu clicks
    popupView.findViewById(R.id.home).setOnClickListener(v -> {
        // Handle menu item 1
        popupWindow.dismiss();
    });
    popupView.findViewById(R.id.bWorkoutlog).setOnClickListener(v -> {
        // Handle menu item 1
        openWorkoutItemLog();
        popupWindow.dismiss();
    });
    popupView.findViewById(R.id.bAnalytics).setOnClickListener(v -> {
        // Handle menu item 1
        openAnalytics();
        popupWindow.dismiss();
    });

    popupView.findViewById(R.id.bRateUs).setOnClickListener(v -> {
        // Handle menu item 1
        openRateApp();
        popupWindow.dismiss();
    });
    popupView.findViewById(R.id.bRecommend).setOnClickListener(v -> {
        // Handle menu item 1
        openRecommend();
        popupWindow.dismiss();
    });
    popupView.findViewById(R.id.bRequestfeatures).setOnClickListener(v -> {
        // Handle menu item 1
        openRequestFeature();
        popupWindow.dismiss();
    });
    popupView.findViewById(R.id.bLanguage).setOnClickListener(v -> {
        // Handle menu item 1
        openLanguage();
        popupWindow.dismiss();
    });

    popupView.findViewById(R.id.bBackground_image).setOnClickListener(v -> {
        // Handle menu item 1
        openBackGroundChange();
        popupWindow.dismiss();
    });

    popupView.findViewById(R.id.bSubscription).setOnClickListener(v -> {
        // Handle menu item 1
        openSubscription();
        popupWindow.dismiss();
    });





    // Add outside touch dismissal
    popupView.setOnTouchListener((v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            popupWindow.dismiss();
            return true;
        }
        return false;
    });
}

private int dpToPx(int dp) {
    return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
}

private void openLanguage()
{
    startActivity(new Intent(Menu_Activity_backup_20250923.this, LanguageSelectionActivity.class));
}
private void openWorkoutItemLog()
{
    startActivity(new Intent(Menu_Activity_backup_20250923.this, item_workout_history.class));
}
private void openTimer()
{
    startActivity(new Intent(Menu_Activity_backup_20250923.this, Menu_Activity_backup_20250923.class));
}


private void forPremiumUser(){
boolean localPremium = premiumManager.isPremiumUser();
Log.d("Menu_Activity", "Local premium status: " + localPremium);
if (localPremium) {
    analytics.setVisibility(View.VISIBLE);
    analytics.setEnabled(true);
    //disable adverts for premium users
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
            startActivity(new Intent(Menu_Activity_backup_20250923.this, PerformanceAnalyticsActivity.class));
            return;
        }

        Intent intent = new Intent(Menu_Activity_backup_20250923.this, SubscriptionActivity.class);
        intent.putExtra("feature_redirect", "analytics");
        startActivity(intent);

    } catch (Exception e) {
        Log.e("Menu_Activity", "Error in openAnalytics: " + e.getMessage(), e);
        try {
            Intent intent = new Intent(Menu_Activity_backup_20250923.this, SubscriptionActivity.class);
            intent.putExtra("feature_redirect", "analytics");
            startActivity(intent);
        } catch (Exception ex) {
            Log.e("Menu_Activity", "Even fallback failed: " + ex.getMessage());
        }
    }
}


private void showLoadingDialog() {
    runOnUiThread(() -> {
        if (!isFinishing() && !isDestroyed()) {
            // Show your loading dialog here
            // progressDialog.show();
        }
    });
}


private void dismissLoadingDialog() {
    runOnUiThread(() -> {
        if (!isFinishing() && !isDestroyed()) {
            // Dismiss your loading dialog here
            // if (progressDialog != null && progressDialog.isShowing()) {
            //     progressDialog.dismiss();
            // }
        }
    });
}
private void openRequestFeature()
{
   // Toast.makeText(Menu_Activity.this, "Request a new Feature: " , Toast.LENGTH_SHORT).show();
    startActivity(new Intent(Menu_Activity_backup_20250923.this, FeatureRequestActivity.class));

}

private void openBackGroundChange() {
    Log.d("Menu_Activity", "openBackGroundChange() called");

    try {
        // First, try a simple check without billing
        boolean localPremium = prefs.getBoolean("is_premium", false);
        Log.d("Menu_Activity", "Simple local premium status: " + localPremium);

        if (localPremium) {
            Log.d("Menu_Activity", "Going directly to Background change");
            startActivity(new Intent(Menu_Activity_backup_20250923.this, ChangeBackgroundActivity.class));
            return;
        }

        // If not premium, go directly to subscription page
        Log.d("Menu_Activity", "Going to Subscription page");
        Intent intent = new Intent(Menu_Activity_backup_20250923.this, SubscriptionActivity.class);
        intent.putExtra("feature_redirect", "background_change");
        startActivity(intent);

    } catch (Exception e) {
        Log.e("Menu_Activity", "Error in openBackGroundChange: " + e.getMessage(), e);
        // Fallback: go to subscription page
        try {
            Intent intent = new Intent(Menu_Activity_backup_20250923.this, SubscriptionActivity.class);
            intent.putExtra("feature_redirect", "background_change");
            startActivity(intent);
        } catch (Exception ex) {
            Log.e("Menu_Activity", "Even fallback failed: " + ex.getMessage());
        }
    }
}
private void openVideo()
{
    Toast.makeText(Menu_Activity_backup_20250923.this, "Open Video: " ,Toast.LENGTH_SHORT).show();
}
private void openSubscription()
{
    //Toast.makeText(Menu_Activity.this, "Open Voice: " , Toast.LENGTH_SHORT).show();
    startActivity(new Intent(Menu_Activity_backup_20250923.this, SubscriptionActivity.class));
}

private void openRecommend()
{

    shareAppViaWhatsApp();

}
private void  openRateApp()
{
    Intent rateIntent = new Intent(Intent.ACTION_VIEW);
    rateIntent.setData(Uri.parse(appURL));
    // rateIntent.setPackage("com.bessadi.fitwod");
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
private void checkAllPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Log.d(TAG, "Android 11+: isExternalStorageManager: " + Environment.isExternalStorageManager());
    }

    int readPermission = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE);
    Log.d(TAG, "READ_EXTERNAL_STORAGE permission: " +
            (readPermission == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        int mediaPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_MEDIA_IMAGES);
        Log.d(TAG, "READ_MEDIA_IMAGES permission: " +
                (mediaPermission == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
    }
}
protected void onDestroy() {
    super.onDestroy();
    if (loadingDialog != null && loadingDialog.isShowing()) {
        loadingDialog.dismiss();
        loadingDialog = null;
    }
}


private void showExitDialog(){
    new AlertDialog.Builder(this)
            .setTitle("Confirm Exit")
            .setMessage("Do you really want to exit?")
            .setIcon(getResources().getDrawable(R.mipmap.ic_launcher)) // display the logo
            .setPositiveButton("Yes", (dialog, which) -> {
                // Action when OK is clicked
                finishActivity(0);
                System.exit(0);
            })
            .setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
}
}