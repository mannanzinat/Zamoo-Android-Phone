package com.zamoo.live.network.apis;

import com.zamoo.live.network.model.ItemCountry;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ItemCountryApi {

    @GET("get_content_by_country_id")
    Call<ItemCountry> getContentByCountryId(@Query("api_secret_key") String apiKey,
                                            @Query("id") String id);
}
