package com.zamoo.live.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.google.gson.Gson;
import com.onesignal.OneSignal;
import com.zamoo.live.Config;
import com.zamoo.live.NotificationClickHandler;
import com.zamoo.live.network.RetrofitClient;
import com.zamoo.live.network.apis.AppConfigApi;
import com.zamoo.live.network.apis.PaymentConfigApi;
import com.zamoo.live.network.apis.SubscriptionApi;
import com.zamoo.live.network.model.ActiveStatus;
import com.zamoo.live.network.model.AppConfig;
import com.zamoo.live.network.model.PaymentConfig;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.zamoo.live.utils.PreferenceUtils.getExpireTime;

public class MyAppClass extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "download_channel_id";
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        createNotificationChannel();


        // OneSignal Initialization
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationClickHandler(mContext))
                //.inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        SharedPreferences preferences = getSharedPreferences("push", MODE_PRIVATE);
        if (preferences.getBoolean("status", true)) {
            OneSignal.setSubscription(true);
        } else {
            OneSignal.setSubscription(false);
        }

        //

        if (!getFirstTimeOpenStatus()) {
            if (Config.DEFAULT_DARK_THEME_ENABLE) {
                changeSystemDarkMode(true);
            } else {
                changeSystemDarkMode(false);
            }
            saveFirstTimeOpenStatus(true);
        }

        getAppConfigInfo();

        // save payment config info
        updatePaymentConfig();

        // fetched and save the user active status if user is logged in
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE);
        String userId = sharedPreferences.getString(Constants.USER_ID, "");

        if (userId != null && !userId.equals("")) {
            updateActiveStatus(userId);
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NotificationName",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }


    }


    public void changeSystemDarkMode(boolean dark) {
        SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
        editor.putBoolean("dark", dark);
        editor.apply();
    }

    public void saveFirstTimeOpenStatus(boolean dark) {

        SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
        editor.putBoolean("firstTimeOpen", true);
        editor.apply();

    }

    public boolean getFirstTimeOpenStatus() {
        SharedPreferences preferences = getSharedPreferences("push", MODE_PRIVATE);
        return preferences.getBoolean("firstTimeOpen", false);
    }

    public static Context getContext() {
        return mContext;
    }

    public void getAppConfigInfo() {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        AppConfigApi appConfigApi = retrofit.create(AppConfigApi.class);
        Call<AppConfig> call = appConfigApi.getAppConfig(Config.API_KEY);
        call.enqueue(new Callback<AppConfig>() {
            @Override
            public void onResponse(Call<AppConfig> call, Response<AppConfig> response) {

                if (response.code() == 200) {

                    AppConfig appConfig = response.body();
                    // save app config info to shared preference
                    saveAppConfigInfo(appConfig);
                }

            }

            @Override
            public void onFailure(Call<AppConfig> call, Throwable t) {
                t.printStackTrace();

                SharedPreferences preferences = getSharedPreferences(Constants.APP_CONFIG, MODE_PRIVATE);
                Constants.IS_ENABLE_PROGRAM_GUIDE = preferences.getBoolean(Constants.ENABLE_PROGRAM_GUIDE, false);
                Constants.IS_LOGIN_MANDATORY = preferences.getBoolean(Constants.LOGIN_MANDATORY, false);
                Constants.IS_GENRE_SHOW = preferences.getBoolean(Constants.GENRE_SHOW, true);
                Constants.IS_COUNTRY_SHOW = preferences.getBoolean(Constants.COUNTRY_SHOW, true);

            }
        });

    }

    public void saveAppConfigInfo(AppConfig appConfig) {

        Constants.IS_ENABLE_PROGRAM_GUIDE = appConfig.getProgramGuideEnable();
        Constants.IS_LOGIN_MANDATORY = appConfig.getMandatoryLogin();
        Constants.IS_GENRE_SHOW = appConfig.getGenreVisible();
        Constants.IS_COUNTRY_SHOW = appConfig.getCountryVisible();
        Constants.SECURED_DOWNLOAD = appConfig.isSecuredDownload();

        SharedPreferences.Editor editor = getSharedPreferences(Constants.APP_CONFIG, MODE_PRIVATE).edit();
        editor.putString(Constants.NAV_MENU_STYLE, appConfig.getMenu());
        editor.putBoolean(Constants.ENABLE_PROGRAM_GUIDE, appConfig.getProgramGuideEnable());
        editor.putBoolean(Constants.LOGIN_MANDATORY, appConfig.getMandatoryLogin());
        editor.putBoolean(Constants.GENRE_SHOW, appConfig.getGenreVisible());
        editor.putBoolean(Constants.COUNTRY_SHOW, appConfig.getCountryVisible());
        editor.putBoolean(Constants.SECURE_DOWNLOAD, appConfig.isSecuredDownload());
        editor.putString(Constants.ADS_ENABLE, appConfig.getAdsEnable());
        editor.putString(Constants.MOBILE_ADS_NETWORK, appConfig.getMobileAdsNetwork());
        editor.putString(Constants.ADMOB_APP_ID, appConfig.getAdmobAppId());
        editor.putString(Constants.ADMOB_BANNER_ID, appConfig.getAdmobBannerAdsId());
        editor.putString(Constants.ADMOB_INTERSTITIAL_ID, appConfig.getAdmobInterstitialAdsId());
        editor.putString(Constants.FAN_NATIVE_ID, appConfig.getFanNativeAdsPlacementId());
        editor.putString(Constants.FAN_BANNER_ID, appConfig.getFanBannerAdsPlacementId());
        editor.putString(Constants.FAN_INTERSTITIAL_ID, appConfig.getFanInterstitialAdsPlacementId());
        editor.putString(Constants.STARTAPP_APP_ID, appConfig.getStartappAppId());
        editor.apply();
        editor.commit();

    }

    private void updateActiveStatus(String userId) {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                ActiveStatus activeStatus = response.body();
                saveActiveStatus(activeStatus);
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void saveActiveStatus(ActiveStatus activeStatus) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.SUBSCRIPTION_STATUS, MODE_PRIVATE).edit();
        editor.putString(Constants.SUBSCRIPTION_STATUS, activeStatus.getStatus());
        editor.putString(Constants.SUBSCRIPTION_PACKAGE_TITLE, activeStatus.getPackageTitle());
        editor.putString(Constants.SUBSCRIPTION_EXPIRE_DATE, activeStatus.getExpireDate());
        editor.putString(Constants.EXPIRE_TIME, String.valueOf(getExpireTime()));
        //save event list
        List<String> eventList = new ArrayList<>();
        eventList = activeStatus.getEventList();
        Gson gson = new Gson();
        String jsonEvents = gson.toJson(eventList);

        if (!eventList.isEmpty()) {
            editor.putInt("event_size", eventList.size());

            for (int i = 0; i < eventList.size(); i++) {
                editor.remove("event_" + i);
                editor.putString("event_" + i, eventList.get(i));
            }
        }

        // editor.putStringSet(Constants.EVENT_LIST, set);
        editor.apply();
        editor.commit();
    }

    private void updatePaymentConfig() {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentConfigApi paymentConfigApi = retrofit.create(PaymentConfigApi.class);

        Call<PaymentConfig> call = paymentConfigApi.getPaymentConfigInfo(com.zamoo.live.Config.API_KEY);
        call.enqueue(new Callback<PaymentConfig>() {
            @Override
            public void onResponse(Call<PaymentConfig> call, Response<PaymentConfig> response) {
                PaymentConfig paymentConfig = response.body();
                savePaymentConfigInfo(paymentConfig.getConfig());
            }

            @Override
            public void onFailure(Call<PaymentConfig> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void savePaymentConfigInfo(com.zamoo.live.network.model.Config config) {

        ApiResources.CURRENCY = config.getCurrency();
        ApiResources.PAY_STACK_PUBLIC_KEY = config.getPayStackPublicKey();
        ApiResources.PAYPAL_CLIENT_ID = config.getPaypalClientId();
        ApiResources.RAVE_ENCRYPTION_KEY = config.getRaveEncKey();
        ApiResources.RAVE_PUBLIC_KEY = config.getRavePublicKey();

        SharedPreferences.Editor editor = getSharedPreferences("paymentConfig", MODE_PRIVATE).edit();
        editor.putString("currencySymbol", config.getCurrencySymbol());
        editor.putString("paypalEmail", config.getPaypalEmail());
        editor.putString("stripePublishableKey", config.getStripePublishableKey());
        editor.putString("stripeSecretKey", config.getStripeSecretKey());
        editor.putString("currency", config.getCurrency());
        editor.apply();
    }

}