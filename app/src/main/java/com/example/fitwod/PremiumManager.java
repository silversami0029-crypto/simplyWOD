package com.example.fitwod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

public class PremiumManager {
    // Define all your product IDs (MAKE SURE THESE MATCH GOOGLE PLAY CONSOLE)
    private static final String PRODUCT_MONTHLY = "monthly_subscription";
    private static final String PRODUCT_YEARLY = "premium_yearly";
    private static final String PRODUCT_LIFETIME = "premium_life_time_membership";

    private static final String PREMIUM_STATUS_KEY = "is_premium";
    private static final String PREFS_NAME = "premium_prefs";
    private static final String TAG = "PremiumManager";

    // Track the currently selected product
    private String selectedProductId = PRODUCT_MONTHLY; // Default
    private String selectedSkuType = BillingClient.SkuType.SUBS; // Default to subscription

    private Context context;
    private BillingClient billingClient;
    private SharedPreferences prefs;
    private User currentUser;
    private boolean isConnecting = false;

    public PremiumManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentUser = new User();

        Log.d(TAG, "PremiumManager created");
        setupBillingClient();
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
                .enablePendingPurchases()
                .setListener(new PurchasesUpdatedListener() {
                    @Override
                    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                            handlePurchases(purchases);
                        }
                    }
                })
                .build();

        connectToBillingService();
    }

    private void connectToBillingService() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing service connected");
                    checkPremiumStatus();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected");
            }
        });
    }

    private void handlePurchases(List<Purchase> purchases) {
        Log.d(TAG, "Handling " + purchases.size() + " purchases");

        for (Purchase purchase : purchases) {
            Log.d(TAG, "Purchase: " + purchase.getSkus() + ", State: " + purchase.getPurchaseState() + ", Ack: " + purchase.isAcknowledged());

            if ((purchase.getSkus().contains(PRODUCT_MONTHLY) ||
                    purchase.getSkus().contains(PRODUCT_YEARLY) ||
                    purchase.getSkus().contains(PRODUCT_LIFETIME)) &&
                    purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

                if (!purchase.isAcknowledged()) {
                    Log.d(TAG, "Acknowledging purchase: " + purchase.getSkus());
                    acknowledgePurchase(purchase);
                } else {
                    Log.d(TAG, "Purchase already acknowledged");
                    setPremiumStatus(true);
                    sendPremiumUpdateBroadcast();
                }
            }
        }
    }

    private void acknowledgePurchase(Purchase purchase) {
        Log.d(TAG, "Acknowledging purchase: " + purchase.getPurchaseToken());

        AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
            Log.d(TAG, "Acknowledge result: " + billingResult.getResponseCode());
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully");
                setPremiumStatus(true);
                sendPremiumUpdateBroadcast();
            } else {
                Log.e(TAG, "Failed to acknowledge purchase: " + billingResult.getResponseCode());
            }

        });
    }

    private void sendPremiumUpdateBroadcast() {
        Intent intent = new Intent("PREMIUM_STATUS_UPDATED");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void checkPremiumStatus() {
        // Check subscriptions
        QueryPurchasesParams subsParams = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build();

        billingClient.queryPurchasesAsync(subsParams, (billingResult, purchases) -> {
            boolean hasSubscription = checkPurchasesForPremium(purchases, PRODUCT_MONTHLY) ||
                    checkPurchasesForPremium(purchases, PRODUCT_YEARLY);

            if (hasSubscription) {
                setPremiumStatus(true);
            } else {
                // Check in-app purchases
                QueryPurchasesParams inappParams = QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build();

                billingClient.queryPurchasesAsync(inappParams, (billingResult2, purchases2) -> {
                    boolean hasLifetime = checkPurchasesForPremium(purchases2, PRODUCT_LIFETIME);
                    setPremiumStatus(hasLifetime);
                });
            }
        });
    }

    private boolean checkPurchasesForPremium(List<Purchase> purchases, String productId) {
        for (Purchase purchase : purchases) {
            if (purchase.getSkus().contains(productId) && purchase.isAcknowledged()) {
                return true;
            }
        }
        return false;
    }

    public boolean isPremiumUser() {
        boolean isPremium = prefs.getBoolean(PREMIUM_STATUS_KEY, false);
        Log.d(TAG, "isPremiumUser: " + isPremium);
        return isPremium;
    }

    public void setPremiumStatus(boolean isPremium) {
        prefs.edit().putBoolean(PREMIUM_STATUS_KEY, isPremium).apply();
        Log.d(TAG, "Premium status set to: " + isPremium);
    }

    // Product selection methods
    public void setMonthlySubscription() {
        selectedProductId = PRODUCT_MONTHLY;
        selectedSkuType = BillingClient.SkuType.SUBS;
    }

    public void setYearlySubscription() {
        selectedProductId = PRODUCT_YEARLY;
        selectedSkuType = BillingClient.SkuType.SUBS;
    }

    public void setLifetimePurchase() {
        selectedProductId = PRODUCT_LIFETIME;
        selectedSkuType = BillingClient.SkuType.INAPP;
    }

    public void launchPurchaseFlow(Activity activity) {
        if (billingClient.isReady()) {
            launchPurchaseFlowInternal(activity, selectedProductId, selectedSkuType);
        }
    }

    private void launchPurchaseFlowInternal(Activity activity, String productId, String skuType) {
        List<String> skuList = new ArrayList<>();
        skuList.add(productId);

        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(skuType)
                .build();

        billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> skuDetailsList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null && !skuDetailsList.isEmpty()) {
                    SkuDetails skuDetails = skuDetailsList.get(0);
                    BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build();
                    billingClient.launchBillingFlow(activity, billingFlowParams);
                }
            }
        });
    }

    public void checkPremiumStatusWithCallback(Activity activity, Runnable callback) {
        if (billingClient.isReady()) {
            checkPremiumStatus();
        }
        new Handler(Looper.getMainLooper()).postDelayed(callback, 1000);
    }

    public void forcePremiumStatusCheck() {
        if (billingClient.isReady()) {
            checkPremiumStatus();
        }
    }

    public static void openSubscriptionManagement(Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/account/subscriptions"));
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "Unable to open subscription management", Toast.LENGTH_SHORT).show();
        }
    }

    public void destroy() {
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }
}