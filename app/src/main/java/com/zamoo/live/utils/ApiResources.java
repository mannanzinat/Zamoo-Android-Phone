package com.zamoo.live.utils;

import com.zamoo.live.Config;

public class ApiResources {
    public static String adStatus="0", adType = "",adMobBannerId="null", adMobInterstitialId="null", adMobPublisherId="null";

    public static String CURRENCY; // must be valid currency code
    public static double POUND_TO_NGN_EXCHANGE_RATE = 444.33;

    public static String PAYPAL_CLIENT_ID;

    // Create your Rave live keys from the Rave Dashboard and put them below when you are about to publish on Play Store
    public static String RAVE_PUBLIC_KEY;
    public static String RAVE_ENCRYPTION_KEY;

    public static final String RAVE_COUNTRY_CODE = "NG"; // This country code support every currency except TZS,ZAR,KES,GHS.
    public static final boolean RAVE_STAGING_MODE = true; // make it false if you are using rave live keys and publish app to play store.

    public static String PAY_STACK_PUBLIC_KEY;


    String URL = Config.API_SERVER_URL;

    String API_SECRECT_KEY = "api_secret_key="+ Config.API_KEY;


    String slider = URL+"get_slider?"+API_SECRECT_KEY;
    String latest_movie = URL+"get_latest_movies?"+API_SECRECT_KEY;
    String get_movie = URL+"get_movies?"+API_SECRECT_KEY+"&&page=";
    String get_featured_tv = URL+"get_featured_tv_channel?"+API_SECRECT_KEY;
    String get_live_tv = URL+"get_featured_tv_channel?"+ API_SECRECT_KEY+"&&page=";

    String featuredRadio = URL + "get_featured_radio?" + API_SECRECT_KEY;

    String featuredEvent = URL + "get_event?" + API_SECRECT_KEY;

    String latestTvSeries = URL+"get_latest_tvseries?"+API_SECRECT_KEY;

    String tvSeries = URL+"get_tvseries?"+API_SECRECT_KEY+"&&page=";

    String allCountry = URL+"get_all_country?"+API_SECRECT_KEY;

    String allGenre = URL+"get_all_genre?"+API_SECRECT_KEY;

    String details = URL+"get_single_details?"+API_SECRECT_KEY;

    String movieByCountry = URL+"get_movie_by_country_id?"+API_SECRECT_KEY;
    String movieByGenre = URL+"get_movie_by_genre_id?"+API_SECRECT_KEY;
    String login = URL+"login?"+API_SECRECT_KEY;
    String signup = URL+"signup?"+API_SECRECT_KEY;

    String searchUrl = URL+"search?"+API_SECRECT_KEY;
    String favoriteUrl = URL+"get_favorite?"+API_SECRECT_KEY;

    String passResetUrl = URL+"password_reset?"+API_SECRECT_KEY+"&&email=";
    String profileUpdateURL = URL+"update_profile?"+API_SECRECT_KEY;
    String profileURL = URL+"get_user_details_by_email?"+API_SECRECT_KEY+"&&email=";
    String addFav = URL+"add_favorite?"+API_SECRECT_KEY;

    String favStatusURl = URL+"verify_favorite_list?"+API_SECRECT_KEY;
    String removeFav = URL+"remove_favorite?"+API_SECRECT_KEY;

    String addComment = URL+"add_comments?"+API_SECRECT_KEY;

    String commentsURL = URL+"get_all_comments?"+API_SECRECT_KEY;

    String addReplyURL = URL+"add_replay?"+API_SECRECT_KEY;
    String getAllReply = URL+"get_all_replay?"+API_SECRECT_KEY;
    String termsURL = Config.TERMS_URL;
    String genreMovieURL = URL+"get_features_genre_and_movie?"+API_SECRECT_KEY;
    String adDetails = URL+"get_ads?"+API_SECRECT_KEY;

    //for radio
    String getAllRadio = URL + "get_all_radio_by_category?" + API_SECRECT_KEY;

    public String getAdDetails() {
        return adDetails;
    }

    public String getGenreMovieURL() {
        return genreMovieURL;
    }

    public String getTermsURL() {
        return termsURL;
    }

    public String getGetAllReply() {
        return getAllReply;
    }

    public String getAddReplyURL() {
        return addReplyURL;
    }

    public String getCommentsURL() {
        return commentsURL;
    }

    public String getAddComment() {
        return addComment;
    }

    public String getRemoveFav() {
        return removeFav;
    }

    public String getFavStatusURl() {
        return favStatusURl;
    }

    public String getAddFav() {
        return addFav;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public String getProfileUpdateURL() {
        return profileUpdateURL;
    }

    public String getPassResetUrl() {
        return passResetUrl;
    }

    public String getFavoriteUrl() {
        return favoriteUrl;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public String getLogin() {
        return login;
    }

    public String getSignup() {
        return signup;
    }

    public String getMovieByCountry() {
        return movieByCountry;
    }

    public String getMovieByGenre() {
        return movieByGenre;
    }

    public String getDetails() {
        return details;
    }

    public String getAllGenre() {
        return allGenre;
    }

    public String getAllCountry() {
        return allCountry;
    }

    public String getTvSeries() {
        return tvSeries;
    }

    public String getLatestTvSeries() {
        return latestTvSeries;
    }

    public String getSlider() {
        return slider;
    }

    public String getGet_live_tv() {
        return get_live_tv;
    }

    public String getGet_featured_tv() {
        return get_featured_tv;
    }

    public String getLatest_movie() {
        return latest_movie;
    }

    public String getGet_movie() {
        return get_movie;
    }

    public String getGetAllRadio() {
        return getAllRadio;
    }

    public String getFeaturedRadio() {
        return featuredRadio;
    }

    public String getFeaturedEvent() {
        return featuredEvent;
    }
}