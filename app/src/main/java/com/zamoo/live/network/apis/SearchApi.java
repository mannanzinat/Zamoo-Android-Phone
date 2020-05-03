package com.zamoo.live.network.apis;

import com.zamoo.live.network.model.SearchModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SearchApi {

    @GET("search")
    Call<SearchModel> getSearchData(@Query("api_secret_key") String apiSecreteKey,
                                    @Query("q") String query, @Query("type") String type,
                                    @Query("range") String range);

}
