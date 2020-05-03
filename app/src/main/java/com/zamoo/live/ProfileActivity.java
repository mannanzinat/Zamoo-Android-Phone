package com.zamoo.live;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zamoo.live.network.RetrofitClient;
import com.zamoo.live.network.apis.DeactivateAccountApi;
import com.zamoo.live.network.apis.ProfileApi;
import com.zamoo.live.network.model.ResponseStatus;
import com.zamoo.live.utils.ApiResources;
import com.zamoo.live.utils.Constants;
import com.zamoo.live.utils.FileUtil;
import com.zamoo.live.utils.ToastMsg;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class ProfileActivity extends AppCompatActivity {
    private EditText etName,etEmail,etPass, genderSpinner;
    private Button btnUpdate, deactivateBt;
    private ProgressDialog dialog;
    private String URL="",strGender;
    private CircleImageView userIv;
    private static final int GALLERY_REQUEST_CODE = 1;
    private Uri imageUri;
    private ProgressBar progressBar;
    private String id;
    boolean isDark;
    private String selectedGender = "Male";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "profile_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etName = findViewById(R.id.name);
        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.password);
        btnUpdate = findViewById(R.id.signup);
        userIv = findViewById(R.id.user_iv);
        progressBar = findViewById(R.id.progress_bar);
        deactivateBt = findViewById(R.id.deactive_bt);
        genderSpinner = findViewById(R.id.gender_spinner);

        SharedPreferences preferences = getSharedPreferences(Constants.USER_DATA,MODE_PRIVATE);
        id = preferences.getString(Constants.USER_ID,"0");
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etEmail.getText().toString().equals("")){
                    Toast.makeText(ProfileActivity.this,"Please enter valid email",Toast.LENGTH_LONG).show();
                    return;
                }else if (etName.getText().toString().equals("")){
                    Toast.makeText(ProfileActivity.this,"Please enter name",Toast.LENGTH_LONG).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                String email = etEmail.getText().toString();
                String pass = etPass.getText().toString();
                String name = etName.getText().toString();

                updateProfile(id, email, name, pass);

            }
        });

        String urlProfile = new ApiResources().getProfileURL()+preferences.getString(Constants.USER_EMAIL,"");

        //gender spinner setup
        final String[] genderArray = new String[2];
        genderArray[0] = "Male";
        genderArray[1] = "Female";
        genderSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Select Gender");
                builder.setSingleChoiceItems(genderArray, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((TextView) v).setText(genderArray[i]);
                        selectedGender = genderArray[i];
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });


        getProfile();

    }

    @Override
    protected void onStart() {
        super.onStart();

        userIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        deactivateBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDeactiveDialog();

            }
        });

    }

    private void showDeactiveDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_deactivate, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        final EditText passEt = view.findViewById(R.id.pass_et);
        final EditText reasonEt = view.findViewById(R.id.reason_et);
        final Button okBt = view.findViewById(R.id.ok_bt);
        Button cancelBt = view.findViewById(R.id.cancel_bt);
        ImageView closeIv = view.findViewById(R.id.close_iv);
        final ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        LinearLayout topLayout = view.findViewById(R.id.top_layout);
        if (isDark) {
            topLayout.setBackgroundColor(getResources().getColor(R.color.overlay_dark_30));
        }

        okBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = passEt.getText().toString();
                String reason = reasonEt.getText().toString();

                if (TextUtils.isEmpty(pass)) {
                    new ToastMsg(ProfileActivity.this).toastIconError("Please enter your password");
                    return;
                } else if(TextUtils.isEmpty(reason)) {
                    new ToastMsg(ProfileActivity.this).toastIconError("Please enter your reason");
                    return;
                }
                deactivateAccount(pass, reason, alertDialog, progressBar);


            }
        });

        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


    }

    private void deactivateAccount(String pass, String reason, final AlertDialog alertDialog, final ProgressBar progressBar) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        DeactivateAccountApi api = retrofit.create(DeactivateAccountApi.class);
        Call<ResponseStatus> call = api.deactivateAccount(id, pass, reason, Config.API_KEY);
        call.enqueue(new Callback<ResponseStatus>() {
            @Override
            public void onResponse(Call<ResponseStatus> call, retrofit2.Response<ResponseStatus> response) {

                if (response.code() == 200) {
                    ResponseStatus resStatus = response.body();
                    if (resStatus.equals("success")) {
                        //delete from firebase authintication
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null){
                            FirebaseAuth.getInstance().getCurrentUser().delete();
                        }


                        logoutUser();
                        new ToastMsg(ProfileActivity.this).toastIconSuccess(resStatus.getData());
                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        alertDialog.dismiss();
                        finish();
                    } else {
                        new ToastMsg(ProfileActivity.this).toastIconError(resStatus.getData());
                        alertDialog.dismiss();
                    }

                } else {
                    new ToastMsg(ProfileActivity.this).toastIconError("Something went wrong");
                    alertDialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<ResponseStatus> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(ProfileActivity.this).toastIconError("Something went wrong");
                alertDialog.dismiss();
            }
        });

    }

    public void logoutUser() {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE).edit();
        editor.putString(Constants.USER_NAME, null);
        editor.putString(Constants.USER_EMAIL, null);
        editor.putString(Constants.USER_ID,null);
        editor.putBoolean(Constants.USER_LOGIN_STATUS,false);
        editor.apply();
        editor.commit();
    }

    private void openGallery() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , GALLERY_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            /*case 0:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    userIv.setImageURI(selectedImage);
                }
                break;*/
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    userIv.setImageURI(selectedImage);
                    imageUri = selectedImage;
                }
                break;
        }

    }

    public void saveImageUrl(String imageUrl) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE).edit();
        editor.putString(Constants.USER_PROFILE_IMAGE_URL, imageUrl);
        editor.apply();
        editor.commit();
    }


    private void getProfile(){
        SharedPreferences preferences = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE);
        String userName = preferences.getString(Constants.USER_NAME,"");
        String userEmail = preferences.getString(Constants.USER_EMAIL,"");
        String userProfileImage = preferences.getString(Constants.USER_PROFILE_IMAGE_URL,"");
        String useGender = preferences.getString(Constants.USER_GENDER, "");


        Picasso.get()
                .load(Uri.parse(userProfileImage))
                .placeholder(R.drawable.ic_account_circle_black)
                .error(R.drawable.ic_account_circle_black)
                .into(userIv);

        etName.setText(userName);
        etEmail.setText(userEmail);

        if (useGender.isEmpty()) {
            genderSpinner.setText(R.string.male);
        } else {
            genderSpinner.setText(useGender);
            selectedGender = useGender;
        }

    }

    private void updateProfile(final String idString, final String emailString, final String nameString, final String passString){
        File file = null;
        RequestBody requestFile = null;
        MultipartBody.Part multipartBody = null;
        try {
            file = FileUtil.from(ProfileActivity.this, imageUri);
            requestFile = RequestBody.create(MediaType.parse("multipart/form-data"),
                    file);

            multipartBody = MultipartBody.Part.createFormData("photo",
                    file.getName(),requestFile);

        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody email = RequestBody.create(MediaType.parse("text/plain"), emailString);
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), idString);
        final RequestBody name = RequestBody.create(MediaType.parse("text/plain"), nameString);
        RequestBody password = RequestBody.create(MediaType.parse("text/plain"), passString);
        RequestBody gender = RequestBody.create(MediaType.parse("text/plain"), selectedGender);
        RequestBody key = RequestBody.create(MediaType.parse("text/plain"), Config.API_KEY);

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ProfileApi api = retrofit.create(ProfileApi.class);
        Call<ResponseBody> call = api.updateProfile(id, name, email, password, gender, key, multipartBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.code() == 200) {
                    SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_DATA, MODE_PRIVATE).edit();
                    editor.putString(Constants.USER_NAME, nameString);
                    editor.putString(Constants.USER_EMAIL, emailString);
                    editor.putString(Constants.USER_ID, idString);
                    editor.putString(Constants.USER_PROFILE_IMAGE_URL, String.valueOf(imageUri));
                    editor.putString(Constants.USER_PASSWORD, passString);
                    editor.putString(Constants.USER_GENDER, selectedGender);
                    editor.putBoolean(Constants.USER_LOGIN_STATUS,true);
                    editor.apply();
                    editor.commit();
                    //update ui
                    getProfile();

                    Toast.makeText(ProfileActivity.this, "Success.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                t.printStackTrace();
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
