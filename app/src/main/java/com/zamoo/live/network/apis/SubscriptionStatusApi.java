package com.zamoo.live.network.apis;

import com.zamoo.live.network.model.SubscriptionStatus;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SubscriptionStatusApi {
    @GET("check_user_subscription_status")
    Call<SubscriptionStatus> getSubscriptionStatus(@Query("api_secret_key") String api_key,
                                                   @Query("user_id") String userId);

}
