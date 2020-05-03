package com.zamoo.live;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.zamoo.live.utils.PreferenceUtils;
import com.zamoo.live.utils.ApiResources;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.ToastMsg;
import com.zamoo.live.utils.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {


    private EditText etEmail,etPass;
    private TextView tvSignUp,tvReset;
    private Button btnLogin;
    private ProgressDialog dialog;
    private View backgroundView;

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
        setContentView(R.layout.activity_login);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Back to login with phone");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "login_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etEmail=findViewById(R.id.email);
        etPass=findViewById(R.id.password);
        tvSignUp=findViewById(R.id.signup);
        btnLogin=findViewById(R.id.signin);
        tvReset=findViewById(R.id.reset_pass);
        backgroundView=findViewById(R.id.background_view);
        if (isDark) {
            backgroundView.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
            btnLogin.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
        }

        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, PassResetActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isValidEmailAddress(etEmail.getText().toString())){
                    new ToastMsg(LoginActivity.this).toastIconError("Please enter valid email");
                }
                else if(etPass.getText().toString().equals("")){
                    new ToastMsg(LoginActivity.this).toastIconError("Please enter password");
                }else {
                    String email = "&&email="+etEmail.getText().toString();
                    String pass = "&&password="+etPass.getText().toString();
                    String url = new ApiResources().getLogin()+email+pass;
                    login(url, etPass.getText().toString());
                }
            }
        });

        tvSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }

        });
    }

    private void login(String url, final String pass){
        url = url.replaceAll(" ", "%20");
        dialog.show();
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                dialog.cancel();
                try {
                    if (response.getString("status").equals("success")){

                        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE).edit();
                        editor.putString(Constants.USER_NAME, response.getString("name"));
                        editor.putString(Constants.USER_EMAIL, response.getString("email"));
                        editor.putString(Constants.USER_ID,response.getString("user_id"));
                        editor.putString(Constants.USER_PROFILE_IMAGE_URL, response.getString("image_url"));
                        editor.putString(Constants.USER_PASSWORD, pass);
                        editor.putString(Constants.USER_GENDER, response.getString("gender"));
                        editor.putBoolean(Constants.USER_LOGIN_STATUS,true);
                        editor.apply();
                        editor.commit();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        //save user login time, expire time
                        PreferenceUtils.updateSubscriptionStatus(LoginActivity.this);

                        startActivity(intent);
                        finish();
                    }else if (response.getString("status").equals("error")){
                        new ToastMsg(LoginActivity.this).toastIconError(response.getString("data"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.cancel();
                new ToastMsg(LoginActivity.this).toastIconError(getString(R.string.error_toast));
            }
        });
        new VolleySingleton(LoginActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }


}
