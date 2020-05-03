package com.zamoo.live.utils;

import android.os.Environment;

import java.io.File;

public class Constants {

    //you may change bellow configuration from admin panel, so no need to make any changes
    //public static String NAV_MENU_STYLE = "grid"; // or "vertical"
    public static boolean IS_ENABLE_PROGRAM_GUIDE = false;
    public static boolean IS_LOGIN_MANDATORY = false;

    public static boolean IS_GENRE_SHOW = true;
    public static boolean IS_COUNTRY_SHOW = true;

    public static final String ADMOB = "admob";
    public static final String START_APP = "startApp";
    public static final String NETWORK_AUDIENCE = "fan";

    public static boolean SECURED_DOWNLOAD;

    public static String workId;

    public static String DOWNLOAD_DIR = Environment.getExternalStorageDirectory().toString()+File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator;

    public static final String USER_DATA= "user_data";
    public static final String USER_ID = "user_id";
    public static final String USER_EMAIL = "user_email";
    public static final String USER_NAME = "user_name";
    public static final String USER_PASSWORD = "user_password";
    public static final String USER_LOGIN_STATUS = "login_status";
    public static final String USER_PROFILE_IMAGE_URL = "profileImageUrl";
    public static final String USER_GENDER = "user_gender";

    public static final String SUBSCRIPTION_STATUS = "activeStatus";
    public static final String SUBSCRIPTION_PACKAGE_TITLE= "package_title";
    public static final String SUBSCRIPTION_EXPIRE_DATE = "expire_date";
    public static final String EXPIRE_TIME = "current_time";
    public static final String EVENT_LIST = "event_list";

    //app config res
    public static final String APP_CONFIG = "appConfig";
    public static final String NAV_MENU_STYLE = "navMenuStyle";
    public static final String ENABLE_PROGRAM_GUIDE = "enableProgramGuide";
    public static final String LOGIN_MANDATORY = "loginMandatory";
    public static final String GENRE_SHOW = "genreShow";
    public static final String COUNTRY_SHOW = "countryShow";
    public static final String SECURE_DOWNLOAD = "securedDownload";
    public static final String ADS_ENABLE = "ads_enable";
    public static final String MOBILE_ADS_NETWORK = "mobile_ads_network";
    public static final String ADMOB_APP_ID = "admob_app_id";
    public static final String ADMOB_BANNER_ID = "admob_banner_ads_id";
    public static final String ADMOB_INTERSTITIAL_ID = "admob_interstitial_ads_id";
    public static final String FAN_NATIVE_ID = "fan_native_ads_placement_id";
    public static final String FAN_BANNER_ID = "fan_banner_ads_placement_id";
    public static final String FAN_INTERSTITIAL_ID = "fan_interstitial_ads_placement_id";
    public static final String STARTAPP_APP_ID = "startapp_app_id";


    //public static SimpleExoPlayer radioPlayer = null;

}