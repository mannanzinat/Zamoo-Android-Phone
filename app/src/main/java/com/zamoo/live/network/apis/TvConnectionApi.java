package com.zamoo.live.network.apis;

import com.zamoo.live.network.model.TvConnection;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TvConnectionApi {

    @GET("get_tv_connection_code")
    Call<TvConnection> getConnectionCode(@Query("api_secret_key") String apiKey,
                                         @Query("id") String userId);
}
