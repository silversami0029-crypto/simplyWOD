package com.example.fitwod;
import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;

public class AdManager {
    private RewardedAd rewardedAd;
    private PreferenceManager prefs;
    private Context context;

    public AdManager(Context context) {
        this.context = context;
        this.prefs = new PreferenceManager(context);
    }

    public void initializeAds() {
        if (!prefs.isPremiumPurchased()) {
            MobileAds.initialize(context);
            loadRewardedAd();
        }
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(context, "ca-app-pub-your-rewarded-ad-unit-id", adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        rewardedAd = ad;
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        rewardedAd = null;
                    }
                });
    }

    public void showAd(Activity activity, Runnable onAdDismissed) {
        if (!prefs.isPremiumPurchased() && rewardedAd != null) {
            rewardedAd.show(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(RewardItem rewardItem) {
                    // Handle reward if needed
                }
            });

            // Note: You might want to set up a listener for when the ad is dismissed
            // This requires a custom callback setup since RewardedAd doesn't have the same callback structure
        } else {
            onAdDismissed.run();
        }
    }

    public void showBannerAd(AdView adView) {
        if (!prefs.isPremiumPurchased()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adView.setVisibility(View.VISIBLE);
        } else {
            adView.setVisibility(View.GONE);
        }
    }
}