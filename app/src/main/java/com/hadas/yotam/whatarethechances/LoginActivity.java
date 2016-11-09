package com.hadas.yotam.whatarethechances;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    FragmentTransaction fragmentTransaction;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;
    GoogleApiClient mGoogleApiClient;
    Boolean googleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(savedInstanceState==null) {
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(android.R.id.content,new SignInFragment()).commit();
        }
        //set googleSignIn boolean to true, variable to know if google sign in is available
        googleSignIn=true;
        //setting up GoogleSignInOptions
        GoogleSignInOptions mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        if(mGoogleApiClient==null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    //if connection failed set googleSignIn to false, google sign in is NOT available
                    googleSignIn = false;
                }
            }).addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                    .build();
        }

        //set Firebase Auth and User
        if(AppConstants.mFirebaseUser==null)
            Utilities.setUserAuth();
        //set my Auth
        mAuth = AppConstants.mFirebaseAuth;
        //Define listener to start main activity if user log in
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null)
                {
                    Log.d("tag",firebaseAuth.getCurrentUser().getUid()+" "+firebaseAuth.getCurrentUser().getProviders());
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth!=null &&mAuthStateListener!=null)
            mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth!=null &&mAuthStateListener!=null)
            mAuth.removeAuthStateListener(mAuthStateListener);
    }
}
