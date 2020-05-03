package com.zamoo.live;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;

public class FacebookSignInActivity extends AppCompatActivity {
    private static int RC_SIGN_IN_FACEBOOK = 123;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "sign_up_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        firebaseAuth = FirebaseAuth.getInstance();
        signInWithFacebook();
    }

    private void signInWithFacebook() {

        if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
               // final String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                //already signed in
                if (!FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber().isEmpty()){
                    //sendDataToServer(phoneNumber);*/
                }

            }

        } else {

            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.FacebookBuilder().build());
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN_FACEBOOK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN_FACEBOOK) {

            final IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in

            } else {
                // sign in failed
                if (response == null) {
                    //Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                    onBackPressed();
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
        } else {
            //failed
            //Toast.makeText(this, "Facebook login success", Toast.LENGTH_SHORT).show();

        }
    }
}
