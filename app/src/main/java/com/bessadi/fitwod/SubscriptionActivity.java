package com.bessadi.fitwod;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bessadi.fitwod.R;

public class SubscriptionActivity extends AppCompatActivity {
    private LinearLayout monthlyOption, yearlyOption, lifeTimeOption;
    private PremiumManager premiumManager;
    private Button subscribeButton;
    private ProgressDialog loadingDialog;
    private SharedPreferences prefs;
    private BroadcastReceiver premiumUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
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

        prefs = getSharedPreferences("premium_prefs", Context.MODE_PRIVATE);
        premiumManager = new PremiumManager(this);

        monthlyOption = findViewById(R.id.monthly_option);
        yearlyOption = findViewById(R.id.yearly_option);
        lifeTimeOption = findViewById(R.id.life_time_option);
        subscribeButton = findViewById(R.id.btn_subscribe);

        subscribeButton.setOnClickListener(v -> launchPurchaseFlow());

        setupOptionListeners();
        selectOption(monthlyOption, "monthly");
        premiumManager.setMonthlySubscription();
        setupPremiumUpdateReceiver();
    }

    private void setupPremiumUpdateReceiver() {
        premiumUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkAndRedirect();
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(premiumUpdateReceiver, new IntentFilter("PREMIUM_STATUS_UPDATED"));
    }

    private void launchPurchaseFlow() {
        Log.d("SubscriptionActivity", "Launching purchase flow");
        showLoadingDialog();
        premiumManager.launchPurchaseFlow(this);

        // Auto-dismiss after 30 seconds
        new Handler().postDelayed(() -> {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                Log.d("SubscriptionActivity", "Purchase timeout - dismissing dialog");
                dismissLoadingDialog();
            }
        }, 30000);
    }

    private void setupOptionListeners() {
        monthlyOption.setOnClickListener(v -> {
            selectOption(monthlyOption, "monthly");
            premiumManager.setMonthlySubscription();
        });

        yearlyOption.setOnClickListener(v -> {
            selectOption(yearlyOption, "yearly");
            premiumManager.setYearlySubscription();
        });

        lifeTimeOption.setOnClickListener(v -> {
            selectOption(lifeTimeOption, "lifetime");
            premiumManager.setLifetimePurchase();
        });
    }

    private void selectOption(LinearLayout selectedOption, String planType) {
        monthlyOption.setBackgroundResource(R.drawable.subscription_option_bg);
        yearlyOption.setBackgroundResource(R.drawable.subscription_option_bg);
        lifeTimeOption.setBackgroundResource(R.drawable.subscription_option_bg);
        selectedOption.setBackgroundResource(R.drawable.subscription_option_bg_selected);
    }

    private void checkAndRedirect() {
        boolean isPremium = premiumManager.isPremiumUser() || prefs.getBoolean("is_premium", false);
        Log.d("SubscriptionActivity", "checkAndRedirect - Premium: " + isPremium);

        if (isPremium) {
            Log.d("SubscriptionActivity", "User is premium, redirecting...");
            dismissLoadingDialog();
            redirectToFeature();
        }

    }

    private void redirectToFeature() {
        String feature = getIntent().getStringExtra("feature_redirect");
        Log.d("SubscriptionActivity", "Redirecting to: " + feature);
        if ("analytics".equals(feature)) {
            startActivity(new Intent(this, PerformanceAnalyticsActivity.class));
        } else if ("background_change".equals(feature)) {
            startActivity(new Intent(this, ChangeBackgroundActivity.class));
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("SubscriptionActivity", "onResume");
        checkAndRedirect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (premiumUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(premiumUpdateReceiver);
        }
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(this);
            loadingDialog.setMessage("Processing purchase...");
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}