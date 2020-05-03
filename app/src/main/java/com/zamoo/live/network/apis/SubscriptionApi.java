package com.zamoo.live.network.apis;

import com.zamoo.live.network.model.ActiveStatus;
import com.zamoo.live.network.model.SubscriptionHistory;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SubscriptionApi {

    @GET("check_user_subscription_status")
    Call<ActiveStatus> getActiveStatus(@Query("api_secret_key") String key,
                                       @Query("user_id") String userId);

    @GET("get_subscription_history")
    Call<SubscriptionHistory> getSubscriptionHistory(@Query("api_secret_key") String key,
                                                     @Query("user_id") String userId);
    @GET("cancel_subscription")
    Call<ResponseBody> cancelSubscription(@Query("api_secret_key") String key,
                                          @Query("user_id") String userId,
                                          @Query("subscription_id") String subscriptionId);

}
