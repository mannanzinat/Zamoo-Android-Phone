package com.zamoo.live.network.apis;

import android.net.Uri;

import com.zamoo.live.network.model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface FirebaseAuthApi {
    @FormUrlEncoded
    @POST("firebase_auth")
    Call<User> sendGoogleAuthStatus(@Field("api_secret_key") String key,
                                    @Field("uid") String uid,
                                    @Field("email") String email,
                                    @Field("name") String name,
                                    @Field("image_url") Uri image);

    @FormUrlEncoded
    @POST("firebase_auth")
    Call<User> sendPhoneAuthStatus(@Field("api_secret_key") String key,
                                   @Field("uid") String uid,
                                   @Field("phone") String phoneNo);
    @FormUrlEncoded
    @POST("firebase_auth")
    Call<User> sendFacebookAuthStatus(@Field("api_secret_key") String key,
                                      @Field("uid") String uid,
                                      @Field("name") String name,
                                      @Field("email") String email,
                                      @Field("photo_url") String photoUrl);
}
