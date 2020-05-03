package com.zamoo.live;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.zamoo.live.network.RetrofitClient;
import com.zamoo.live.network.apis.TvConnectionApi;
import com.zamoo.live.network.model.TvConnection;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.Tools;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchCompat,switcDarkMode;
    private TextView tvTerms;
    private LinearLayout shareLayout;
    private LinearLayout tvConnectLayout;
    private ProgressBar progressBar;

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
        setContentView(R.layout.activity_settings);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "settings_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        switchCompat=findViewById(R.id.notify_switch);
        tvTerms=findViewById(R.id.tv_term);
        shareLayout=findViewById(R.id.share_layout);
        tvConnectLayout = findViewById(R.id.tv_connect_layout);
        progressBar = findViewById(R.id.code_progress);


        SharedPreferences preferences=getSharedPreferences("push",MODE_PRIVATE);
        if (preferences.getBoolean("status", true)){
            switchCompat.setChecked(true);
        }else {
            switchCompat.setChecked(false);
        }


        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
                    editor.putBoolean("status",true);
                    editor.apply();

                }else {
                    SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
                    editor.putBoolean("status",false);
                    editor.apply();
                }
            }
        });



        tvTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this,TermsActivity.class));
            }
        });

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tools.share(SettingsActivity.this, "");
            }
        });

        tvConnectLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTvConnectionDialog();
            }
        });

    }

    private void showTvConnectionDialog() {
        progressBar.setVisibility(View.VISIBLE);
        //get user id from sharedpreference
        SharedPreferences prefs = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE);
        String userId = prefs.getString(Constants.USER_ID,"");

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        TvConnectionApi api = retrofit.create(TvConnectionApi.class);
        Call<TvConnection> call = api.getConnectionCode(Config.API_KEY, userId);
        call.enqueue(new Callback<TvConnection>() {
            @Override
            public void onResponse(Call<TvConnection> call, Response<TvConnection> response) {
                if (response.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    final Dialog dialog = new Dialog(SettingsActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.tv_code_dialog_layout);
                    TextView codeTextView = dialog.findViewById(R.id.code_text_view);
                    codeTextView.setText(response.body().getCode());
                    dialog.setCancelable(true);
                    dialog.show();
                }
            }

            @Override
            public void onFailure(Call<TvConnection> call, Throwable t) {

            }
        });
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

}
