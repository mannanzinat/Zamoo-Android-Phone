package com.zamoo.live.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppConfig {

    @SerializedName("menu")
    @Expose
    private String menu;
    @SerializedName("program_guide_enable")
    @Expose
    private Boolean programGuideEnable;
    @SerializedName("mandatory_login")
    @Expose
    private Boolean mandatoryLogin;
    @SerializedName("genre_visible")
    @Expose
    private Boolean genreVisible;
    @SerializedName("country_visible")
    @Expose
    private Boolean countryVisible;
    @SerializedName("ads_enable")
    @Expose
    private String adsEnable;
    @SerializedName("mobile_ads_network")
    @Expose
    private String mobileAdsNetwork;
    @SerializedName("admob_app_id")
    @Expose
    private String admobAppId;
    @SerializedName("admob_banner_ads_id")
    @Expose
    private String admobBannerAdsId;
    @SerializedName("admob_interstitial_ads_id")
    @Expose
    private String admobInterstitialAdsId;
    @SerializedName("fan_native_ads_placement_id")
    @Expose
    private String fanNativeAdsPlacementId;
    @SerializedName("fan_banner_ads_placement_id")
    @Expose
    private String fanBannerAdsPlacementId;
    @SerializedName("fan_interstitial_ads_placement_id")
    @Expose
    private String fanInterstitialAdsPlacementId;
    @SerializedName("startapp_app_id")
    @Expose
    private String startappAppId;

    @SerializedName("secured_download")
    @Expose
    private  boolean securedDownload;

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public Boolean getProgramGuideEnable() {
        return programGuideEnable;
    }

    public void setProgramGuideEnable(Boolean programGuideEnable) {
        this.programGuideEnable = programGuideEnable;
    }

    public Boolean getMandatoryLogin() {
        return mandatoryLogin;
    }

    public void setMandatoryLogin(Boolean mandatoryLogin) {
        this.mandatoryLogin = mandatoryLogin;
    }

    public Boolean getGenreVisible() {
        return genreVisible;
    }

    public void setGenreVisible(Boolean genreVisible) {
        this.genreVisible = genreVisible;
    }

    public Boolean getCountryVisible() {
        return countryVisible;
    }

    public void setCountryVisible(Boolean countryVisible) {
        this.countryVisible = countryVisible;
    }

    public String getAdsEnable() {
        return adsEnable;
    }

    public void setAdsEnable(String adsEnable) {
        this.adsEnable = adsEnable;
    }

    public String getMobileAdsNetwork() {
        return mobileAdsNetwork;
    }

    public void setMobileAdsNetwork(String mobileAdsNetwork) {
        this.mobileAdsNetwork = mobileAdsNetwork;
    }

    public String getAdmobAppId() {
        return admobAppId;
    }

    public void setAdmobAppId(String admobAppId) {
        this.admobAppId = admobAppId;
    }

    public String getAdmobBannerAdsId() {
        return admobBannerAdsId;
    }

    public void setAdmobBannerAdsId(String admobBannerAdsId) {
        this.admobBannerAdsId = admobBannerAdsId;
    }

    public String getAdmobInterstitialAdsId() {
        return admobInterstitialAdsId;
    }

    public void setAdmobInterstitialAdsId(String admobInterstitialAdsId) {
        this.admobInterstitialAdsId = admobInterstitialAdsId;
    }

    public String getFanNativeAdsPlacementId() {
        return fanNativeAdsPlacementId;
    }

    public void setFanNativeAdsPlacementId(String fanNativeAdsPlacementId) {
        this.fanNativeAdsPlacementId = fanNativeAdsPlacementId;
    }

    public String getFanBannerAdsPlacementId() {
        return fanBannerAdsPlacementId;
    }

    public void setFanBannerAdsPlacementId(String fanBannerAdsPlacementId) {
        this.fanBannerAdsPlacementId = fanBannerAdsPlacementId;
    }

    public String getFanInterstitialAdsPlacementId() {
        return fanInterstitialAdsPlacementId;
    }

    public void setFanInterstitialAdsPlacementId(String fanInterstitialAdsPlacementId) {
        this.fanInterstitialAdsPlacementId = fanInterstitialAdsPlacementId;
    }

    public String getStartappAppId() {
        return startappAppId;
    }

    public void setStartappAppId(String startappAppId) {
        this.startappAppId = startappAppId;
    }

    public boolean isSecuredDownload() {
        return securedDownload;
    }

    public void setSecuredDownload(boolean securedDownload) {
        this.securedDownload = securedDownload;
    }
}
