package com.zamoo.live;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zamoo.live.network.RetrofitClient;
import com.zamoo.live.network.apis.FirebaseAuthApi;
import com.zamoo.live.network.model.User;
import com.zamoo.live.utils.PreferenceUtils;
import com.zamoo.live.utils.Constants;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FirebaseSignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignInActivity";
    FirebaseAuth firebaseAuth;
    private static final int RC_SIGN_IN_GOOGLE = 123;
    private static int RC_SIGN_IN_PHONE = 124;
    private static int RC_SIGN_IN_FACEBOOK = 125;

    private ProgressBar progressBar;
    private View backgroundView;
    private Button phonAuth, emailAuth, facebookAUth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_sign_up);


        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "sign_up_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        //backgroundView=findViewById(R.id.background_view);
        phonAuth = findViewById(R.id.phoneSignInBtn);
        emailAuth = findViewById(R.id.emailSignInBtn);
        facebookAUth = findViewById(R.id.facebookSignInBtn);

        /*if (isDark) {
            //backgroundView.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
            phonAuth.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
            emailAuth.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
            facebookAUth.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
        }*/

        progressBar = findViewById(R.id.phone_auth_progress_bar);

        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void signInWithPhone(View view) {
        phoneSignIn();
    }

    private void phoneSignIn(){
        progressBar.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
                //already signed in
                if (!FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().isEmpty())
                    sendPhoneDataToServer(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
            }

        } else {
            progressBar.setVisibility(View.GONE);
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.PhoneBuilder().build());
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN_PHONE);
        }

    }

    private void sendPhoneDataToServer(String phone) {
        progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.sendPhoneAuthStatus(Config.API_KEY, uid, phone);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200){
                    if (response.body().getStatus().equals("success")) {
                        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE).edit();
                        editor.putString(Constants.USER_NAME, response.body().getName());
                        editor.putString(Constants.USER_EMAIL, response.body().getEmail());
                        editor.putString(Constants.USER_ID,response.body().getUserId());
                        editor.putString(Constants.USER_PROFILE_IMAGE_URL, response.body().getImageUrl());
                        editor.putString(Constants.USER_GENDER, response.body().getGender());
                        editor.putBoolean(Constants.USER_LOGIN_STATUS,true);
                        editor.apply();
                        editor.commit();

                        Intent intent = new Intent(FirebaseSignUpActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        //save user login time, expire time
                        PreferenceUtils.updateSubscriptionStatus(FirebaseSignUpActivity.this);

                        progressBar.setVisibility(View.GONE);
                        startActivity(intent);
                        finish();
                    }

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                phoneSignIn();
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN_PHONE) {

            final IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!user.getPhoneNumber().isEmpty()) {
                    sendPhoneDataToServer(user.getPhoneNumber());
                } else {
                    //empty
                    phoneSignIn();
                }
            } else {
                // sign in failed
                if (response == null) {
                    //Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    //Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    //Toast.makeText(this, "Error !!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        } else if (requestCode == RC_SIGN_IN_FACEBOOK){
            final IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!user.getUid().isEmpty()) {
                    String username = user.getDisplayName();
                    String photoUrl = String.valueOf(user.getPhotoUrl());
                    String email = user.getEmail();

                    sendFacebookDataToServer(username, photoUrl, email);


                } else {
                    //empty
                    signInWithFacebook();
                }
            } else {
                // sign in failed
                if (response == null) {
                    //Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    //Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    //Toast.makeText(this, "Error !!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        }else if (requestCode == RC_SIGN_IN_GOOGLE) {
            final IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!user.getUid().isEmpty()) {
                    sendGoogleDataToServer(user.getEmail());
                } else {
                    //empty
                    googleSignIn();
                }
            } else {
                // sign in failed
                if (response == null) {
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.PROVIDER_ERROR) {
                    Toast.makeText(this, "Provider error !!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Error !!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void sendFacebookDataToServer(String username, String photoUrl, String email) {
        progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.sendFacebookAuthStatus(Config.API_KEY, uid, username, email, photoUrl);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200){
                    if (response.body().getStatus().equals("success")) {
                        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE).edit();
                        editor.putString(Constants.USER_NAME, response.body().getName());
                        editor.putString(Constants.USER_EMAIL, response.body().getEmail());
                        editor.putString(Constants.USER_ID,response.body().getUserId());
                        editor.putString(Constants.USER_PROFILE_IMAGE_URL, response.body().getImageUrl());
                        editor.putString(Constants.USER_GENDER, response.body().getGender());
                        editor.putBoolean(Constants.USER_LOGIN_STATUS,true);
                        editor.apply();
                        editor.commit();

                        Intent intent = new Intent(FirebaseSignUpActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        //save user login time, expire time
                        PreferenceUtils.updateSubscriptionStatus(FirebaseSignUpActivity.this);

                        progressBar.setVisibility(View.GONE);
                        startActivity(intent);
                        finish();
                    }

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                signInWithFacebook();
            }
        });
    }

    public void signInWithEmail(View view) {
        startActivity(new Intent(FirebaseSignUpActivity.this, LoginActivity.class));
    }


    public void signInWithFacebook(View view) {
       //startActivity(new Intent(FirebaseSignUpActivity.this, FacebookSignInActivity.class));
        signInWithFacebook();
    }

    private void signInWithFacebook() {
        progressBar.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
                //already signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!user.getUid().isEmpty()) {
                    String username = user.getDisplayName();
                    String photoUrl = String.valueOf(user.getPhotoUrl());
                    String email = user.getEmail();

                    sendFacebookDataToServer(username, email, photoUrl);
                }

            }

        } else {
            progressBar.setVisibility(View.GONE);
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.FacebookBuilder()
                            .setPermissions(Arrays.asList("public_profile"))
                            .build());
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN_FACEBOOK);
        }
    }

    public void signInWithGoogle(View view) {
        googleSignIn();
    }

    private void googleSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
                //already signed in
                sendGoogleDataToServer(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }

        } else {
            progressBar.setVisibility(View.GONE);
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build());
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN_GOOGLE);
        }
    }

    private void sendGoogleDataToServer(String email) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.sendGoogleAuthStatus(Config.API_KEY, uid, email,  user.getDisplayName(), user.getPhotoUrl());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {
                        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE).edit();
                        editor.putString(Constants.USER_NAME, response.body().getName());
                        editor.putString(Constants.USER_EMAIL, response.body().getEmail());
                        editor.putString(Constants.USER_ID, response.body().getUserId());
                        editor.putString(Constants.USER_PROFILE_IMAGE_URL, response.body().getImageUrl());
                        editor.putString(Constants.USER_GENDER, response.body().getGender());
                        editor.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        editor.apply();
                        editor.commit();

                        Intent intent = new Intent(FirebaseSignUpActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        //save user login time, expire time
                        PreferenceUtils.updateSubscriptionStatus(FirebaseSignUpActivity.this);

                        progressBar.setVisibility(View.GONE);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                googleSignIn();
            }
        });
    }
}


