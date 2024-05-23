package com.example.foody.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foody.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.internal.OnConnectionFailedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements OnConnectionFailedListener, View.OnClickListener, FirebaseAuth.AuthStateListener {
     Button btnGoogleSignin;
     LoginButton btnFacebookSignin;
    GoogleApiClient apiClient;
    public static final int REQUEST_CODE_GOOGLE = 3;
    public static final int REQUEST_CODE_FACEBOOK = 4;
    public static int CHECK_AUTH_PROVIDER_SIGNIN = 0; // 0: Google, 1: Facebook
    FirebaseAuth firebaseAuth;
    CallbackManager callBackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.layout_login);
        firebaseAuth = firebaseAuth.getInstance();
         callBackManager = CallbackManager.Factory.create();

        btnGoogleSignin = (Button) findViewById(R.id.btnGoogleSingin);
        btnFacebookSignin = findViewById(R.id.btnFacebookSignin);
        btnFacebookSignin.setPermissions("email", "public_profile"); //  set profile and email permission
        btnFacebookSignin.registerCallback(callBackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                CHECK_AUTH_PROVIDER_SIGNIN = 1;
                String tokenID = loginResult.getAccessToken().getToken();
                authenFirebase(tokenID);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(@NonNull FacebookException e) {

            }
        });
        btnGoogleSignin.setOnClickListener(this);
        clientGoogleSignIn();
    }

    private void googleSignIn(GoogleApiClient apiClient){
        CHECK_AUTH_PROVIDER_SIGNIN = 0;
        Intent iGoogleSignIn = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        startActivityForResult(iGoogleSignIn, REQUEST_CODE_GOOGLE);
    }

    private void clientGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

         apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this::onConnectionFailed)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
    private void authenFirebase(String tokenID){
        AuthCredential authCredential;
        if (CHECK_AUTH_PROVIDER_SIGNIN == 0) {
            authCredential = GoogleAuthProvider.getCredential(tokenID, null);
            firebaseAuth.signInWithCredential(authCredential);
        }else if (CHECK_AUTH_PROVIDER_SIGNIN == 1) {
            authCredential = FacebookAuthProvider.getCredential(tokenID);
            firebaseAuth.signInWithCredential(authCredential);
        }

    }
     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE) {
            if (resultCode == RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                GoogleSignInAccount account = result.getSignInAccount();
                String tokenID = account.getIdToken();
                authenFirebase(tokenID);

            }
        } else if (requestCode == REQUEST_CODE_FACEBOOK) {
            callBackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (v.getId() == R.id.btnGoogleSingin) {
            googleSignIn(apiClient);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(this);
    }
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            Intent iHomePage = new Intent(this, HomePageActivity.class);
            startActivity(iHomePage);
        } else{
            Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
        }
    }
}
