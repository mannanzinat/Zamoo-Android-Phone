package com.zamoo.live.network.apis;

import com.zamoo.live.network.model.RadioModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface FeaturedRadioApi {

    @GET("get_featured_radio")
    Call<List<RadioModel>> getFeaturedRadio(@Query("api_secret_key") String key);
}
