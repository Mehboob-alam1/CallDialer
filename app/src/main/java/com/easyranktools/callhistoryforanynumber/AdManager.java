package com.easyranktools.callhistoryforanynumber;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManager {
    private static final String TAG = "AdManager";

    // Replace with your actual Ad Unit IDs
    public static final String INTERSTITIAL_ID = "ca-app-pub-8028241846578443/1156525109";
    public static final String BANNER_ID = "ca-app-pub-8028241846578443/1052567413";

    private static InterstitialAd mInterstitialAd;
    private static boolean adsRemoved = false; // Later toggle with in-app purchase

    public static void initialize(Context context) {
        MobileAds.initialize(context, initializationStatus -> Log.d(TAG, "AdMob initialized"));
    }

    public static void loadInterstitial(Context context) {
        if (adsRemoved) return;

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(context, INTERSTITIAL_ID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
                Log.d(TAG, "Interstitial loaded");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Log.e(TAG, "Failed to load interstitial: " + loadAdError.getMessage());
                mInterstitialAd = null;
            }
        });
    }

    public static void showInterstitial(Activity activity) {
        if (adsRemoved) return;

        if (mInterstitialAd != null) {
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial closed");
                    loadInterstitial(activity); // Preload next
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.e(TAG, "Failed to show interstitial: " + adError.getMessage());
                }
            });
            mInterstitialAd.show(activity);
        } else {
            Log.d(TAG, "Interstitial not ready");
        }
    }

    public static void loadBanner(Context context, FrameLayout container) {
        if (adsRemoved) {
            container.removeAllViews();
            return;
        }

        AdView adView = new AdView(context);
        adView.setAdUnitId(BANNER_ID);
        adView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        container.removeAllViews();
        container.addView(adView);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public static void removeAds() {
        adsRemoved = true;
    }

    public static boolean areAdsRemoved() {
        return adsRemoved;
    }
}
