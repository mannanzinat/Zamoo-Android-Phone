package com.zamoo.live.network.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Config {

    @SerializedName("currency_symbol")
    @Expose
    private String currencySymbol;
    @SerializedName("paypal_email")
    @Expose
    private String paypalEmail;
    @SerializedName("paypal_client_id")
    @Expose
    private String paypalClientId;

    @SerializedName("stripe_publishable_key")
    @Expose
    private String stripePublishableKey;

    @SerializedName("stripe_secret_key")
    @Expose
    private String stripeSecretKey;

    @SerializedName("currency")
    @Expose
    private String currency;


    @SerializedName("reve_public_key")
    @Expose
    private String ravePublicKey;

    @SerializedName("reve_encryption_key")
    @Expose
    private String raveEncKey;

    @SerializedName("reve_secret_key")
    @Expose
    private String raveSecretKey;

    @SerializedName("play_stack_public_key")
    @Expose
    private String payStackPublicKey;


    public String getPaypalClientId() {
        return paypalClientId;
    }

    public void setPaypalClientId(String paypalClientId) {
        this.paypalClientId = paypalClientId;
    }

    public String getRavePublicKey() {
        return ravePublicKey;
    }

    public void setRavePublicKey(String ravePublicKey) {
        this.ravePublicKey = ravePublicKey;
    }

    public String getRaveEncKey() {
        return raveEncKey;
    }

    public void setRaveEncKey(String raveEncKey) {
        this.raveEncKey = raveEncKey;
    }

    public String getRaveSecretKey() {
        return raveSecretKey;
    }

    public void setRaveSecretKey(String raveSecretKey) {
        this.raveSecretKey = raveSecretKey;
    }

    public String getPayStackPublicKey() {
        return payStackPublicKey;
    }

    public void setPayStackPublicKey(String payStackPublicKey) {
        this.payStackPublicKey = payStackPublicKey;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getPaypalEmail() {
        return paypalEmail;
    }

    public void setPaypalEmail(String paypalEmail) {
        this.paypalEmail = paypalEmail;
    }

    public String getStripePublishableKey() {
        return stripePublishableKey;
    }

    public void setStripePublishableKey(String stripePublishableKey) {
        this.stripePublishableKey = stripePublishableKey;
    }

    public String getStripeSecretKey() {
        return stripeSecretKey;
    }

    public void setStripeSecretKey(String stripeSecretKey) {
        this.stripeSecretKey = stripeSecretKey;
    }

    @Override
    public String toString() {
        return "Config{" +
                "currencySymbol='" + currencySymbol + '\'' +
                ", paypalEmail='" + paypalEmail + '\'' +
                ", paypalClientId='" + paypalClientId + '\'' +
                ", stripePublishableKey='" + stripePublishableKey + '\'' +
                ", stripeSecretKey='" + stripeSecretKey + '\'' +
                ", currency='" + currency + '\'' +
                ", ravePublicKey='" + ravePublicKey + '\'' +
                ", raveEncKey='" + raveEncKey + '\'' +
                ", raveSecretKey='" + raveSecretKey + '\'' +
                ", payStackPublicKey='" + payStackPublicKey + '\'' +
                '}';
    }
}
