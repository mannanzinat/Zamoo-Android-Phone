package com.zamoo.live;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.zamoo.live.utils.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SplashscreenActivity extends AppCompatActivity {

    private int SPLASH_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splashscreen);

        createKeyHash(this, "com.oxoo.spagreen");

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(SPLASH_TIME);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (isLogedIn()) {
                        Intent intent = new Intent(SplashscreenActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                        finish();
                    } else {

                        if (isLoginMandetory()) {
                            startActivity(new Intent(SplashscreenActivity.this,FirebaseSignUpActivity.class));

                        } else {
                            Intent intent = new Intent(SplashscreenActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        }

                        finish();
                    }

                }
            }
        };
        timer.start();

    }


    public boolean isLogedIn() {
        SharedPreferences preferences = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE);
        return preferences.getBoolean(Constants.USER_LOGIN_STATUS, false);
    }

    public boolean isLoginMandetory(){
        SharedPreferences preferences = getSharedPreferences(Constants.APP_CONFIG, MODE_PRIVATE);
        return preferences.getBoolean(Constants.LOGIN_MANDATORY, false);
    }

    public static void createKeyHash(Activity activity, String yourPackage) {
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(yourPackage, PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
