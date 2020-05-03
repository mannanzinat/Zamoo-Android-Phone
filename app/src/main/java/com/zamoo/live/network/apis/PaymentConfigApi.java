package com.zamoo.live.network.apis;

import com.zamoo.live.network.model.PaymentConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PaymentConfigApi {


    @GET("get_payment_config")
    Call<PaymentConfig> getPaymentConfigInfo(@Query("api_secret_key") String key);

}
