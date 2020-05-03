package com.zamoo.live.network.apis;

import com.zamoo.live.network.model.AllPackage;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PackageApi {

    @GET("get_all_package")
    Call<AllPackage> getAllPackage(@Query("api_secret_key") String key);

}
