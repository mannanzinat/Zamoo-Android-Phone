package com.zamoo.live.utils;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class PopUpAds {

    public static void ShowInterstitialAds(Context context) {

        final InterstitialAd mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(ApiResources.adMobInterstitialId);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                mInterstitialAd.show();

                /*Random rand = new Random();
                int i = rand.nextInt(10)+1;

                Log.e("INTER AD:", String.valueOf(i));

                if (i%2==0){
                    mInterstitialAd.show();
                }*/
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);

            }
        });

//            final InterstitialAd mInterstitial = new InterstitialAd(context);
//            mInterstitial.setAdUnitId(ApiResources.adMobInterstitialId);
//            GDPRChecker.Request request = GDPRChecker.getRequest();
//            AdRequest.Builder builder = new AdRequest.Builder();
//            if (request == GDPRChecker.Request.NON_PERSONALIZED) {
//                Bundle extras = new Bundle();
//                extras.putString("npa", "1");
//                builder.addNetworkExtrasBundle(AdMobAdapter.class, extras);
//            }
//            mInterstitial.loadAd(builder.build());
//
//            mInterstitial.show();
//
//            if (!mInterstitial.isLoaded()) {
//                AdRequest.Builder builder1 = new AdRequest.Builder();
//                if (request == GDPRChecker.Request.NON_PERSONALIZED) {
//                    Bundle extras = new Bundle();
//                    extras.putString("npa", "1");
//                    builder1.addNetworkExtrasBundle(AdMobAdapter.class, extras);
//                }
//                mInterstitial.loadAd(builder1.build());
//            }
//            mInterstitial.setAdListener(new AdListener() {
//                @Override
//                public void onAdLoaded() {
//                    super.onAdLoaded();
//                    mInterstitial.show();
//                }
//            });
    }
}
