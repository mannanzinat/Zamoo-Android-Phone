package com.zamoo.live.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.zamoo.live.Config;
import com.zamoo.live.network.RetrofitClient;
import com.zamoo.live.network.apis.SubscriptionStatusApi;
import com.zamoo.live.network.model.SubscriptionStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceUtils {
    public static final String TAG = "PreferenceUtils";


    public static boolean isActivePlan(Context context) {
        String status = getSubscriptionStatus(context);
        return status.equals("active");
    }

    public static boolean isLoggedIn(Context context){
        SharedPreferences preferences = context.getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE);
        return preferences.getBoolean(Constants.USER_LOGIN_STATUS,false);
    }

    public static boolean isMandatoryLogin(Context context){
        SharedPreferences preferences = context.getSharedPreferences(Constants.APP_CONFIG, MODE_PRIVATE);
        return preferences.getBoolean(Constants.LOGIN_MANDATORY,false);
    }

    private static String getSubscriptionStatus(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SUBSCRIPTION_STATUS, MODE_PRIVATE);
        return preferences.getString(Constants.SUBSCRIPTION_STATUS,"");
    }

    public static long getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String currentDateandTime = sdf.format(new Date());

        Date date = null;
        try {
            date = sdf.parse(currentDateandTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.getTimeInMillis();
    }

    public static long getExpireTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd:HH:mm");
        String currentDateandTime = sdf.format(new Date());

        Date date = null;
        try {
            date = sdf.parse(currentDateandTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, 1);

        return calendar.getTimeInMillis();
    }

    public static boolean isValid(Context context){
        String savedTime = getUpdateTime(context);
        long currentTime = getCurrentTime();

        if (Long.parseLong(savedTime) < currentTime){
            return false;
        }

        return true;
    }

    private static String getUpdateTime(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SUBSCRIPTION_STATUS, MODE_PRIVATE);
        String time = preferences.getString(Constants.EXPIRE_TIME, "0");

        return time;
    }


    public static void updateSubscriptionStatus(final Context context) {
        //get saved user id
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE);
        String userId = sharedPreferences.getString(Constants.USER_ID, "");
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionStatusApi api = retrofit.create(SubscriptionStatusApi.class);
        Call<SubscriptionStatus> call = api.getSubscriptionStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<SubscriptionStatus>() {
            @Override
            public void onResponse(Call<SubscriptionStatus> call, Response<SubscriptionStatus> response) {
                if (response.isSuccessful()) {
                    if (response.code() == 200) {

                        //get subscription details
                        String status = response.body().getStatus();
                        String package_title = response.body().getPackageTitle();
                        String expire_date = response.body().getExpireDate();
                        List<String> eventList = new ArrayList<>();
                        eventList = response.body().getEventList();
                        Gson gson = new Gson();
                        String jsonEvents = gson.toJson(eventList);

                        //now save to sharedPreference
                        SharedPreferences.Editor sp = context.getSharedPreferences(Constants.SUBSCRIPTION_STATUS, MODE_PRIVATE).edit();
                        sp.putString(Constants.EXPIRE_TIME, String.valueOf(getExpireTime()));
                        sp.putString(Constants.SUBSCRIPTION_STATUS, status);
                        sp.putString(Constants.SUBSCRIPTION_PACKAGE_TITLE, package_title);
                        sp.putString(Constants.SUBSCRIPTION_EXPIRE_DATE, expire_date);
                        sp.putString(Constants.EVENT_LIST, jsonEvents);

                        sp.apply();
                        sp.commit();

                    }

                }
            }

            @Override
            public void onFailure(Call<SubscriptionStatus> call, Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());

            }
        });
    }

    public static void clearSubscriptionSavedData(Context context){
        //now save to sharedPreference
        SharedPreferences.Editor sp = context.getSharedPreferences(Constants.SUBSCRIPTION_STATUS, MODE_PRIVATE).edit();
        sp.putString(Constants.EXPIRE_TIME, null);
        sp.putString(Constants.SUBSCRIPTION_STATUS, null);
        sp.putString(Constants.SUBSCRIPTION_PACKAGE_TITLE, null);
        sp.putString(Constants.SUBSCRIPTION_EXPIRE_DATE, null);
        sp.putString(Constants.EVENT_LIST, null);

        sp.apply();
        sp.commit();
    }

    public static String getEvents(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.SUBSCRIPTION_STATUS, MODE_PRIVATE);

        return preferences.getString(Constants.EVENT_LIST, "");
    }

}
